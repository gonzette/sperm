package com.dirlt.java.peeper;

import org.jboss.netty.channel.*;
import org.jboss.netty.handler.codec.http.HttpResponse;

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
    public AsyncClient client;

    public ProxyHandler(Configuration configuration, ProxyConnector proxyConnector, ProxyConnector.Node node) {
        this.configuration = configuration;
        this.proxyConnector = proxyConnector;
        this.node = node;
    }

    @Override
    public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        PeepServer.logger.debug("proxy channel connected");
        connected = true;
        node.connectionNumber.incrementAndGet();
        channel = e.getChannel();
        channel.setReadable(false);
        proxyConnector.pushConnection(this);
    }

    @Override
    public void channelClosed(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        PeepServer.logger.debug("proxy connection closed");
    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx, final MessageEvent e) throws Exception {
        PeepServer.logger.debug("proxy message received");
        StatStore.getInstance().addCounter("proxy.rpc.in.count", 1);
        channel.setReadable(false);
        proxyConnector.pushConnection(this);
        HttpResponse httpResponse = (HttpResponse) e.getMessage();
        AsyncClient now = client;
        client = null;
        now.proxyBuffer = httpResponse.getContent();
        CpuWorkerPool.getInstance().submit(now);
    }

    @Override
    public void writeComplete(ChannelHandlerContext ctx, WriteCompletionEvent e) throws Exception {
        PeepServer.logger.debug("proxy write completed");
        StatStore.getInstance().addCounter("proxy.rpc.out.count", 1);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) {
        // e.getCause() instanceof ReadTimeoutException
        // e.getCause() instanceof WriteTimeoutException

        PeepServer.logger.debug("proxy exception caught : " + e.getCause());
        StatStore.getInstance().addCounter("proxy.exception.count", 1);

        e.getChannel().setAttachment(node.socketAddress.toString());
        proxyConnector.onChannelClosed(e.getChannel(),
                connected ? ProxyConnector.Node.ClosedCause.kReadWriteFailed :
                        ProxyConnector.Node.ClosedCause.kConnectionFailed);
        e.getChannel().close();

        // client maybe stuck.
        if (client != null) {
            client.proxyChannelClosed = true;
            CpuWorkerPool.getInstance().submit(client);
        }
    }
}
