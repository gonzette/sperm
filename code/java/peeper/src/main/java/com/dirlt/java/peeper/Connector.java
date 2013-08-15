package com.dirlt.java.peeper;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.*;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.jboss.netty.handler.codec.http.HttpRequestEncoder;
import org.jboss.netty.handler.codec.http.HttpResponseDecoder;
import org.jboss.netty.util.HashedWheelTimer;
import org.jboss.netty.util.Timer;

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
public class Connector {
    private static Connector instance = null;

    private Configuration configuration;
    private AtomicInteger connectionNumber = new AtomicInteger(0);
    private BlockingQueue<AsyncClient> requestQueue = null;
    private float avgRequestQueueSize = 0.0f;
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
        sb.append(String.format("average request queue size = %.2f\n", avgRequestQueueSize));
        sb.append(String.format("connection number = %d\n", connectionNumber.get()));
        for (Map.Entry<String, Node> entry : nodes.entrySet()) {
            sb.append(String.format("node: %s\n", entry.getKey()));
            sb.append(String.format("\tconnection number = %d\n", entry.getValue().connectionNumber.get()));
            sb.append(String.format("\taverage connect failure count = %.2f\n", entry.getValue().avgConnectFailureCount));
            sb.append(String.format("\taverage read-write failure count = %.2f\n", entry.getValue().avgReadWriteFailureCount));
        }
        return sb.toString();
    }

    public static void init(Configuration configuration) {
        instance = new Connector(configuration);
    }

    public static Connector getInstance() {
        return instance;
    }

    public void pushRequest(AsyncClient client) {
        requestQueue.add(client);
    }

    public AsyncClient popRequest() {
        AsyncClient client = null;
        while (true) {
            try {
                // poll interval as 500ms.
                client = requestQueue.poll(500, TimeUnit.MICROSECONDS);
            } catch (InterruptedException e) {
                // ignore.
            }
            if (client != null) {
                break;
            }
        }
        return client;
    }

    public void onChannelClosed(Channel channel, Node.ClosedCause cause) {
        PeepServer.logger.debug("connector on channel closed end");
        connectionNumber.decrementAndGet();
        PeepServer.logger.debug("closed channel remote address = " + channel.getAttachment() + ", cause = " + cause);
        Node node = nodes.get(channel.getAttachment());
        node.connectionNumber.decrementAndGet();
        if (cause == Node.ClosedCause.kReadWriteFailed) {
            node.readWriteFailureCount.incrementAndGet();
        } else if (cause == Node.ClosedCause.kConnectionFailed) {
            node.connectFailureCount.incrementAndGet();
        }
    }

    public void addConnection() {
        //PeepServer.logger.debug("add connection");
        // avg load is low and connection number is enough.
        if (avgRequestQueueSize < 1.5f && connectionNumber.get() >= configuration.getProxyMinConnectionNumber()) {
            return;
        }
        // otherwise we have to make more connection.
        for (int i = 0; i < configuration.getProxyAddConnectionNumberStep(); i++) {
            if (connectionNumber.get() >= configuration.getProxyMaxConnectionNumber()) {
                break;
            }
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
            // if node can be selected to be connected.
            if (node == null) {
                break;
            }
            connectionNumber.incrementAndGet();
            node.bootstrap.connect(node.socketAddress);
        }
    }

    public Connector(final Configuration configuration) {
        this.configuration = configuration;
        requestQueue = new LinkedBlockingQueue<AsyncClient>(configuration.getProxyQueueSize());
        final Timer timer = new HashedWheelTimer();
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
            final Connector connector = this;
            bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
                public ChannelPipeline getPipeline() throws Exception {
                    ChannelPipeline pipeline = Channels.pipeline();
                    pipeline.addLast("decoder", new HttpResponseDecoder());
                    pipeline.addLast("encoder", new HttpRequestEncoder());
//                pipeline.addLast("rto_handler", new ReadTimeoutHandler(timer, configuration.getProxyReadTimeout(), TimeUnit.MILLISECONDS));
//                pipeline.addLast("wto_handler", new WriteTimeoutHandler(timer, configuration.getProxyWriteTimeout(), TimeUnit.MILLISECONDS));
                    pipeline.addLast("handler", new ProxyHandler(configuration, connector, node));
                    return pipeline;
                }
            });
            node.bootstrap = bootstrap;
            nodes.put(node.socketAddress.toString(), node);
        }
        // timer to decrease failure count.
        java.util.Timer tickTimer = new java.util.Timer(true);
        tickTimer.scheduleAtFixedRate(new TimerTask() {
            private int recoveryTickNumber = configuration.getProxyRecoveryTickNumber();

            @Override
            public void run() {
                recoveryTickNumber -= 1;
                if (recoveryTickNumber == 0) {
                    for (Map.Entry<String, Node> entry : nodes.entrySet()) {
                        Node node = entry.getValue();
                        node.avgConnectFailureCount = node.avgConnectFailureCount * 0.4f + node.connectFailureCount.get() * 0.6f;
                        node.avgReadWriteFailureCount = node.avgReadWriteFailureCount * 0.6f + node.readWriteFailureCount.get() * 0.6f;
                        node.connectFailureCount.set(0);
                        node.readWriteFailureCount.set(0);
                    }
                }
                avgRequestQueueSize = avgRequestQueueSize * 0.4f + requestQueue.size() * 0.6f;
                addConnection();
            }
        }, 0, configuration.getProxyTimerTickInterval());
    }
}
