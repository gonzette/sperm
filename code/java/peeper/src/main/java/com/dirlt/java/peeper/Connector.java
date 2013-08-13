package com.dirlt.java.peeper;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.*;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.jboss.netty.handler.codec.http.HttpChunkAggregator;
import org.jboss.netty.handler.codec.http.HttpRequestDecoder;
import org.jboss.netty.handler.codec.http.HttpResponseEncoder;
import org.jboss.netty.handler.timeout.ReadTimeoutHandler;
import org.jboss.netty.handler.timeout.WriteTimeoutHandler;
import org.jboss.netty.util.HashedWheelTimer;
import org.jboss.netty.util.Timer;

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
    private ClientBootstrap bootstrap = null;
    // each time addConnection will create at least following number connections.
    public static final int kAddConnectionStep = 4;

    public static class Node {
        public InetSocketAddress socketAddress;
        public float staticWeight;
        // more fields.
        public static final int kMaxFailureCount = 10;
        // do not use atomic integer because we have threshold.
        int connectFailureCount = 0;
        int readWriteFailureCount = 0;
        int connectionNumber = 0;

        enum ClosedCause {
            kActive,
            kConnectionFailed,
            kReadWriteFailed
        }

        public float getWeight() {
            // less weight, more prefer.
            // here we don't consider failure count thread safe.
            float dynWeight = connectFailureCount * 0.6f + readWriteFailureCount * 0.3f + connectionNumber * 0.1f;
            return dynWeight * 1.0f / staticWeight;
        }

        public boolean isSelectable() {
            return connectFailureCount < kMaxFailureCount;
        }
    }

    private Map<String, Node> nodes = null;

    public static void init(Configuration configuration) {
        instance = new Connector(configuration);
    }

    public static Connector getInstance() {
        return instance;
    }

    public void request(AsyncClient client) {
        requestQueue.add(client);
    }

    public void onChannelClosed(Channel channel, Node.ClosedCause cause) {
        Node node = nodes.get(channel.getRemoteAddress().toString());
        connectionNumber.decrementAndGet();
        node.connectionNumber -= 1;
        if (cause == Node.ClosedCause.kConnectionFailed) {
            synchronized (node) {
                if (node.connectFailureCount < Node.kMaxFailureCount) {
                    node.connectFailureCount += 1;
                }
            }
        } else if (cause == Node.ClosedCause.kReadWriteFailed) {
            synchronized (node) {
                if (node.readWriteFailureCount < Node.kMaxFailureCount) {
                    node.readWriteFailureCount += 1;
                }
            }
        } else {
            // cause == kActive;
        }
    }

    public void balanceConnection() {
        // TODO(dirlt):
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
        bootstrap = new ClientBootstrap(channelFactory);
        final Connector connector = this;
        bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
            public ChannelPipeline getPipeline() throws Exception {
                ChannelPipeline pipeline = Channels.pipeline();
                pipeline.addLast("decoder", new HttpRequestDecoder());
                pipeline.addLast("aggregator", new HttpChunkAggregator(1024 * 1024 * 8));
                pipeline.addLast("encoder", new HttpResponseEncoder());
                pipeline.addLast("rto_handler", new ReadTimeoutHandler(timer, configuration.getProxyReadTimeout(), TimeUnit.MILLISECONDS));
                pipeline.addLast("wto_handler", new WriteTimeoutHandler(timer, configuration.getProxyWriteTimeout(), TimeUnit.MILLISECONDS));
                pipeline.addLast("handler", new ProxyHandler(configuration, connector));
                return pipeline;
            }
        });
        // parse nodes.
        nodes = new HashMap<String, Node>();
        String backendNodes = configuration.getBackendNodes();
        for (String s : backendNodes.split(",")) {
            String[] ss = s.split(":");
            String host = ss[0];
            int port = Integer.valueOf(ss[1]).intValue();
            Node node = new Node();
            node.socketAddress = new InetSocketAddress(host, port);
            node.staticWeight = 1.0f; // TODO(dirlt): some preference?
            // TODO(dirlt): by comparing local hostname
            nodes.put(node.socketAddress.toString(), node);
        }
        // timer to decrease failure count.
        java.util.Timer tickTimer = new java.util.Timer(true);
        tickTimer.scheduleAtFixedRate(new TimerTask() {
            private int recoveryTickCount = configuration.getProxyRecoveryTickNumber();

            @Override
            public void run() {
                recoveryTickCount -= 1;
                if (recoveryTickCount == 0) {
                    for (Map.Entry<String, Node> entry : nodes.entrySet()) {
                        Node node = entry.getValue();
                        synchronized (node) {
                            if (node.connectFailureCount > 0) {
                                node.connectFailureCount -= 1;
                            }
                            if (node.readWriteFailureCount > 0) {
                                node.readWriteFailureCount -= 1;
                            }
                        }
                    }
                    recoveryTickCount = configuration.getProxyRecoveryTickNumber();
                }
                balanceConnection();
            }
        }, 0, configuration.getProxyTimerTickInterval());
    }
}
