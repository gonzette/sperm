package com.dirlt.java.veritas;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.*;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.jboss.netty.handler.codec.http.HttpRequestEncoder;
import org.jboss.netty.handler.codec.http.HttpResponseDecoder;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.TimerTask;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created with IntelliJ IDEA.
 * User: dirlt
 * Date: 8/13/13
 * Time: 2:01 PM
 * To change this template use File | Settings | File Templates.
 */
public class ProxyConnector {
    private static ProxyConnector instance = null;

    private Configuration configuration;
    // how many connection overall.
    private AtomicInteger connectionNumber = new AtomicInteger(0);
    // how many connection available.
    private BlockingQueue<ProxyHandler> availableConnectionPool = null;
    private float avgAvailableConnectionPoolSize = 0.0f;
    private Map<String, Node> nodes = null;

    public static class Node {
        InetSocketAddress socketAddress;
        float staticWeight;
        ClientBootstrap bootstrap;
        // more fields.
        AtomicInteger connectFailureCount = new AtomicInteger(0);
        AtomicInteger readWriteFailureCount = new AtomicInteger(0);
        AtomicInteger connectionNumber = new AtomicInteger(0);
        float avgConnectFailureCount = 0.0f;
        float avgReadWriteFailureCount = 0.0f;

        enum ClosedCause {
            kConnectionFailed,
            kReadWriteFailed
        }

        public float getWeight() {
            // less weight, more prefer.
            // here we don't consider failure count thread safe.
            float dynWeight = avgConnectFailureCount * 0.6f + avgReadWriteFailureCount * 0.3f + connectionNumber.get() * 0.1f;
            return dynWeight * 1.0f / staticWeight;
        }

        public boolean isCreatable() {
            return connectionNumber.get() < 8192;
        }

        public boolean isClosable() {
            return connectionNumber.get() > 2;
        }
    }

    public String getStat() {
        StringBuffer sb = new StringBuffer();
        sb.append(String.format("average available connection pool size = %.2f\n", avgAvailableConnectionPoolSize));
        sb.append(String.format("connection number = %d\n", connectionNumber.get()));
        for (Map.Entry<String, Node> entry : nodes.entrySet()) {
            sb.append(String.format("node = %s\n", entry.getKey()));
            sb.append(String.format("\tconnection number = %d\n", entry.getValue().connectionNumber.get()));
            sb.append(String.format("\taverage connect failure count = %.2f\n", entry.getValue().avgConnectFailureCount));
            sb.append(String.format("\taverage read-write failure count = %.2f\n", entry.getValue().avgReadWriteFailureCount));
        }
        return sb.toString();
    }

    public static void init(Configuration configuration) {
        instance = new ProxyConnector(configuration);
    }

    public static ProxyConnector getInstance() {
        return instance;
    }

    public ProxyHandler acquireConnection() {
        try {
            ProxyHandler handler = null;
            int retry = 3;
            while (retry > 0) {
                handler = availableConnectionPool.poll(50, TimeUnit.MILLISECONDS);
                if (handler == null) {
                    VeritasServer.logger.debug("proxy connector acquire connection timeout");
                    addConnection(1);
                } else {
                    VeritasServer.logger.debug("proxy connector acquire connection OK!");
                    break;
                }
                retry--;
            }
            return handler;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void releaseConnection(ProxyHandler handler) {
        try {
            VeritasServer.logger.debug("proxy connector release connection");
            availableConnectionPool.put(handler);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void onChannelClosed(Channel channel, Node.ClosedCause cause) {
        VeritasServer.logger.debug("connector on channel closed end");
        connectionNumber.decrementAndGet();
        VeritasServer.logger.debug("closed channel remote address = " + channel.getAttachment() + ", cause = " + cause);
        Node node = nodes.get(channel.getAttachment());
        if (cause == Node.ClosedCause.kConnectionFailed) {
            node.connectFailureCount.incrementAndGet();
        } else {
            node.connectionNumber.decrementAndGet();
            if (cause == Node.ClosedCause.kReadWriteFailed) {
                node.readWriteFailureCount.incrementAndGet();
            }
        }
    }

    public Node selectNode() {
        Node node = null;
        float minWeight = Float.MAX_VALUE;
        for (Map.Entry<String, Node> entry : nodes.entrySet()) {
            Node x = entry.getValue();
            if (!x.isCreatable()) {
                continue;
            }
            float weight = x.getWeight();
            if (weight < minWeight) {
                minWeight = weight;
                node = x;
            }
        }
        // may return null.
        return node;
    }

    public void addConnection(int count) {
        while (count > 0) {
            if (connectionNumber.get() >= configuration.getProxyMaxConnectionNumber()) {
                VeritasServer.logger.debug("connection number get max");
                return;
            }
            Node node = selectNode();
            if (node == null) {
                return;
            }
            connectionNumber.incrementAndGet();
            node.bootstrap.connect(node.socketAddress);
            count--;
        }
    }

    public ProxyConnector(final Configuration configuration) {
        this.configuration = configuration;
        availableConnectionPool = new LinkedBlockingQueue<ProxyHandler>(configuration.getProxyQueueSize());
        ChannelFactory channelFactory = new NioClientSocketChannelFactory(
                Executors.newCachedThreadPool(),
                Executors.newCachedThreadPool(),
                configuration.getProxyAcceptIOThreadNumber(),
                configuration.getProxyIOThreadNumber());
        // parse nodes.
        nodes = new HashMap<String, Node>();
        String backendNodes = configuration.getBackendNodes();
        for (String s : backendNodes.split(",")) {
            String[] ss = s.split(":");
            String host = ss[0];
            int port = Integer.valueOf(ss[1]).intValue();
            final Node node = new Node();
            node.socketAddress = new InetSocketAddress(host, port);
            node.staticWeight = 1.0f;
            try {
                String hostname = InetAddress.getLocalHost().getHostName();
                if (hostname.equals(host)) {
                    node.staticWeight = 2.0f;
                }
            } catch (Exception e) {
                // ignore it.
            }
            ClientBootstrap bootstrap = new ClientBootstrap(channelFactory);
            final ProxyConnector proxyConnector = this;
            bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
                public ChannelPipeline getPipeline() throws Exception {
                    ChannelPipeline pipeline = Channels.pipeline();
                    pipeline.addLast("decoder", new HttpResponseDecoder());
                    pipeline.addLast("encoder", new HttpRequestEncoder());
//                    pipeline.addLast("rto_handler", new ReadTimeoutHandler(timer, configuration.getProxyReadTimeout(), TimeUnit.MILLISECONDS));
//                    pipeline.addLast("wto_handler", new WriteTimeoutHandler(timer, configuration.getProxyWriteTimeout(), TimeUnit.MILLISECONDS));
                    pipeline.addLast("handler", new ProxyHandler(configuration, proxyConnector, node));
                    return pipeline;
                }
            });
            // control connect timeout
            // maybe specify by another option.
            bootstrap.setOption("connectTimeoutMillis", 50);
            node.bootstrap = bootstrap;
            nodes.put(node.socketAddress.toString(), node);
        }
        // timer to decrease failure count.
        java.util.Timer tickTimer = new java.util.Timer(true);
        tickTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                for (Map.Entry<String, Node> entry : nodes.entrySet()) {
                    Node node = entry.getValue();
                    node.avgConnectFailureCount = node.avgConnectFailureCount * 0.4f + node.connectFailureCount.get() * 0.6f;
                    node.avgReadWriteFailureCount = node.avgReadWriteFailureCount * 0.6f + node.readWriteFailureCount.get() * 0.6f;
                    node.connectFailureCount.set(0);
                    node.readWriteFailureCount.set(0);
                }
                avgAvailableConnectionPoolSize = avgAvailableConnectionPoolSize * 0.4f + availableConnectionPool.size() * 0.6f;
            }
        }, 0, configuration.getProxyTimerTickInterval());
    }
}
