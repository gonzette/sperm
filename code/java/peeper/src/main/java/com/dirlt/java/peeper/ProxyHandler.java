package com.dirlt.java.peeper;

import org.jboss.netty.channel.*;
import org.jboss.netty.handler.codec.http.HttpRequest;

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
    private AsyncClient client = null;
    private boolean connected = false;

    public ProxyHandler(Configuration configuration, Connector connector) {
        this.configuration = configuration;
        this.connector = connector;
    }

    private void driveAsyncClient(Channel channel) {
        client = Connector.getInstance().popRequest();
        client.proxyChannel = channel;
        client.code = AsyncClient.Status.kProxyRequestId;
        client.run();
    }

    @Override
    public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        PeepServer.logger.debug("proxy channel connected");
        connected = true;
        driveAsyncClient(ctx.getChannel());
    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx, final MessageEvent e) throws Exception {
        PeepServer.logger.debug("proxy message received");
        HttpRequest request = (HttpRequest) e.getMessage();
        StatStore.getInstance().addCounter("proxy.rpc.in.count", 1);
        client.proxyBuffer = request.getContent();
        client.run();
        if (client.code == AsyncClient.Status.kResponse) {
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

        PeepServer.logger.debug("proxy exception caught");
        StatStore.getInstance().addCounter("proxy.exception.count", 1);

        connector.onChannelClosed(e.getChannel(),
                connected ? Connector.Node.ClosedCause.kReadWriteFailed : Connector.Node.ClosedCause.kConnectionFailed);
        e.getChannel().close();
    }
}
