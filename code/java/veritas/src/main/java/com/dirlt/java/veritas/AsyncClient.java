package com.dirlt.java.veritas;


import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.handler.codec.http.*;
import org.jboss.netty.handler.timeout.ReadTimeoutHandler;
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
    public int retryProxyId;
    public long requestProxyInfoTimestamp;
    public int retryProxyInfo;
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
        retryProxyId = 0;
        retryProxyInfo = 0;
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

    public static void writeContent(Channel channel, String content) {
        // so simple.
        byte[] bs = content.getBytes();
        int size = bs.length;
        HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
        response.setHeader("Content-Length", size);
        ChannelBuffer buffer = ChannelBuffers.buffer(size);
        buffer.writeBytes(bs);
        response.setContent(buffer);
        channel.write(response);
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
            if (v == null || !(v instanceof String)) {
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
                    requestTimeout = configuration.getTimeout();
                }
            }
        }
        debug("request timeout = " + requestTimeout);
        // control flow.
        code = Status.kProxyRequestId;
        // if request contain 'umid' already,
        JSONObject obj = (JSONObject) veritasRequest;
        // if has umid field, we don't need to request id.
        if (obj.containsKey(kUmengIdKey)) {
            debug("veritas proxy request id shortcut");
            umengId = (String) obj.get(kUmengIdKey);
            code = Status.kProxyRequestInfo;
        }
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

    public long calcRequestTimeout(int retryTime) {
        final long kCPUReservedTimeSlice = 10; // allocate 10ms for CPU.
        long timeout = requestTimeout - (System.currentTimeMillis() - requestTimestamp);
        return timeout - kCPUReservedTimeSlice;
    }

    public void handleProxyRequestId() {
        debug("veritas handle proxy request id");
        if (retryProxyId == 0) {
            requestProxyIdTimestamp = System.currentTimeMillis();
        }
        // check timeout.
        long timeout = calcRequestTimeout(retryProxyId);
        if (timeout < 0) {
            raiseException("detect timeout in request proxy id");
            return;
        }
        // build request.
        proxyIdRequestBuilder.setTimeout((int) timeout);
        JSONObject object = (JSONObject) veritasRequest;
        for (String key : kDeviceIdKeys) {
            Object v = object.get(key);
            if (v == null) {
                continue;
            }
            subProxyIdRequestBuilder.setRowKey(key + "_" + v);
            subProxyIdRequestBuilder.setTableName(configuration.getDeviceIdMappingTable());
            subProxyIdRequestBuilder.setColumnFamily(configuration.getDeviceIdMappingColumnFamily());
            subProxyIdRequestBuilder.addQualifiers(kUmengIdKey);
            proxyIdRequestBuilder.addRequests(subProxyIdRequestBuilder.build());
        }
        proxyIdRequest = proxyIdRequestBuilder.build();
        // obtain connection.
        ProxyHandler handler = null;
        while (true) {
            handler = ProxyConnector.getInstance().acquireConnection();
            if (handler == null) {
                if (calcRequestTimeout(retryProxyId) < 0) {
                    raiseException("detect timeout in request proxy id");
                    return;
                }
            } else {
                break;
            }
        }
        // write it out.
        code = Status.kProxyResponseId;
        handler.client = this;
        StatStore.getInstance().addMetric(StatStore.MetricFieldName.kProxyIdRequestCount, 1);
        writeMessage("/multi-read", handler.channel, proxyIdRequest, StatStore.MetricFieldName.kProxyIdRequestBytes);
        // add read exception.
        handler.channel.getPipeline().addBefore(handler.context.getName(), "rto_handler",
                new ReadTimeoutHandler(AsyncClient.timer, timeout, TimeUnit.MILLISECONDS));
        handler.channel.setReadable(true);
        return;
    }

    public void handleProxyResponseId() {
        debug("veritas handle proxy response id");
        // check proxy channel closed.
        if (proxyChannelClosed) {
            proxyChannelClosed = false;
            retryProxyId += 1;
            code = Status.kProxyRequestId;
            run();
            return;
        }
        // parse response.
        int size = proxyBuffer.readableBytes();
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
        proxyIdRequestBuilder.clear();
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
        // update metric.
        StatStore.getInstance().updateClock(StatStore.ClockFieldName.kProxyId, System.currentTimeMillis() - requestProxyIdTimestamp);
        StatStore.getInstance().addMetric(StatStore.MetricFieldName.kProxyIdResponseBytes, size);
        // control flo.
        code = Status.kProxyRequestInfo;
        run();
    }

    public void handleProxyRequestInfo() {
        debug("veritas handle proxy request info");
        if (retryProxyInfo == 0) {
            requestProxyInfoTimestamp = System.currentTimeMillis();
        }
        // check timeout.
        long timeout = calcRequestTimeout(retryProxyInfo);
        if (timeout < 0) {
            raiseException("detect timeout in request proxy info");
            return;
        }
        // build request.
        proxyInfoRequestBuilder.setTimeout((int) timeout);
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
        // obtain connection.
        ProxyHandler handler = null;
        while (true) {
            handler = ProxyConnector.getInstance().acquireConnection();
            if (handler == null) {
                if (calcRequestTimeout(retryProxyInfo) < 0) {
                    raiseException("detect timeout in request proxy info");
                    return;
                }
            } else {
                break;
            }
        }
        // write it out.
        code = Status.kProxyResponseInfo;
        handler.client = this;
        StatStore.getInstance().addMetric(StatStore.MetricFieldName.kProxyInfoRequestCount, 1);
        writeMessage("/read", handler.channel, proxyInfoRequest, StatStore.MetricFieldName.kProxyInfoRequestBytes);
        // add read exception.
        handler.channel.getPipeline().addBefore(handler.context.getName(), "rto_handler",
                new ReadTimeoutHandler(AsyncClient.timer, timeout, TimeUnit.MILLISECONDS));
        handler.channel.setReadable(true);
        return;
    }

    public void handleProxyResponseInfo() {
        debug("veritas handle proxy response info");
        if (proxyChannelClosed) {
            proxyChannelClosed = false;
            retryProxyInfo += 1;
            code = Status.kProxyRequestInfo;
            run();
            return;
        }
        // parse proxy info response.
        int size = proxyBuffer.readableBytes();
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
        proxyInfoResponseBuilder.clear();
        // build response.
        JSONObject object = new JSONObject();
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
        // fill request id.
        JSONObject origin = (JSONObject) veritasRequest;
        if (origin.containsKey(kRequestIdKey)) {
            object.put(kRequestIdKey, origin.get(kRequestIdKey));
        }
        veritasResponse = object;
        // update metric.
        StatStore.getInstance().addMetric(StatStore.MetricFieldName.kProxyInfoResponseBytes, size);
        StatStore.getInstance().updateClock(StatStore.ClockFieldName.kProxyInfo, System.currentTimeMillis() - requestProxyInfoTimestamp);
        // control flow.
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
        // assemble response.
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
        // write response.
        if (veritasResponse instanceof JSONObject) {
            JSONObject object = (JSONObject) veritasResponse;
            writeContent(veritasChannel, object.toJSONString());
        } else if (veritasResponse instanceof JSONArray) {
            JSONArray array = (JSONArray) veritasResponse;
            writeContent(veritasChannel, array.toJSONString());
        }
        // update metric.
        StatStore.getInstance().addMetric(StatStore.MetricFieldName.kRPCResponseCount, 1);
        StatStore.getInstance().updateClock(StatStore.ClockFieldName.kRequest, System.currentTimeMillis() - requestTimestamp);
        // open channel for reading.
        debug("veritas set readable true");
        veritasChannel.setReadable(true);
    }
}
