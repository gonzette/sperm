package com.dirlt.java.veritas;

import org.jboss.netty.channel.*;
import org.jboss.netty.handler.codec.http.HttpRequest;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Set;
import java.util.TreeSet;

/**
 * Created with IntelliJ IDEA.
 * User: dirlt
 * Date: 12/8/12
 * Time: 1:44 AM
 * To change this template use File | Settings | File Templates.
 */

public class VeritasHandler extends SimpleChannelHandler {
    private static final Set<String> allowedPath = new TreeSet<String>();

    static {
        allowedPath.add("/stat");
        allowedPath.add("/tell");
    }

    private Configuration configuration;
    private AsyncClient client; // binding to the channel pipeline.

    public VeritasHandler(Configuration configuration) {
        this.configuration = configuration;
        client = new AsyncClient(configuration); // each handler corresponding a channel or a connection.
    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
        VeritasServer.logger.debug("veritas message received");
        HttpRequest request = (HttpRequest) e.getMessage();
        Channel channel = e.getChannel();
        StatStore stat = StatStore.getInstance();
        String path = null;
        String query = null;

        // invalid uri.
        try {
            URI uri = new URI(request.getUri());
            path = uri.getPath();
            if (path.isEmpty() || path.equals("/")) {
                path = "/stat";
            }
            query = uri.getQuery();
        } catch (URISyntaxException ex) {
            // ignore.
            stat.addCounter("veritas.uri.invalid.count", 1);
            channel.close();
            return;
        }

        // invalid path.
        if (!allowedPath.contains(path)) {
            stat.addCounter("veritas.uri.unknown.count", 1);
            channel.close(); // just close the connection.
            return;
        }

        stat.addCounter("veritas.rpc.in.count", 1);

        // as stat, we can easily handle it.
        if (path.equals("/stat")) {
            String content = StatStore.getStat();
            AsyncClient.writeContent(channel, content);
            return;
        }

        client.init();
        client.code = AsyncClient.Status.kHttpRequest;
        client.path = path;
        client.query = query;
        client.veritasChannel = channel;
        client.veritasBuffer = request.getContent();
        client.requestTimestamp = System.currentTimeMillis();
        channel.setReadable(false);
        CpuWorkerPool.getInstance().submit(client);
    }

    @Override
    public void writeComplete(ChannelHandlerContext ctx, WriteCompletionEvent e) throws Exception {
        VeritasServer.logger.debug("veritas write completed");
        StatStore.getInstance().addCounter("veritas.rpc.out.count", 1);
    }

    @Override
    public void channelOpen(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        VeritasServer.logger.debug("veritas.connection open");
    }

    @Override
    public void channelClosed(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        VeritasServer.logger.debug("veritas connection closed");
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
        // e.getCause() instanceof ReadTimeoutException
        // e.getCause() instanceof WriteTimeoutException

        VeritasServer.logger.debug("veritas exception caught : " + e.getCause());
        StatStore.getInstance().addCounter("veritas.exception.count", 1);
        e.getChannel().close();
    }
}