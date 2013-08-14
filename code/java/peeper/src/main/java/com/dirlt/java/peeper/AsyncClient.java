package com.dirlt.java.peeper;


import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.handler.codec.http.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created with IntelliJ IDEA.
 * User: dirlt
 * Date: 8/13/13
 * Time: 1:39 PM
 * To change this template use File | Settings | File Templates.
 */
public class AsyncClient implements Runnable {
    private static JSONParser parser = new JSONParser();
    private Configuration configuration;
    private static final long kDefaultTimeout = 100 * 1000; // 100s.(that's long enough).
    private static final String kDeviceIdMappingTable = "device_id_mapping";
    private static final String kDeviceIdMappingColumnFamily = "mapping";
    private static final String kDeviceIdMappingQualifier = "umid";
    private static final String kUserInfoTable = "user_info";
    private static final String kUserInfoColumnFamily = "info";
    private static final String kDeviceIdKeys[] = "imei,udid,mac,idfa,openudid,idfv,utdid".split(",");
    private static final Set<String> reqTypeKeys = new TreeSet<String>();

    static {
        reqTypeKeys.add("demographic");
        reqTypeKeys.add("geographic");
        reqTypeKeys.add("tcate");
    }

    enum Status {
        kStat,
        kHttpRequest,
        kSingleRequest,
        kMultiRequest,
        kResponse,
        kHttpResponse,
        kProxyRequestId,
        kProxyResponseId,
        kProxyRequestInfo,
        kProxyResponseInfo,
    }

    enum RequestStatus {
        kOK,
        kException,
        kTimeout,
    }

    public Status code;
    public String path; // url path.
    public String query; // url query;
    public AsyncClient parent;
    public RequestStatus requestStatus = RequestStatus.kOK;
    public boolean subRequest = false;
    public AtomicInteger refCounter;
    public List<AsyncClient> clients;

    public Channel peeperChannel;
    public Channel proxyChannel;
    public ChannelBuffer peeperBuffer;
    public Object peeperRequest;
    public Object peeperResponse;
    public ChannelBuffer proxyBuffer;
    public MessageProtos1.MultiReadRequest proxyIdRequest;
    public MessageProtos1.MultiReadResponse proxyIdResponse;
    public MessageProtos1.ReadRequest proxyInfoRequest;
    public MessageProtos1.ReadResponse proxyInfoResponse;
    public long requestTimestamp;
    public long requestTimeout;

    public AsyncClient(Configuration configuration) {
        this.configuration = configuration;
    }

    public void init() {
        subRequest = false;
        requestStatus = RequestStatus.kOK;
        clients = null;
        peeperChannel = null;
        proxyChannel = null;
    }

    public void raiseException() {
        if (proxyChannel != null) {
            Connector.getInstance().onChannelClosed(proxyChannel, Connector.Node.ClosedCause.kReadWriteFailed);
            proxyChannel.close();
        }
        requestStatus = RequestStatus.kException;
        code = Status.kResponse;
        run();
    }

    public int detectTimeout(String stage) {
        int rest = (int) (requestTimeout + requestTimestamp - System.currentTimeMillis());
        if (rest < 0) {
            PeepServer.logger.debug("detect timeout at stage '" + stage + "'");
            StatStore.getInstance().addCounter("rpc.timeout.count." + stage, 1);
        }
        return rest;
    }

    public static void writeContent(String id, Channel channel, String content) {
        // so simple.
        HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
        response.setHeader("Content-Length", content.length());
        ChannelBuffer buffer = ChannelBuffers.buffer(content.length());
        buffer.writeBytes(content.getBytes());
        response.setContent(buffer);
        channel.write(response);
        StatStore.getInstance().addCounter(id + ".rpc.out.bytes", content.length());
    }

    public static void writeMessage(String id, Channel channel, Message message) {
        HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
        int size = message.getSerializedSize();
        response.setHeader("Content-Length", size);
        ByteArrayOutputStream os = new ByteArrayOutputStream(size);
        try {
            message.writeTo(os);
            ChannelBuffer buffer = ChannelBuffers.copiedBuffer(os.toByteArray());
            response.setContent(buffer);
            channel.write(response); // write over.
            StatStore.getInstance().addCounter(id + ".rpc.out.bytes", size);
        } catch (Exception e) {
            // just ignore it.
        }
    }

    public void run() {
        switch (code) {
            case kHttpRequest:
                handleHttpRequest();
                break;
            case kSingleRequest:
                handleSingleRequest();
                break;
            case kMultiRequest:
                handleMultiRequest();
                break;
            case kProxyRequestId:
                handleProxyRequestId();
                break;
            case kProxyResponseId:
                handleProxyResponseId();
                break;
            case kProxyRequestInfo:
                handleProxyRequestInfo();
                break;
            case kProxyResponseInfo:
                handleProxyResponseInfo();
                break;
            case kResponse:
                handleResponse();
                break;
            case kHttpResponse:
                handleHttpResponse();
                break;
        }
    }

    public void handleHttpRequest() {
        PeepServer.logger.debug("peeper http request");
        int size = peeperBuffer.readableBytes();
        Object request = null;
        if (size != 0) {
            StatStore.getInstance().addCounter("peeper.rpc.in.bytes", size);
            byte[] bs = new byte[size];
            peeperBuffer.readBytes(bs);
            try {
                request = parser.parse(new InputStreamReader(new ByteArrayInputStream(bs)));
            } catch (Exception e) {
                PeepServer.logger.debug("peeper parse json exception");
                if (configuration.isDebug()) {
                    e.printStackTrace();
                }
                StatStore.getInstance().addCounter("peeper.rpc.in.count.invalid", 1);
                peeperChannel.close();
                return;
            }
        } else {
            QueryStringDecoder decoder = new QueryStringDecoder(query, false);
            JSONObject object = new JSONObject();
            object.putAll(decoder.getParameters());
            request = object;
        }

        if (request instanceof JSONObject) {
            code = Status.kSingleRequest;
        } else if (request instanceof JSONArray) {
            code = Status.kMultiRequest;
        } else {
            PeepServer.logger.debug("peeper invalid json type");
            StatStore.getInstance().addCounter("peeper.rpc.in.count.invalid", 1);
            peeperChannel.close();
            return;
        }

        peeperRequest = request;
        // same thread.
        run();
    }

    public void handleSingleRequest() {
        PeepServer.logger.debug("peeper handle single request");
        // parse json.
        JSONObject object = (JSONObject) peeperRequest;
        boolean ok = true;
        Object account = object.get("account");
        if (account == null || !(account instanceof String)) {
            PeepServer.logger.debug("peeper invalid json at field 'account'");
            ok = false;
        }
        Object reqtype = object.get("reqtype");
        if (reqtype == null || !(reqtype instanceof String)) {
            PeepServer.logger.debug("peeper invalid json at field 'reqtype'");
            ok = false;
        }
        Object device = object.get("device");
        if (device == null || !(device instanceof JSONObject)) {
            PeepServer.logger.debug("peeper invalid json, at field 'device'");
            ok = false;
        }
        if (!ok) {
            raiseException();
            return;
        }
        requestTimeout = kDefaultTimeout;
        Object timeout = object.get("timeout");
        if (timeout != null && timeout instanceof Integer) {
            requestTimeout = ((Integer) timeout).intValue();
        }

        // make protocol buffer object.
        // use multi read.
        int to = detectTimeout("before-makepb-proxy-id");
        if (to < 0) {
            raiseException();
        }
        MessageProtos1.MultiReadRequest.Builder builder = MessageProtos1.MultiReadRequest.newBuilder();
        builder.setTimeout(to);

        JSONObject dev = (JSONObject) peeperRequest;
        ok = false;
        for (String key : kDeviceIdKeys) {
            Object v = dev.get(key);
            if (!(v instanceof String)) {
                continue;
            }
            ok = true;
            MessageProtos1.ReadRequest.Builder sub = MessageProtos1.ReadRequest.newBuilder();
            sub.setRowKey(key + "_" + v);
            sub.setTableName(kDeviceIdMappingTable);
            sub.setColumnFamily(kDeviceIdMappingColumnFamily);
            sub.addQualifiers(kDeviceIdMappingQualifier);
            builder.addRequests(sub);
        }
        if (!ok) {
            PeepServer.logger.debug("peeper invalid json, no field in 'device'");
            raiseException();
            return;
        }
        proxyIdRequest = builder.build();
        code = Status.kProxyRequestId;
        Connector.getInstance().pushRequest(this);
    }

    public void handleMultiRequest() {
        PeepServer.logger.debug("peeper handle multi request");
        JSONArray array = (JSONArray) peeperRequest;
        for (int i = 0; i < array.size(); i++) {
            Object sub = array.get(i);
            if (!(sub instanceof JSONObject)) {
                PeepServer.logger.debug("peeper invalid json array");
                StatStore.getInstance().addCounter("peeper.rpc.in.count.invalid", 1);
                peeperChannel.close();
                return;
            }
        }
        clients = new LinkedList<AsyncClient>();
        refCounter = new AtomicInteger(array.size());
        for (int i = 0; i < array.size(); i++) {
            JSONObject sub = (JSONObject) array.get(i);
            AsyncClient client = new AsyncClient(configuration);
            client.init();
            client.code = Status.kSingleRequest;
            client.subRequest = true;
            client.parent = this;
            client.peeperRequest = sub;
            client.requestTimestamp = requestTimestamp;
            clients.add(client);
            CpuWorkerPool.getInstance().submit(client);
        }
    }

    public void handleProxyRequestId() {
        PeepServer.logger.debug("peeper handle proxy request id");
        int to = detectTimeout("before-request-proxy-id");
        if (to < 0) {
            raiseException();
            return;
        }
        code = Status.kProxyResponseId;
        writeMessage("proxy", proxyChannel, proxyIdRequest);
    }

    public void handleProxyResponseId() {
        PeepServer.logger.debug("peeper handle proxy response id");
        int size = proxyBuffer.readableBytes();
        StatStore.getInstance().addCounter("rpc.in.bytes", size);
        byte[] bs = new byte[size];
        proxyBuffer.readBytes(bs);
        MessageProtos1.MultiReadResponse.Builder builder = MessageProtos1.MultiReadResponse.newBuilder();
        try {
            builder.mergeFrom(bs);
        } catch (InvalidProtocolBufferException e) {
            // just close channel.
            PeepServer.logger.debug("proxy parse message exception");
            StatStore.getInstance().addCounter("proxy.rpc.in.count.invalid", 1);
            raiseException();
            return;
        }
        proxyIdResponse = builder.build();
        // try to fetch umid.
        String umid = null;
        for (MessageProtos1.ReadResponse response : proxyIdResponse.getResponsesList()) {
            ByteString content = response.getKvs(0).getContent();
            if (content.isEmpty()) { // no content.
                continue;
            }
            umid = content.toString();
        }
        if (umid == null) {
            PeepServer.logger.debug("proxy request id, umid == null");
            StatStore.getInstance().addCounter("rpc.null-umid.count", 1);
            raiseException();
            return;
        }
        int timeout = detectTimeout("before-makepb-proxy-info");
        if (timeout < 0) {
            raiseException();
            return;
        }
        // make protocol buffer.
        MessageProtos1.ReadRequest.Builder rdBuilder = MessageProtos1.ReadRequest.newBuilder();
        rdBuilder.setTimeout(timeout);
        rdBuilder.setTableName(kUserInfoTable);
        rdBuilder.setColumnFamily(kUserInfoColumnFamily);
        rdBuilder.setRowKey(umid);
        String xs[] = ((String) ((JSONObject) peeperRequest).get("reqtype")).split(",");
        for (String x : xs) {
            if (reqTypeKeys.contains(x)) {
                rdBuilder.addQualifiers(x);
            }
        }
        proxyInfoRequest = rdBuilder.build();
        code = Status.kProxyRequestInfo;
        run();
    }

    public void handleProxyRequestInfo() {
        PeepServer.logger.debug("peeper handle proxy request info");
        int to = detectTimeout("before-request-proxy-info");
        if (to < 0) {
            raiseException();
            return;
        }
        code = Status.kProxyResponseInfo;
        writeMessage("proxy", proxyChannel, proxyInfoRequest);
    }

    public void handleProxyResponseInfo() {
        PeepServer.logger.debug("peeper handle proxy response info");
        int size = proxyBuffer.readableBytes();
        StatStore.getInstance().addCounter("rpc.in.bytes", size);
        byte[] bs = new byte[size];
        proxyBuffer.readBytes(bs);
        MessageProtos1.ReadResponse.Builder builder = MessageProtos1.ReadResponse.newBuilder();
        try {
            builder.mergeFrom(bs);
        } catch (InvalidProtocolBufferException e) {
            // just close channel.
            PeepServer.logger.debug("proxy parse message exception");
            StatStore.getInstance().addCounter("proxy.rpc.in.count.invalid", 1);
            raiseException();
            return;
        }
        proxyInfoResponse = builder.build();
        JSONObject object = new JSONObject();
        JSONObject origin = (JSONObject) peeperRequest;
        if (origin.containsKey("reqid")) {
            object.put("reqid", origin.get("reqid"));
        }
        JSONObject content = new JSONObject();
        for (MessageProtos1.ReadResponse.KeyValue keyValue : proxyInfoResponse.getKvsList()) {
            content.put(keyValue.getQualifier(), keyValue.getContent().toString());
        }
        object.put("content", content);
        object.put("ecode", "OK");
        peeperResponse = object;
        code = Status.kResponse;
        run();
    }

    public void handleResponse() {
        if (requestStatus == RequestStatus.kOK) {
            StatStore.getInstance().addCounter("peeper.rpc.duration", System.currentTimeMillis() - requestTimestamp);
        }
        if (!subRequest) {
            if (requestStatus != RequestStatus.kOK) {
                peeperChannel.close();
                return;
            }
            code = Status.kHttpResponse;
            run();
        } else {
            int count = parent.refCounter.decrementAndGet();
            if (count == 0) {
                JSONArray result = new JSONArray();
                for (AsyncClient client : parent.clients) {
                    if (client.requestStatus != RequestStatus.kOK) {
                        parent.peeperChannel.close();
                        return;
                    }
                    result.add(client.peeperRequest);
                }
                parent.peeperResponse = result;
                parent.code = Status.kHttpResponse;
                parent.run();
            }
        }
    }

    public void handleHttpResponse() {
        if (peeperResponse instanceof JSONObject) {
            JSONObject object = (JSONObject) peeperResponse;
            writeContent("peeper", peeperChannel, object.toJSONString());
        } else if (peeperResponse instanceof JSONArray) {
            JSONArray array = (JSONArray) peeperResponse;
            writeContent("peeper", peeperChannel, array.toJSONString());
        }
    }

}
