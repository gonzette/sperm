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

import java.io.ByteArrayOutputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created with IntelliJ IDEA.
 * User: dirlt
 * Date: 8/13/13
 * Time: 1:39 PM
 * To change this template use File | Settings | File Templates.
 */
public class AsyncClient implements Runnable {
    // for detecting timeout.
    public static Timer timer = new HashedWheelTimer();
    private static AtomicLong incrementId = new AtomicLong(0);
    private static final String kRequestIdKey = "reqid";
    private static final String kRequestTypeKey = "reqtype";
    private static final String kTimeoutKey = "timeout";
    private static final String kUmengIdKey = "umid";
    private static final String kContentKey = "content";
    private static final String kErrorCodeKey = "ecode";
    private static final List<String> kRequiredKeys = new LinkedList<String>();
    private static final List<String> kDeviceIdKeys = new LinkedList<String>();

//    private static final Set<String> kRequestTypes = new TreeSet<String>();

    static {
        kRequiredKeys.add("account");
        kRequiredKeys.add("reqtype");

        kDeviceIdKeys.add(kUmengIdKey);
        kDeviceIdKeys.add("imei");
        kDeviceIdKeys.add("udid");
        kDeviceIdKeys.add("mac");
        kDeviceIdKeys.add("idfa");
        kDeviceIdKeys.add("openudid");
        kDeviceIdKeys.add("idfv");
        kDeviceIdKeys.add("utdid");

//        kRequestTypes.add("demographic");
//        kRequestTypes.add("geographic");
//        kRequestTypes.add("tcate");
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

    private Configuration configuration;
    private JSONParser parser = new JSONParser();
    public Status code;
    public String path; // url path.
    public String query; // url query;
    public AsyncClient parent;
    public RequestStatus requestStatus = RequestStatus.kOK;
    public String requestMessage;
    public boolean subRequest = false;
    public AtomicInteger refCounter;
    public List<AsyncClient> clients;
    public Long id;

    public Channel veritasChannel;
    public volatile boolean proxyChannelClosed;
    public ChannelBuffer veritasBuffer;
    public Object veritasRequest;
    public Object veritasResponse;
    public ChannelBuffer proxyBuffer;
    public MessageProtos1.MultiReadRequest proxyIdRequest;
    public MessageProtos1.MultiReadRequest.Builder proxyIdRequestBuilder;
    public MessageProtos1.ReadRequest.Builder subProxyIdRequestBuilder;
    public MessageProtos1.MultiReadResponse proxyIdResponse;
    public MessageProtos1.MultiReadResponse.Builder proxyIdResponseBuilder;
    public MessageProtos1.ReadRequest proxyInfoRequest;
    public MessageProtos1.ReadRequest.Builder proxyInfoRequestBuilder;
    public MessageProtos1.ReadResponse proxyInfoResponse;
    public MessageProtos1.ReadResponse.Builder proxyInfoResponseBuilder;
    public String umengId;
    public long requestTimestamp;
    public long requestProxyIdTimestamp;
    public long requestProxyInfoTimestamp;
    public long requestTimeout;

    public AsyncClient(Configuration configuration) {
        this.configuration = configuration;
        preAllocation();
    }

    public void preAllocation() {
        refCounter = new AtomicInteger();
        clients = new LinkedList<AsyncClient>();
        proxyIdRequestBuilder = MessageProtos1.MultiReadRequest.newBuilder();
        subProxyIdRequestBuilder = MessageProtos1.ReadRequest.newBuilder();
        proxyIdResponseBuilder = MessageProtos1.MultiReadResponse.newBuilder();
        proxyInfoRequestBuilder = MessageProtos1.ReadRequest.newBuilder();
        proxyInfoResponseBuilder = MessageProtos1.ReadResponse.newBuilder();
    }

    public void reset() {
        clients.clear();
        proxyIdRequestBuilder.clear();
        subProxyIdRequestBuilder.clear();
        proxyIdResponseBuilder.clear();
        proxyInfoRequestBuilder.clear();
        proxyInfoResponseBuilder.clear();
    }

    public void init(Status code, boolean subRequest) {
        this.code = code;
        this.subRequest = subRequest;
        id = incrementId.getAndIncrement();
        requestStatus = RequestStatus.kOK;
        veritasChannel = null;
        veritasBuffer = null;
        proxyBuffer = null;
        proxyChannelClosed = false;
        umengId = null;
        requestTimeout = configuration.getTimeout();
        reset();
    }

    public void debug(String message) {
        VeritasServer.logger.debug("async id#" + id + ": " + message);
    }

    public void raiseException(String message) {
        debug("raise exception with " + message);
        requestStatus = RequestStatus.kException;
        requestMessage = message;
        code = Status.kResponse;
        run();
    }

    public void raiseException(Exception e) {
        raiseException(e.toString());
    }

    public int detectTimeout(String stage) {
        debug("detect timeout at stage '" + stage + "'");
        int rest = (int) (requestTimeout + requestTimestamp - System.currentTimeMillis());
        debug("rest timeout = " + rest + " at stage '" + stage + "'");
        if (rest < 0) {
            debug("timeout occurs at stage '" + stage + "'");
            StatStore.getInstance().addCounter("rpc.timeout.count." + stage, 1);
        }
        return rest;
    }

    public static void writeContent(Channel channel, String content) {
        // so simple.
        byte[] bs = content.getBytes();
        int size = bs.length;
//        VeritasServer.logger.debug("write content...");
        HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
//        VeritasServer.logger.debug("write content...set header");
        response.setHeader("Content-Length", size);
//        VeritasServer.logger.debug("write content...write bytes");
        ChannelBuffer buffer = ChannelBuffers.buffer(size);
        buffer.writeBytes(bs);
//        VeritasServer.logger.debug("write content...set content");
        response.setContent(buffer);
//        VeritasServer.logger.debug("write content...write channel");
        channel.write(response);
//        VeritasServer.logger.debug("write content...write stat store");
        StatStore.getInstance().addMetric(StatStore.MetricFieldName.kRPCOutBytes, size);
    }

    public static void writeMessage(String path, Channel channel, Message message, StatStore.MetricFieldName name) {
        HttpRequest httpRequest = new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST, path);
        int size = message.getSerializedSize();
        httpRequest.setHeader("Content-Length", size);
        ByteArrayOutputStream os = new ByteArrayOutputStream(size);
        try {
            message.writeTo(os);
            ChannelBuffer buffer = ChannelBuffers.copiedBuffer(os.toByteArray());
            httpRequest.setContent(buffer);
            channel.write(httpRequest); // write over.
            StatStore.getInstance().addMetric(name, size);
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
        debug("veritas handle http request");
        StatStore.getInstance().updateClock(StatStore.ClockFieldName.kCPUQueue, System.currentTimeMillis() - requestTimestamp);
        int size = veritasBuffer.readableBytes();
        Object request = null;
        if (size != 0) {
            StatStore.getInstance().addMetric(StatStore.MetricFieldName.kRPCInBytes, size);
            byte[] bs = new byte[size];
            veritasBuffer.readBytes(bs);
            String json = new String(bs);
            try {
                request = parser.parse(json);
            } catch (Exception e) {
                debug("veritas parse json exception");
                if (configuration.isDebug()) {
                    e.printStackTrace();
                    System.err.println("JSON String = " + json);
                }
                StatStore.getInstance().addCounter("veritas.rpc.in.count.invalid", 1);
                raiseException(e);
                return;
            }
        } else if (!query.isEmpty()) {
            debug("handle get request");
            QueryStringDecoder decoder = new QueryStringDecoder(query, false);
            JSONObject object = new JSONObject();
            for (String key : decoder.getParameters().keySet()) {
                object.put(key, decoder.getParameters().get(key).get(0));
            }
            StatStore.getInstance().addMetric(StatStore.MetricFieldName.kRPCInBytes, query.length());
            request = object;
        } else {
            raiseException("unknown request type");
            return;
        }

        if (request instanceof JSONObject) {
            code = Status.kSingleRequest;
            StatStore.getInstance().addMetric(StatStore.MetricFieldName.kRPCSingleRequestCount, 1);
            StatStore.getInstance().addMetric(StatStore.MetricFieldName.kRequestCount, 1);
        } else if (request instanceof JSONArray) {
            code = Status.kMultiRequest;
            StatStore.getInstance().addMetric(StatStore.MetricFieldName.kRPCMultiRequestCount, 1);
        } else {
            debug("veritas invalid json type");
            StatStore.getInstance().addCounter("veritas.rpc.in.count.invalid", 1);
            raiseException("invalid json");
            return;
        }

        veritasRequest = request;
        // same thread.
        run();
    }

    public void handleSingleRequest() {
        debug("veritas handle single request");
        // parse json.
        JSONObject object = (JSONObject) veritasRequest;
        boolean ok = true;
        for (String k : kRequiredKeys) {
            Object v = object.get(k);
            if (v == null) {
                debug("veritas invalid json at field '" + k + "'");
                ok = false;
                break;
            }
        }
        if (!ok) {
            raiseException("invalid json without require fields");
            return;
        }
        ok = false;
        for (String k : kDeviceIdKeys) {
            Object v = object.get(k);
            if (!(v instanceof String)) {
                continue;
            }
            ok = true;
        }
        if (!ok) {
            raiseException("invalid json without any device id");
            return;
        }
        // checkout timeout
        requestTimeout = configuration.getTimeout();
        Object timeout = object.get(kTimeoutKey);
        if (timeout != null) {
            if (timeout instanceof Integer) {
                requestTimeout = (Integer) timeout;
            } else if (timeout instanceof String) {
                try {
                    requestTimeout = Integer.valueOf((String) timeout);
                } catch (Exception e) {
                    timeout = configuration.getTimeout();
                }
            }
        }
        debug("request timeout = " + requestTimeout);
        // control flow.
        code = Status.kProxyRequestId;
        run();
    }

    public void handleMultiRequest() {
        debug("veritas handle multi request");
        JSONArray array = (JSONArray) veritasRequest;
        for (int i = 0; i < array.size(); i++) {
            Object sub = array.get(i);
            if (!(sub instanceof JSONObject)) {
                debug("veritas invalid json array");
                StatStore.getInstance().addCounter("veritas.rpc.in.count.invalid", 1);
                raiseException("invalid json");
                return;
            }
        }
        refCounter.set(array.size());
        StatStore.getInstance().addMetric(StatStore.MetricFieldName.kRequestCount, array.size());
        for (int i = 0; i < array.size(); i++) {
            JSONObject sub = (JSONObject) array.get(i);
            AsyncClient client = new AsyncClient(configuration);
            client.init(Status.kSingleRequest, true);
            client.parent = this;
            client.veritasRequest = sub;
            client.requestTimestamp = requestTimestamp;
            clients.add(client);
            CpuWorkerPool.getInstance().submit(client);
        }
    }

    public void handleProxyRequestId() {
        debug("veritas handle proxy request id");
        // detect timeout.
        int to = detectTimeout("before-request-proxy-id");
        if (to < 0) {
            raiseException("timeout before request proxy id");
            return;
        }
        // build request.
        proxyIdRequestBuilder.setTimeout(to);
        JSONObject object = (JSONObject) veritasRequest;
        // if has umid field, we don't need to request id.
        if (object.containsKey(kUmengIdKey)) {
            debug("veritas proxy request id shortcut");
            umengId = (String) object.get(kUmengIdKey);
            code = Status.kProxyRequestInfo;
            run();
            return;
        }
        for (String key : kDeviceIdKeys) {
            Object v = object.get(key);
            subProxyIdRequestBuilder.setRowKey(key + "_" + v);
            subProxyIdRequestBuilder.setTableName(configuration.getDeviceIdMappingTable());
            subProxyIdRequestBuilder.setColumnFamily(configuration.getDeviceIdMappingColumnFamily());
            subProxyIdRequestBuilder.addQualifiers(kUmengIdKey);
            proxyIdRequestBuilder.addRequests(subProxyIdRequestBuilder.build());
        }
        proxyIdRequest = proxyIdRequestBuilder.build();
        // write it out.
        code = Status.kProxyResponseId;
        requestProxyIdTimestamp = System.currentTimeMillis();
        ProxyHandler handler = ProxyConnector.getInstance().acquireConnection();
        if (handler == null) {
            raiseException("proxy connector id acquire connection failed");
            return;
        }
        handler.client = this;
        // write timeout exception.
        debug("proxy request id add wto handler");
        handler.context.getPipeline().addBefore(handler.context.getName(), "wto_handler",
                new WriteTimeoutHandler(timer, to, TimeUnit.MILLISECONDS));
        StatStore.getInstance().addMetric(StatStore.MetricFieldName.kProxyIdRequestCount, 1);
        writeMessage("/multi-read", handler.channel, proxyIdRequest, StatStore.MetricFieldName.kProxyIdRequestBytes);
        return;
    }

    public void handleProxyResponseId() {
        debug("veritas handle proxy response id");
        // check proxy channel closed.
        if (proxyChannelClosed) {
            raiseException("proxy id channel closed");
            return;
        }
        StatStore.getInstance().updateClock(StatStore.ClockFieldName.kProxyId, System.currentTimeMillis() - requestProxyIdTimestamp);
        // parse response.
        int size = proxyBuffer.readableBytes();
        StatStore.getInstance().addMetric(StatStore.MetricFieldName.kProxyIdResponseBytes, size);
        byte[] bs = new byte[size];
        proxyBuffer.readBytes(bs);
        try {
            proxyIdResponseBuilder.mergeFrom(bs);
        } catch (InvalidProtocolBufferException e) {
            debug("proxy id parse message exception");
            StatStore.getInstance().addCounter("proxy.rpc.in.count.invalid", 1);
            raiseException(e);
            return;
        }
        proxyIdResponse = proxyIdResponseBuilder.build();
        // try to fetch umid and error message.
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
            debug("proxy request id but with error : " + emesg);
            StatStore.getInstance().addCounter("proxy.rpc.in.count.invalid", 1);
            raiseException(emesg);
            return;
        }
        if (umid == null) {
            debug("proxy request id, umid == null");
            StatStore.getInstance().addCounter("rpc.null-umid.count", 1);
            raiseException("null umid");
            return;
        }
        umengId = umid;
        code = Status.kProxyRequestInfo;
        run();
    }

    public void handleProxyRequestInfo() {
        // proxy request info.
        debug("veritas handle proxy request info");
        int to = detectTimeout("before-request-proxy-info");
        if (to < 0) {
            raiseException("timeout before request proxy info");
            return;
        }
        // make protocol buffer.
        proxyInfoRequestBuilder.setTimeout(to);
        proxyInfoRequestBuilder.setTableName(configuration.getUserInfoTable());
        proxyInfoRequestBuilder.setColumnFamily(configuration.getUserInfoColumnFamily());
        proxyInfoRequestBuilder.setRowKey(umengId);
        String xs[] = ((String) ((JSONObject) veritasRequest).get(kRequestTypeKey)).split(",");
        for (String x : xs) {
            if (x.isEmpty()) {
                continue;
            }
//            if (kRequestTypes.contains(x)) {
//                rdBuilder.addQualifiers(x);
//            }
            proxyInfoRequestBuilder.addQualifiers(x);
        }
        proxyInfoRequest = proxyInfoRequestBuilder.build();
//        System.out.println(proxyInfoRequest.toString());
        // write it out.
        code = Status.kProxyResponseInfo;
        requestProxyInfoTimestamp = System.currentTimeMillis();
        ProxyHandler handler = ProxyConnector.getInstance().acquireConnection();
        if (handler == null) {
            raiseException("proxy connector info acquire connection failed");
            return;
        }
        handler.client = this;
        // write timeout exception.
        debug("proxy request info add wto handler");
        handler.context.getPipeline().addBefore(handler.context.getName(), "wto_handler",
                new WriteTimeoutHandler(timer, to, TimeUnit.MILLISECONDS));
        StatStore.getInstance().addMetric(StatStore.MetricFieldName.kProxyInfoRequestCount, 1);
        writeMessage("/read", handler.channel, proxyInfoRequest, StatStore.MetricFieldName.kProxyInfoRequestBytes);
        return;
    }

    public void handleProxyResponseInfo() {
        debug("veritas handle proxy response info");
        if (proxyChannelClosed) {
            raiseException("proxy info channel closed");
            return;
        }
        StatStore.getInstance().updateClock(StatStore.ClockFieldName.kProxyInfo, System.currentTimeMillis() - requestProxyInfoTimestamp);
        int size = proxyBuffer.readableBytes();
        StatStore.getInstance().addMetric(StatStore.MetricFieldName.kProxyInfoResponseBytes, size);
        byte[] bs = new byte[size];
        proxyBuffer.readBytes(bs);
        try {
            proxyInfoResponseBuilder.mergeFrom(bs);
        } catch (InvalidProtocolBufferException e) {
            debug("proxy info parse message exception");
            StatStore.getInstance().addCounter("proxy.rpc.in.count.invalid", 1);
            raiseException(e);
            return;
        }
        proxyInfoResponse = proxyInfoResponseBuilder.build();
//        System.out.println(proxyInfoResponse.toString());
        debug("veritas proxy response info : new JSONObject");
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
                String s = keyValue.getContent().toStringUtf8().trim();
                // maybe not json.
                if (s.startsWith("{") || s.startsWith("[")) {
                    try {
                        Object v = parser.parse(s);
                        content.put(keyValue.getQualifier(), v);
                    } catch (ParseException e) {
                        debug("proxy hbase qualifier " + keyValue.getQualifier() + ", content = " + keyValue.getContent().toStringUtf8());
                        if (!configuration.isResponseWithBestEffort()) {
                            object.put(kErrorCodeKey, "proxy hbase content = " + e.toString());
                            ok = false;
                            break;
                        }
                    }
                } else {
                    content.put(keyValue.getQualifier(), s);
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
        debug("veritas handle response");
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
                    result.add(client.veritasResponse);
                }
                parent.veritasResponse = result;
                parent.code = Status.kHttpResponse;
                parent.run();
            }
        }
    }

    public void handleHttpResponse() {
        debug("veritas handle http response");
        StatStore.getInstance().updateClock(StatStore.ClockFieldName.kRequest, System.currentTimeMillis() - requestTimestamp);
        if (veritasResponse instanceof JSONObject) {
            JSONObject object = (JSONObject) veritasResponse;
            writeContent(veritasChannel, object.toJSONString());
        } else if (veritasResponse instanceof JSONArray) {
            JSONArray array = (JSONArray) veritasResponse;
            writeContent(veritasChannel, array.toJSONString());
        }
        debug("veritas set readable true");
        StatStore.getInstance().addMetric(StatStore.MetricFieldName.kRPCResponseCount, 1);
        veritasChannel.setReadable(true);
    }
}
