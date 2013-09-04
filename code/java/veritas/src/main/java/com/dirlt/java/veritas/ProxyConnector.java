package com.dirlt.java.veritas;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.*;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.jboss.netty.handler.codec.http.HttpRequestEncoder;
import org.jboss.netty.handler.codec.http.HttpResponseDecoder;
import org.jboss.netty.util.Timeout;
import org.jboss.netty.util.TimerTask;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
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
    private int rrId = 0;
    // how many connection overall.
    private AtomicInteger connectionNumber = new AtomicInteger(0);
    // how many connection available.
    private Map<String, Node> nodes;
    private Node[] aNodes;

    public static class Node {
        InetSocketAddress socketAddress;
        float staticWeight;
        ClientBootstrap bootstrap;
        BlockingQueue<ProxyHandler> pool;
        AtomicInteger connectionNumber = new AtomicInteger(0);
        int punishCount;
        static final int kPunishThreshold = 10;
    }

    public String getStat() {
        StringBuffer sb = new StringBuffer();
        sb.append(String.format("connection number = %d\n", connectionNumber.get()));
        for (Map.Entry<String, Node> entry : nodes.entrySet()) {
            sb.append(String.format("node = %s\n", entry.getKey()));
            sb.append(String.format("\tconnection number = %d\n", entry.getValue().connectionNumber.get()));
            sb.append(String.format("\tavailable connection = %d\n", entry.getValue().pool.size()));
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
        int id = rrId;
        rrId = (rrId + 1) % aNodes.length;
        boolean sleep = false;
        for (int i = 0; i < aNodes.length; i++) {
            int idx = (id + i) % aNodes.length;
            Node node = aNodes[idx];
            if (node.punishCount >= (Node.kPunishThreshold - 2)) {
                continue;
            }
            try {
                sleep = true;
                int retry = 3;
                while (retry > 0) {
                    ProxyHandler handler = node.pool.poll(configuration.getProxyConnectTimeout(), TimeUnit.MILLISECONDS);
                    if (handler == null) {
                        VeritasServer.logger.debug("proxy connector acquire connection timeout");
                        connect(node);
                    } else {
                        VeritasServer.logger.debug("proxy connector acquire connection OK!");
                        return handler;
                    }
                    retry--;
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
                // pass.
            }
        }
        if (!sleep) {
            // if we turn around and find no connection available, we have to wait.
            try {
                Thread.sleep(configuration.getProxyConnectTimeout());
            } catch (Exception e) {
                e.printStackTrace();
                // pass.
            }
        }
        return null;
    }

    public void releaseConnection(ProxyHandler handler) {
        try {
            VeritasServer.logger.debug("proxy connector release connection");
            handler.node.pool.put(handler);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void onChannelClosed(Channel channel, final Node node) {
        // connection closed.
        VeritasServer.logger.debug("connector on channel closed end");
        connectionNumber.decrementAndGet();
        node.connectionNumber.decrementAndGet();
        node.punishCount += 1;
        if (node.punishCount >= Node.kPunishThreshold) {
            node.punishCount = Node.kPunishThreshold;
        }
        AsyncClient.timer.newTimeout(new TimerTask() {
            @Override
            public void run(Timeout timeout) throws Exception {
                ProxyConnector.getInstance().connect(node);
            }
        }, configuration.getProxyConnectTimeout() * (1 << node.punishCount), TimeUnit.MILLISECONDS);
    }

    public void connect(Node node) {
        // if connection number on this node got threshold.
        if (node.connectionNumber.get() < configuration.getProxyConnectionNumberPerNode()) {
            connectionNumber.incrementAndGet();
            node.connectionNumber.incrementAndGet();
            node.bootstrap.connect(node.socketAddress);
        }
    }

    public ProxyConnector(final Configuration configuration) {
        this.configuration = configuration;
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
                    pipeline.addLast("handler", new ProxyHandler(configuration, proxyConnector, node));
                    return pipeline;
                }
            });
            // control connect timeout
            // maybe specify by another option.
            bootstrap.setOption("connectTimeoutMillis", configuration.getProxyConnectTimeout());
            node.bootstrap = bootstrap;
            node.pool = new LinkedBlockingQueue<ProxyHandler>(configuration.getProxyConnectionNumberPerNode() * 2 + 16);
            nodes.put(node.socketAddress.toString(), node);
        }
        aNodes = new Node[nodes.size()];
        int index = 0;
        for (Map.Entry<String, Node> entry : nodes.entrySet()) {
            aNodes[index] = entry.getValue();
            index += 1;
        }
    }
}
