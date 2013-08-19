package com.dirlt.java.veritas;


import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.handler.codec.http.*;
import org.jboss.netty.handler.timeout.WriteTimeoutHandler;
import org.jboss.netty.util.HashedWheelTimer;
import org.jboss.netty.util.Timer;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;
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
    public static Timer timer = new HashedWheelTimer();
    private Configuration configuration;

    private static final long kDefaultTimeout = 100 * 1000; // 100s.(that's long enough).
    private static final String kDeviceIdMappingTable = "device_id_mapping";
    private static final String kDeviceIdMappingColumnFamily = "mapping";
    private static final String kDeviceIdMappingQualifier = "umid";
    private static final String kUserInfoTable = "user_info";
    private static final String kUserInfoColumnFamily = "info";
    private static final String kRequestIdKey = "reqid";
    private static final String kRequestTypeKey = "reqtype";
    private static final String kTimeoutKey = "timeout";
    private static final String kUmengIdKey = "umid";
    private static final String kContentKey = "content";
    private static final String kErrorCodeKey = "ecode";
    private static final String kRequiredKeys[] = "account,reqtype".split(",");
    private static final String kDeviceIdKeys[] = "imei,udid,mac,idfa,openudid,idfv,utdid".split(",");
    private static final Set<String> kRequestTypes = new TreeSet<String>();

    static {
        kRequestTypes.add("demographic");
        kRequestTypes.add("geographic");
        kRequestTypes.add("tcate");
    }

    enum Status {
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
    }

    public Status code;
    public String path; // url path.
    public String query; // url query;
    public AsyncClient parent;
    public RequestStatus requestStatus = RequestStatus.kOK;
    public String requestMessage;
    public boolean subRequest = false;
    public AtomicInteger refCounter;
    public List<AsyncClient> clients;

    public Channel veritasChannel;
    public volatile boolean proxyChannelClosed;
    public ChannelBuffer veritasBuffer;
    public Object veritasRequest;
    public Object veritasResponse;
    public ChannelBuffer proxyBuffer;
    public MessageProtos1.MultiReadRequest proxyIdRequest;
    public MessageProtos1.MultiReadResponse proxyIdResponse;
    public String umengId;
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
        veritasChannel = null;
        proxyChannelClosed = false;
        umengId = null;
    }

    public void raiseException(String message) {
        requestStatus = RequestStatus.kException;
        requestMessage = message;
        code = Status.kResponse;
        run();
    }

    public void raiseException(Exception e) {
        raiseException(e.toString());
    }

    public int detectTimeout(String stage) {
        int rest = (int) (requestTimeout + requestTimestamp - System.currentTimeMillis());
        if (rest < 0) {
            VeritasServer.logger.debug("detect timeout at stage '" + stage + "'");
            StatStore.getInstance().addCounter("rpc.timeout.count." + stage, 1);
        }
        return rest;
    }

    public static void writeContent(Channel channel, String content) {
        // so simple.
        HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
        response.setHeader("Content-Length", content.length());
        ChannelBuffer buffer = ChannelBuffers.buffer(content.length());
        buffer.writeBytes(content.getBytes());
        response.setContent(buffer);
        channel.write(response);
        StatStore.getInstance().addCounter("veritas.rpc.out.bytes", content.length());
    }

    public static void writeMessage(String path, Channel channel, Message message) {
        HttpRequest httpRequest = new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST, path);
        int size = message.getSerializedSize();
        httpRequest.setHeader("Content-Length", size);
        ByteArrayOutputStream os = new ByteArrayOutputStream(size);
        try {
            message.writeTo(os);
            ChannelBuffer buffer = ChannelBuffers.copiedBuffer(os.toByteArray());
            httpRequest.setContent(buffer);
            channel.write(httpRequest); // write over.
            StatStore.getInstance().addCounter("proxy.rpc.out.bytes", size);
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
        VeritasServer.logger.debug("veritas http request");
        int size = veritasBuffer.readableBytes();
        Object request = null;
        if (size != 0) {
            StatStore.getInstance().addCounter("veritas.rpc.in.bytes", size);
            byte[] bs = new byte[size];
            veritasBuffer.readBytes(bs);
            try {
                request = parser.parse(new InputStreamReader(new ByteArrayInputStream(bs)));
            } catch (Exception e) {
                VeritasServer.logger.debug("veritas parse json exception");
                if (configuration.isDebug()) {
                    e.printStackTrace();
                }
                StatStore.getInstance().addCounter("veritas.rpc.in.count.invalid", 1);
                raiseException(e);
                return;
            }
        } else if (!query.isEmpty()) {
            VeritasServer.logger.debug("handle get request");
            QueryStringDecoder decoder = new QueryStringDecoder(query, false);
            JSONObject object = new JSONObject();
            for (String key : decoder.getParameters().keySet()) {
                object.put(key, decoder.getParameters().get(key).get(0));
            }
            request = object;
        } else {
            raiseException("unknown request type");
            return;
        }

        if (request instanceof JSONObject) {
            code = Status.kSingleRequest;
        } else if (request instanceof JSONArray) {
            code = Status.kMultiRequest;
        } else {
            VeritasServer.logger.debug("veritas invalid json type");
            StatStore.getInstance().addCounter("veritas.rpc.in.count.invalid", 1);
            raiseException("invalid json");
            return;
        }

        veritasRequest = request;
        // same thread.
        run();
    }

    public void handleSingleRequest() {
        VeritasServer.logger.debug("veritas handle single request");
        // parse json.
        JSONObject object = (JSONObject) veritasRequest;
        boolean ok = true;
        for (String k : kRequiredKeys) {
            Object v = object.get(k);
            if (v == null) {
                VeritasServer.logger.debug("veritas invalid json at field '" + k + "'");
                ok = false;
                break;
            }
        }
        if (!ok) {
            raiseException("invalid json without require fields");
            return;
        }
        requestTimeout = kDefaultTimeout;
        Object timeout = object.get(kTimeoutKey);
        if (timeout != null && timeout instanceof Integer) {
            requestTimeout = ((Integer) timeout).intValue();
        }

        VeritasServer.logger.debug("veritas make pb proxy id");
        // make protocol buffer object.
        // use multi read.
        int to = detectTimeout("before-makepb-proxy-id");
        if (to < 0) {
            raiseException("timeout before makepb proxy id");
            return;
        }
        MessageProtos1.MultiReadRequest.Builder builder = MessageProtos1.MultiReadRequest.newBuilder();
        builder.setTimeout(to);

        ok = false;
        for (String key : kDeviceIdKeys) {
            Object v = object.get(key);
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
            VeritasServer.logger.debug("veritas invalid json, no field in 'device'");
            raiseException("invalid json without any field in 'device'");
            return;
        }
        proxyIdRequest = builder.build();
        code = Status.kProxyRequestId;
        run();
    }

    public void handleMultiRequest() {
        VeritasServer.logger.debug("veritas handle multi request");
        JSONArray array = (JSONArray) veritasRequest;
        for (int i = 0; i < array.size(); i++) {
            Object sub = array.get(i);
            if (!(sub instanceof JSONObject)) {
                VeritasServer.logger.debug("veritas invalid json array");
                StatStore.getInstance().addCounter("veritas.rpc.in.count.invalid", 1);
                raiseException("invalid json");
                return;
            }
        }
        if (clients == null) {
            clients = new LinkedList<AsyncClient>();
        } else {
            clients.clear();
        }
        if (refCounter == null) {
            refCounter = new AtomicInteger(array.size());
        } else {
            refCounter.set(array.size());
        }
        for (int i = 0; i < array.size(); i++) {
            JSONObject sub = (JSONObject) array.get(i);
            AsyncClient client = new AsyncClient(configuration);
            client.init();
            client.code = Status.kSingleRequest;
            client.subRequest = true;
            client.parent = this;
            client.veritasRequest = sub;
            client.requestTimestamp = requestTimestamp;
            clients.add(client);
            CpuWorkerPool.getInstance().submit(client);
        }
    }

    public void handleProxyRequestId() {
        VeritasServer.logger.debug("veritas handle proxy request id");
        int to = detectTimeout("before-request-proxy-id");
        if (to < 0) {
            raiseException("timeout before request proxy id");
            return;
        }
        code = Status.kProxyResponseId;
        ProxyHandler handler = ProxyConnector.getInstance().popConnection();
        handler.client = this;
        // write timeout exception.
        VeritasServer.logger.debug("proxy request id add wto handler");
        handler.context.getPipeline().addBefore(handler.context.getName(), "wto_handler",
                new WriteTimeoutHandler(timer, configuration.getProxyWriteTimeout(), TimeUnit.MILLISECONDS));
        writeMessage("/multi-read", handler.channel, proxyIdRequest);
        return;
    }

    public void handleProxyResponseId() {
        VeritasServer.logger.debug("veritas handle proxy response id");
        if (proxyChannelClosed) {
            raiseException("proxy id channel closed");
            return;
        }
        int size = proxyBuffer.readableBytes();
        StatStore.getInstance().addCounter("proxy.rpc.in.bytes", size);
        byte[] bs = new byte[size];
        proxyBuffer.readBytes(bs);
        MessageProtos1.MultiReadResponse.Builder builder = MessageProtos1.MultiReadResponse.newBuilder();
        try {
            builder.mergeFrom(bs);
        } catch (InvalidProtocolBufferException e) {
            VeritasServer.logger.debug("proxy id parse message exception");
            StatStore.getInstance().addCounter("proxy.rpc.in.count.invalid", 1);
            raiseException(e);
            return;
        }
        proxyIdResponse = builder.build();
        // try to fetch umid.
        String umid = null;
        String emesg = null;
        for (MessageProtos1.ReadResponse response : proxyIdResponse.getResponsesList()) {
            if (response.getError()) {
                emesg = response.getMessage();
                break;
            }
            ByteString content = response.getKvs(0).getContent();
            if (content.isEmpty()) { // no content.
                continue;
            }
            umid = content.toStringUtf8();
        }
        if (emesg != null) {
            VeritasServer.logger.debug("proxy request id but with error : " + emesg);
            StatStore.getInstance().addCounter("proxy.rpc.in.count.invalid", 1);
            raiseException(emesg);
            return;
        }
        if (umid == null) {
            VeritasServer.logger.debug("proxy request id, umid == null");
            StatStore.getInstance().addCounter("rpc.null-umid.count", 1);
            raiseException("null umid");
            return;
        }
        umengId = umid;
        int timeout = detectTimeout("before-makepb-proxy-info");
        if (timeout < 0) {
            raiseException("timeout before makepb proxy info");
            return;
        }
        // make protocol buffer.
        MessageProtos1.ReadRequest.Builder rdBuilder = MessageProtos1.ReadRequest.newBuilder();
        rdBuilder.setTimeout(timeout);
        rdBuilder.setTableName(kUserInfoTable);
        rdBuilder.setColumnFamily(kUserInfoColumnFamily);
        rdBuilder.setRowKey(umid);
        String xs[] = ((String) ((JSONObject) veritasRequest).get(kRequestTypeKey)).split(",");
        for (String x : xs) {
            if (kRequestTypes.contains(x)) {
                rdBuilder.addQualifiers(x);
            }
        }
        proxyInfoRequest = rdBuilder.build();
        code = Status.kProxyRequestInfo;
        run();
    }

    public void handleProxyRequestInfo() {
        // proxy request info.
        VeritasServer.logger.debug("veritas handle proxy request info");
        int to = detectTimeout("before-request-proxy-info");
        if (to < 0) {
            raiseException("timeout before request proxy info");
            return;
        }
        code = Status.kProxyResponseInfo;
        ProxyHandler handler = ProxyConnector.getInstance().popConnection();
        handler.client = this;
        // write timeout exception.
        VeritasServer.logger.debug("proxy request info add wto handler");
        handler.context.getPipeline().addBefore(handler.context.getName(), "wto_handler",
                new WriteTimeoutHandler(timer, configuration.getProxyWriteTimeout(), TimeUnit.MILLISECONDS));
        writeMessage("/read", handler.channel, proxyInfoRequest);
        return;
    }

    public void handleProxyResponseInfo() {
        VeritasServer.logger.debug("veritas handle proxy response info");
        if (proxyChannelClosed) {
            raiseException("proxy info channel closed");
            return;
        }
        int size = proxyBuffer.readableBytes();
        StatStore.getInstance().addCounter("proxy.rpc.in.bytes", size);
        byte[] bs = new byte[size];
        proxyBuffer.readBytes(bs);
        MessageProtos1.ReadResponse.Builder builder = MessageProtos1.ReadResponse.newBuilder();
        try {
            builder.mergeFrom(bs);
        } catch (InvalidProtocolBufferException e) {
            VeritasServer.logger.debug("proxy info parse message exception");
            StatStore.getInstance().addCounter("proxy.rpc.in.count.invalid", 1);
            raiseException(e);
            return;
        }
        proxyInfoResponse = builder.build();
        JSONObject object = new JSONObject();
        JSONObject origin = (JSONObject) veritasRequest;
        if (origin.containsKey(kRequestIdKey)) {
            object.put(kRequestIdKey, origin.get(kRequestIdKey));
        }
        if (!proxyInfoResponse.getError()) {
            JSONObject content = new JSONObject();
            boolean ok = true;
            for (MessageProtos1.ReadResponse.KeyValue keyValue : proxyInfoResponse.getKvsList()) {
                if (keyValue.getContent().isEmpty()) {
                    continue;
                }
                try {
                    Object v = parser.parse(keyValue.getContent().toStringUtf8());
                    content.put(keyValue.getQualifier(), v);
                } catch (ParseException e) {
                    VeritasServer.logger.debug("proxy hbase qualifier " + keyValue.getQualifier() + ", content = " + keyValue.getContent().toStringUtf8());
                    object.put(kErrorCodeKey, "proxy hbase content = " + e.toString());
                    ok = false;
                    break;
                }
            }
            if (ok) {
                object.put(kContentKey, content);
                object.put(kErrorCodeKey, "OK");
            }
        } else {
            object.put(kErrorCodeKey, proxyInfoResponse.getMessage());
        }
        object.put(kUmengIdKey, umengId);
        veritasResponse = object;
        code = Status.kResponse;
        run();
    }

    public static void handleExceptionResponse(AsyncClient client) {
        JSONObject object = new JSONObject();
        JSONObject origin = (JSONObject) client.veritasRequest;
        if (origin.containsKey(kRequestIdKey)) {
            object.put(kRequestIdKey, origin.get(kRequestIdKey));
        }
        object.put(kErrorCodeKey, client.requestMessage);
        if (client.umengId != null) {
            object.put(kUmengIdKey, client.umengId);
        }
        client.veritasResponse = object;
    }

    public void handleResponse() {
        VeritasServer.logger.debug("veritas handle response");
        if (requestStatus == RequestStatus.kOK) {
            StatStore.getInstance().addCounter("veritas.rpc.duration", System.currentTimeMillis() - requestTimestamp);
        }
        if (!subRequest) {
            if (requestStatus != RequestStatus.kOK) {
                handleExceptionResponse(this);
            }
            code = Status.kHttpResponse;
            run();
        } else {
            int count = parent.refCounter.decrementAndGet();
            if (count == 0) {
                JSONArray result = new JSONArray();
                for (AsyncClient client : parent.clients) {
                    if (client.requestStatus != RequestStatus.kOK) {
                        handleExceptionResponse(client);
                    }
                    result.add(client.veritasRequest);
                }
                parent.veritasResponse = result;
                parent.code = Status.kHttpResponse;
                parent.run();
            }
        }
    }

    public void handleHttpResponse() {
        VeritasServer.logger.debug("veritas handle http response");
        if (veritasResponse instanceof JSONObject) {
            JSONObject object = (JSONObject) veritasResponse;
            writeContent(veritasChannel, object.toJSONString());
        } else if (veritasResponse instanceof JSONArray) {
            JSONArray array = (JSONArray) veritasResponse;
            writeContent(veritasChannel, array.toJSONString());
        }
        veritasChannel.setReadable(true);
    }
}
