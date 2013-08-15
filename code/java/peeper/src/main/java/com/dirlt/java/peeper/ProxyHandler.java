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
    private Connector connector;
    private Connector.Node node;
    private boolean connected = false;
    private AsyncClient client = null;

    public ProxyHandler(Configuration configuration, Connector connector, Connector.Node node) {
        this.configuration = configuration;
        this.connector = connector;
        this.node = node;
    }

    private void driveAsyncClient(Channel channel) {
        client = Connector.getInstance().popRequest();
        channel.setAttachment(node.socketAddress.toString());
        client.proxyChannel = channel;
        client.code = AsyncClient.Status.kProxyRequestId;
        client.run();
    }

    @Override
    public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        PeepServer.logger.debug("proxy channel connected");
        connected = true;
        node.connectionNumber.incrementAndGet();
        driveAsyncClient(ctx.getChannel());
    }

    @Override
    public void channelClosed(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        PeepServer.logger.debug("proxy connection closed");
    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx, final MessageEvent e) throws Exception {
        PeepServer.logger.debug("proxy message received");
        HttpResponse httpResponse = (HttpResponse) e.getMessage();
        StatStore.getInstance().addCounter("proxy.rpc.in.count", 1);
        client.proxyBuffer = httpResponse.getContent();
        client.run();
        if (client.code == AsyncClient.Status.kHttpResponse) {
            PeepServer.logger.debug("proxy handler try to fetch one async client");
            driveAsyncClient(ctx.getChannel());
        }
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

        if (client != null) {
            client.proxyChannelClosed = true;
            client.run();
        }
        e.getChannel().setAttachment(node.socketAddress.toString());
        connector.onChannelClosed(e.getChannel(),
                connected ? Connector.Node.ClosedCause.kReadWriteFailed : Connector.Node.ClosedCause.kConnectionFailed);
        e.getChannel().close();
    }
}
