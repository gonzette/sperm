package com.dirlt.java.veritas;

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
    public ProxyConnector.Node node;
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
        channel = e.getChannel();
        channel.setReadable(false);
        context = ctx;
        node.punishCount -= 1;
        proxyConnector.releaseConnection(this);
    }

    @Override
    public void channelClosed(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        VeritasServer.logger.debug("proxy connection closed");
        proxyConnector.onChannelClosed(e.getChannel(), node);
        // client maybe stuck.
        if (client != null) {
            client.proxyChannelClosed = true;
            CpuWorkerPool.getInstance().submit(client);
        }
    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx, final MessageEvent e) throws Exception {
        VeritasServer.logger.debug("proxy message received");
        // remove read exception.
        channel.setReadable(false);
        context.getPipeline().remove("rto_handler");
        // make connection available again.
        // clear stat first.
        AsyncClient now = client;
        client = null;
        proxyConnector.releaseConnection(this);
        // handle content.
        HttpResponse httpResponse = (HttpResponse) e.getMessage();
        now.proxyBuffer = httpResponse.getContent();
        CpuWorkerPool.getInstance().submit(now);
    }

    @Override
    public void writeComplete(ChannelHandlerContext ctx, WriteCompletionEvent e) throws Exception {
        VeritasServer.logger.debug("proxy write completed");
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) {
        // e.getCause() instanceof ReadTimeoutException
        // e.getCause() instanceof WriteTimeoutException

        VeritasServer.logger.debug("proxy exception caught : " + e.getCause());
        //e.getCause().printStackTrace();
        StatStore.getInstance().addCounter("proxy.exception." + e.getCause().getClass().getSimpleName() + ".count", 1);
        e.getChannel().close();
    }
}
