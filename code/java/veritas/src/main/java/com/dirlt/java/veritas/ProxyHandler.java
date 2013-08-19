package com.dirlt.java.veritas;

import org.jboss.netty.channel.*;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.timeout.ReadTimeoutHandler;

import java.util.concurrent.TimeUnit;

/**
 * Created with IntelliJ IDEA.
 * User: dirlt
 * Date: 8/13/13
 * Time: 2:23 PM
 * To change this template use File | Settings | File Templates.
 */
public class ProxyHandler extends SimpleChannelHandler {
    private Configuration configuration;
    private ProxyConnector proxyConnector;
    private ProxyConnector.Node node;
    private boolean connected = false;
    public Channel channel;
    public ChannelHandlerContext context;
    public AsyncClient client;

    public ProxyHandler(Configuration configuration, ProxyConnector proxyConnector, ProxyConnector.Node node) {
        this.configuration = configuration;
        this.proxyConnector = proxyConnector;
        this.node = node;
    }

    @Override
    public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        VeritasServer.logger.debug("proxy channel connected");
        connected = true;
        node.connectionNumber.incrementAndGet();
        channel = e.getChannel();
        channel.setReadable(false);
        context = ctx;
        proxyConnector.pushConnection(this);
    }

    @Override
    public void channelClosed(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        VeritasServer.logger.debug("proxy connection closed");

        e.getChannel().setAttachment(node.socketAddress.toString());
        proxyConnector.onChannelClosed(e.getChannel(),
                connected ? ProxyConnector.Node.ClosedCause.kReadWriteFailed :
                        ProxyConnector.Node.ClosedCause.kConnectionFailed);

        // client maybe stuck.
        if (client != null) {
            client.proxyChannelClosed = true;
            CpuWorkerPool.getInstance().submit(client);
        }
    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx, final MessageEvent e) throws Exception {
        VeritasServer.logger.debug("proxy message received");
        StatStore.getInstance().addCounter("proxy.rpc.in.count", 1);
        // remove read exception.
        channel.setReadable(false);
        context.getPipeline().remove("rto_handler");
        // make connection available again.
        proxyConnector.pushConnection(this);
        // client handle it.
        HttpResponse httpResponse = (HttpResponse) e.getMessage();
        AsyncClient now = client;
        client = null; // so handler don't own client any more.
        now.proxyBuffer = httpResponse.getContent();
        CpuWorkerPool.getInstance().submit(now);
    }

    @Override
    public void writeComplete(ChannelHandlerContext ctx, WriteCompletionEvent e) throws Exception {
        VeritasServer.logger.debug("proxy write completed");
        StatStore.getInstance().addCounter("proxy.rpc.out.count", 1);
        // remove write exception
        context.getPipeline().remove("wto_handler");
        // add read exception.
        context.getPipeline().addBefore(ctx.getName(), "rto_handler",
                new ReadTimeoutHandler(AsyncClient.timer,
                        configuration.getProxyReadTimeout(),
                        TimeUnit.MILLISECONDS));
        context.getChannel().setReadable(true);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) {
        // e.getCause() instanceof ReadTimeoutException
        // e.getCause() instanceof WriteTimeoutException

        VeritasServer.logger.debug("proxy exception caught : " + e.getCause());
        if (configuration.isDebug()) {
            e.getCause().printStackTrace();
        }
        StatStore.getInstance().addCounter("proxy.exception.count", 1);
        e.getChannel().close();
    }
}
