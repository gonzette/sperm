package com.dirlt.java.peeper;

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

public class PeepHandler extends SimpleChannelHandler {
    private static final Set<String> allowedPath = new TreeSet<String>();

    static {
        allowedPath.add("/stat");
        allowedPath.add("/peep");
    }

    private Configuration configuration;
    private AsyncClient client; // binding to the channel pipeline.

    public PeepHandler(Configuration configuration) {
        this.configuration = configuration;
        client = new AsyncClient(configuration); // each handler corresponding a channel or a connection.
    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
        PeepServer.logger.debug("peeper message received");
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
            stat.addCounter("peeper.uri.invalid.count", 1);
            channel.close();
            return;
        }

        // invalid path.
        if (!allowedPath.contains(path)) {
            stat.addCounter("peeper.uri.unknown.count", 1);
            channel.close(); // just close the connection.
            return;
        }

        stat.addCounter("peeper.rpc.in.count", 1);

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
        client.peeperChannel = channel;
        client.peeperBuffer = request.getContent();
        client.requestTimestamp = System.currentTimeMillis();
        CpuWorkerPool.getInstance().submit(client);
    }

    @Override
    public void writeComplete(ChannelHandlerContext ctx, WriteCompletionEvent e) throws Exception {
        PeepServer.logger.debug("peeper write completed");
        StatStore.getInstance().addCounter("peeper.rpc.out.count", 1);
    }

    @Override
    public void channelOpen(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        PeepServer.logger.debug("peeper.connection open");
    }

    @Override
    public void channelClosed(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        PeepServer.logger.debug("peeper connection closed");
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
        // e.getCause() instanceof ReadTimeoutException
        // e.getCause() instanceof WriteTimeoutException

        PeepServer.logger.debug("peeper exception caught : " + e.getCause());
        StatStore.getInstance().addCounter("peeper.exception.count", 1);
        client.peeperChannelClosed = true;
        e.getChannel().close();
    }
}