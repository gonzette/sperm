package com.dirlt.java.FastHBaseRest;

import com.dirlt.java.FastHbaseRest.MessageProtos1;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.stumbleupon.async.Deferred;
import com.stumbleupon.async.TimeoutException;
import org.hbase.async.GetRequest;
import org.hbase.async.KeyValue;
import org.hbase.async.PutRequest;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.handler.codec.http.HttpVersion;

import java.io.ByteArrayOutputStream;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created with IntelliJ IDEA.
 * User: dirlt
 * Date: 12/8/12
 * Time: 2:03 AM
 * To change this template use File | Settings | File Templates.
 */

public class AsyncClient implements Runnable {
    public static final String kSep = String.format("%c", 0x0);
    public static final byte[] kEmptyBytes = new byte[0];
    public Configuration configuration;

    public AsyncClient(Configuration configuration) {
        this.configuration = configuration;
        preAllocate();
    }

    // state of each step.
    enum Status {
        kStat,
        kMultiRead,
        kMultiWrite,

        kHttpRequest,
        kReadRequest,
        kWriteRequest,
        kReadLocalCache,
        kReadHBaseService,
        kWriteHBaseService,
        kReadHBaseOver,
        kWriteHBaseOver,
        kReadResponse,
        kWriteResponse,
        kHttpResponse,
    }

    // sub request status
    enum RequestStatus {
        kOK,
        kException,
    }

    public boolean subRequest = false; // whether is sub request.
    public Status code = Status.kStat; // default value.
    public RequestStatus requestStatus = RequestStatus.kOK;
    public String requestMessage;
    public Channel channel;

    // for multi interface.
    public AsyncClient parent;
    public AtomicInteger refCounter;
    public List<AsyncClient> clients;

    public ChannelBuffer buffer;
    public String path;
    public byte[] bs;
    public MessageProtos1.ReadRequest.Builder rdReqBuilder;
    public MessageProtos1.ReadRequest rdReq;
    public MessageProtos1.WriteRequest.Builder wrReqBuilder;
    public MessageProtos1.WriteRequest wrReq;
    public MessageProtos1.MultiReadRequest.Builder mRdReqBuilder;
    public MessageProtos1.MultiReadRequest multiReadRequest;
    public MessageProtos1.MultiWriteRequest.Builder mWrReqBuilder;
    public MessageProtos1.MultiWriteRequest multiWriteRequest;
    public ArrayList<KeyValue> keyValues; // key values from async hbase.
    public MessageProtos1.ReadResponse.Builder rdRes;
    public MessageProtos1.ReadResponse.KeyValue.Builder rdResKeyValue;
    public MessageProtos1.WriteResponse.Builder wrRes;
    public MessageProtos1.MultiReadResponse.Builder mRdRes;
    public MessageProtos1.MultiWriteResponse.Builder mWrRes;
    public Message msg;

    public long requestTimestamp;
    public long requestTimeout;
    public long readStartTimestamp;
    public long readEndTimestamp;
    public long readHBaseServiceStartTimestamp;
    public long readHBaseServiceEndTimestamp;
    public long writeHBaseServiceStartTimestamp;
    public long writeHBaseServiceEndTimestamp;

    public String tableName;
    public String rowKey;
    public String columnFamily;

    public String prefix; // cache key prefix.

    public static String makeCacheKeyPrefix(String tableName, String rowKey, String cf) {
        return tableName + kSep + rowKey + kSep + cf;
    }

    public static String makeCacheKey(String prefix, String column) {
        return prefix + kSep + column;
    }

    private List<String> readCacheQualifiers; // qualifiers that to be queried from cache.
    private List<String> readHBaseQualifiers; // qualifiers that to be queried from hbase.
    // if == null, then read column family.

    public void preAllocate() {
        refCounter = new AtomicInteger();
        clients = new LinkedList<AsyncClient>();
        rdReqBuilder = MessageProtos1.ReadRequest.newBuilder();
        wrReqBuilder = MessageProtos1.WriteRequest.newBuilder();
        mRdReqBuilder = MessageProtos1.MultiReadRequest.newBuilder();
        mWrReqBuilder = MessageProtos1.MultiWriteRequest.newBuilder();
        rdRes = MessageProtos1.ReadResponse.newBuilder();
        rdResKeyValue = MessageProtos1.ReadResponse.KeyValue.newBuilder();
        wrRes = MessageProtos1.WriteResponse.newBuilder();
        mRdRes = MessageProtos1.MultiReadResponse.newBuilder();
        mWrRes = MessageProtos1.MultiWriteResponse.newBuilder();
        readCacheQualifiers = new LinkedList<String>();
    }

    public void reset() {
        clients.clear();
        rdReqBuilder.clear();
        wrReqBuilder.clear();
        mRdReqBuilder.clear();
        mWrReqBuilder.clear();
        rdRes.clear();
        rdResKeyValue.clear();
        wrRes.clear();
        mRdRes.clear();
        mWrRes.clear();
        readCacheQualifiers.clear();
    }

    public void init(Status code, boolean subRequest) {
        this.code = code;
        this.subRequest = subRequest;
        requestStatus = RequestStatus.kOK;
        rdReq = null;
        wrReq = null;
        multiReadRequest = null;
        multiWriteRequest = null;
        reset();
    }

    @Override
    public void run() {
        switch (code) {
            case kHttpRequest:
                handleHttpRequest();
                break;
            case kMultiRead:
                multiRead();
                break;
            case kMultiWrite:
                multiWrite();
                break;
            case kReadRequest:
                readRequest();
                break;
            case kWriteRequest:
                writeRequest();
                break;
            case kReadLocalCache:
                readLocalCache();
                break;
            case kReadHBaseService:
                readHBaseService();
                break;
            case kWriteHBaseService:
                writeHBaseService();
                break;
            case kReadHBaseOver:
                readHBaseOver();
                break;
            case kWriteHBaseOver:
                writeHBaseOver();
                break;
            case kReadResponse:
                readResponse();
                break;
            case kWriteResponse:
                writeResponse();
                break;
            case kHttpResponse:
                handleHttpResponse();
                break;
            default:
                break;
        }
    }

    public void handleHttpRequest() {
        RestServer.logger.debug("http request");
        StatStore.getInstance().updateClock(StatStore.ClockFieldName.kCPUQueue, System.currentTimeMillis() - requestTimestamp);
        int size = buffer.readableBytes();
        StatStore.getInstance().addMetric(StatStore.MetricFieldName.kRPCInBytes, size);
        bs = new byte[size];
        buffer.readBytes(bs);

        if (path.equals("/read")) {
            code = Status.kReadRequest;
        } else if (path.equals("/multi-read")) {
            code = Status.kMultiRead;
        } else if (path.equals("/write")) {
            code = Status.kWriteRequest;
        } else if (path.equals("/multi-write")) {
            code = Status.kMultiWrite;
        } else {
            // impossible.
        }

        run();
    }

    public void raiseReadException(String message) {
        code = Status.kReadResponse;
        requestStatus = RequestStatus.kException;
        requestMessage = message;
        run();
    }

    public void raiseWriteException(String message) {
        code = Status.kWriteResponse;
        requestStatus = RequestStatus.kException;
        requestMessage = message;
        run();
    }

    public void raiseReadException(Exception e) {
        raiseReadException(e.toString());
    }

    public void raiseWriteException(Exception e) {
        raiseWriteException(e.toString());
    }

    public void readRequest() {
        RestServer.logger.debug("read request");
        if (!subRequest) {
            // parse request.
            try {
                rdReqBuilder.mergeFrom(bs);
            } catch (InvalidProtocolBufferException e) {
                // just close channel.
                RestServer.logger.debug("parse message exception");
                StatStore.getInstance().addCounter("rpc.in.count.invalid", 1);
                raiseReadException(e);
                return;
            }
            rdReq = rdReqBuilder.build();
            StatStore.getInstance().addMetric(StatStore.MetricFieldName.kRPCReadCount, 1);
            requestTimeout = configuration.getTimeout();
            if (rdReq.hasTimeout()) {
                requestTimeout = rdReq.getTimeout();
            }
        }
        StatStore.getInstance().addMetric(StatStore.MetricFieldName.kReadRequestCount, 1);
        // proxy.
        try {
            rdReq = RequestProxy.getInstance().handleReadRequest(rdReq);
        } catch (Exception e) {
            // just close channel.
            RestServer.logger.debug("request proxy exception");
            StatStore.getInstance().addCounter("request.proxy.exception", 1);
            raiseReadException(e);
            return;
        }

        readStartTimestamp = System.currentTimeMillis();

        tableName = rdReq.getTableName();
        rowKey = rdReq.getRowKey();
        columnFamily = rdReq.getColumnFamily();

        rdRes.setError(false);

        prefix = makeCacheKeyPrefix(tableName, rowKey, columnFamily);

        if (rdReq.getQualifiersCount() == 0) {
            // read column family
            // then we can't do cache.
            code = Status.kReadHBaseService;
            StatStore.getInstance().addMetric(StatStore.MetricFieldName.kReadRequestOfColumnFamilyCount, 1);
        } else {
            // raise local cache request.
            code = Status.kReadLocalCache;
            StatStore.getInstance().addMetric(StatStore.MetricFieldName.kReadRequestOfColumnCount, 1);
        }
        run();
    }

    public void multiRead() {
        RestServer.logger.debug("multi read request");
        try {
            mRdReqBuilder.mergeFrom(bs);
        } catch (InvalidProtocolBufferException e) {
            // just close channel.
            RestServer.logger.debug("parse message exception");
            StatStore.getInstance().addCounter("rpc.in.count.invalid", 1);
            raiseReadException(e);
            return;
        }
        multiReadRequest = mRdReqBuilder.build();
        if (multiReadRequest.getRequestsCount() == 0) {
            RestServer.logger.debug("multi read no sub request");
            StatStore.getInstance().addCounter("rpc.multi-read.error.count", 1);
            raiseReadException("multi read without any request");
            return;
        }
        StatStore.getInstance().addMetric(StatStore.MetricFieldName.kRPCMultiReadCount, 1);
        refCounter.set(multiReadRequest.getRequestsCount());
        requestTimeout = configuration.getTimeout();
        if (multiReadRequest.hasTimeout()) {
            requestTimeout = multiReadRequest.getTimeout();
        }
        for (MessageProtos1.ReadRequest request : multiReadRequest.getRequestsList()) {
            AsyncClient client = new AsyncClient(configuration);
            client.init(Status.kReadRequest, true);
            client.rdReq = request;
            client.parent = this;
            // sub request inherits from parent request.
            client.requestTimeout = requestTimeout;
            client.requestTimestamp = requestTimestamp;
            clients.add(client);
            CpuWorkerPool.getInstance().submit(client);
        }
    }

    public void writeRequest() {
        RestServer.logger.debug("write request");
        if (!subRequest) {
            // parse request.
            try {
                wrReqBuilder.mergeFrom(bs);
            } catch (InvalidProtocolBufferException e) {
                // just close channel.
                RestServer.logger.debug("parse message exception");
                StatStore.getInstance().addCounter("rpc.in.count.invalid", 1);
                raiseWriteException(e);
                return;
            }
            wrReq = wrReqBuilder.build();
            StatStore.getInstance().addMetric(StatStore.MetricFieldName.kRPCWriteCount, 1);
            requestTimeout = configuration.getTimeout();
            if (wrReq.hasTimeout()) {
                requestTimeout = wrReq.getTimeout();
            }
        }
        StatStore.getInstance().addMetric(StatStore.MetricFieldName.kWriteRequestCount, 1);
        // proxy.
        try {
            wrReq = RequestProxy.getInstance().handleWriteRequest(wrReq);
        } catch (Exception e) {
            // just close channel.
            RestServer.logger.debug("request proxy exception");
            StatStore.getInstance().addCounter("request.proxy.exception", 1);
            raiseWriteException(e);
            return;
        }

        tableName = wrReq.getTableName();
        rowKey = wrReq.getRowKey();
        columnFamily = wrReq.getColumnFamily();

        wrRes.setError(false);

        code = Status.kWriteHBaseService;
        run();
    }

    public void multiWrite() {
        RestServer.logger.debug("multi write request");
        try {
            mWrReqBuilder.mergeFrom(bs);
        } catch (InvalidProtocolBufferException e) {
            // just close channel.
            RestServer.logger.debug("parse message exception");
            StatStore.getInstance().addCounter("rpc.in.count.invalid", 1);
            raiseWriteException(e);
            return;
        }
        multiWriteRequest = mWrReqBuilder.build();
        if (multiWriteRequest.getRequestsCount() == 0) {
            RestServer.logger.debug("multi write no sub request");
            StatStore.getInstance().addCounter("rpc.multi-write.error.count", 1);
            raiseWriteException("multi write without any request");
            return;
        }
        StatStore.getInstance().addMetric(StatStore.MetricFieldName.kRPCMultiWriteCount, 1);
        refCounter.set(multiWriteRequest.getRequestsCount());
        requestTimeout = configuration.getTimeout();
        if (multiWriteRequest.hasTimeout()) {
            requestTimeout = multiWriteRequest.getTimeout();
        }
        for (MessageProtos1.WriteRequest request : multiWriteRequest.getRequestsList()) {
            AsyncClient client = new AsyncClient(configuration);
            client.init(Status.kWriteRequest, true);
            client.wrReq = request;
            client.parent = this;
            // sub request inherits from parent request.
            client.requestTimeout = requestTimeout;
            client.requestTimestamp = requestTimestamp;
            clients.add(client);
            CpuWorkerPool.getInstance().submit(client);
        }
    }

    public void readLocalCache() {
        RestServer.logger.debug("read local cache");

        // check local cache mean while fill the cache request.
        int readCount = 0;
        int cacheCount = 0;
        for (String q : rdReq.getQualifiersList()) {
            String cacheKey = null;
            byte[] b = null;
            if (configuration.isCache()) {
                cacheKey = makeCacheKey(prefix, q);
                RestServer.logger.debug("search cache with key = " + cacheKey);
                b = LocalCache.getInstance().get(cacheKey);
            }
            readCount += 1;
            if (b != null) {
                RestServer.logger.debug("cache hit!");
                cacheCount += 1;
                rdResKeyValue.setQualifier(q);
                rdResKeyValue.setContent(ByteString.copyFrom(b));
                rdRes.addKvs(rdResKeyValue.build());
                rdResKeyValue.clear();
            } else {
                RestServer.logger.debug("read hbase qualifier: " + q);
                readCacheQualifiers.add(q);
            }
        }
        StatStore.getInstance().addMetric(StatStore.MetricFieldName.kReadQualifierCount, readCount);
        StatStore.getInstance().addMetric(StatStore.MetricFieldName.kReadQualifierFromCacheCount, cacheCount);
        StatStore.getInstance().addMetric(StatStore.MetricFieldName.kReadQualifierFromHBaseCount, readCount - cacheCount);

        if (!readCacheQualifiers.isEmpty()) {
            code = Status.kReadHBaseService; // read cache service.
            readHBaseQualifiers = readCacheQualifiers;
            StatStore.getInstance().addMetric(StatStore.MetricFieldName.kReadRequestOfColumnFromHBaseCount, 1);
        } else {
            code = Status.kReadResponse; // return directly.
            StatStore.getInstance().addMetric(StatStore.MetricFieldName.kReadRequestOfColumnFromCacheCount, 1);
        }
        run();
    }

    // TODO(dirlt):maybe need modification.
    private long calcRequestTimeout(int retryTime, long allocateTime, int qualifierCount) {
        final long kCPUReservedTimeSlice = 10; // allocate 10ms for CPU.
        final long kMaxRetryTime = 5;
        if (retryTime >= kMaxRetryTime) {
            return 0;
        }
        float t = (1 << retryTime) * 1.0f / (1 << kMaxRetryTime) * (allocateTime - kCPUReservedTimeSlice);
        return (long) t;
    }

    public void readHBaseService() {
        RestServer.logger.debug("read hbase service");
        RestServer.logger.debug("tableName = " + tableName + ", rowKey = " + rowKey + ", columnFamily = " + columnFamily);

        GetRequest getRequest = new GetRequest(tableName, rowKey);
        getRequest.family(columnFamily);
        if (rdReq.getQualifiersCount() != 0) {
            // otherwise we read all qualifiers from column family.
            // a little bit tedious.
            byte[][] qualifiers = new byte[readHBaseQualifiers.size()][];
            int idx = 0;
            for (String q : readHBaseQualifiers) {
                qualifiers[idx] = Utility.toBytes(q);
                idx += 1;
            }
            getRequest.qualifiers(qualifiers);
        } else {
            // nothing.
        }

        readHBaseServiceStartTimestamp = System.currentTimeMillis();
        Exception except = null;
        int retryTime = 0;
        int qualifierCount = rdReq.getQualifiersCount();
        long allocateTime = requestTimeout;
        while (true) {
            long timeout = calcRequestTimeout(retryTime, allocateTime, qualifierCount);
            RestServer.logger.debug(String.format("timeout=%s, retryTime=%d", timeout, retryTime));
            Deferred<ArrayList<KeyValue>> deferred = HBaseService.getInstance().get(getRequest);
            // we don't use callback because of it's lack of controlling timeout.
            try {
                keyValues = deferred.joinUninterruptibly(timeout + 1);
                readHBaseServiceEndTimestamp = System.currentTimeMillis();
                code = Status.kReadHBaseOver;
                RestServer.logger.debug("succeed read hbase service in timeout = " + timeout);
                except = null; // clear exception.
                break;
            } catch (Exception e) {
                except = e;
                if (timeout == 0) {
                    break;
                }
                if (except instanceof TimeoutException) {
                    retryTime += 1;
                    requestTimeout -= timeout;
                } else {  // other exception, we have nothing to do about it.
                    break;
                }
            }
        }
        if (except != null) {
            except.printStackTrace();
            if (except instanceof TimeoutException) {
                StatStore.getInstance().addMetric(StatStore.MetricFieldName.kReadRequestTimeoutOfHBaseCount, 1);
            } else {
                StatStore.getInstance().addMetric(StatStore.MetricFieldName.kReadRequestFailureOfHBaseCount, 1);
            }
            code = Status.kReadResponse;
            requestStatus = RequestStatus.kException;
            requestMessage = except.toString();
        }
        run();
    }

    public void readHBaseOver() {
        RestServer.logger.debug("read hbase over");
        if (rdReq.getQualifiersCount() != 0) {
            // reorder qualifier according request qualifier order.
            // then builder and the cache.
            Map<String, KeyValue> mapping = new HashMap<String, KeyValue>();
            for (KeyValue kv : keyValues) {
                mapping.put(new String(kv.qualifier()), kv);
            }
            int missingCount = 0;
            for (String k : rdReq.getQualifiersList()) {
                KeyValue kv = mapping.get(k);
                rdResKeyValue.setQualifier(k);
                byte[] v = kEmptyBytes;
                if (kv == null) {
                    rdResKeyValue.setContent(ByteString.EMPTY);
                    missingCount++;
                } else {
                    v = kv.value();
                    rdResKeyValue.setContent(ByteString.copyFrom(kv.value()));
                }
                rdRes.addKvs(rdResKeyValue.build());
                rdResKeyValue.clear();
                // fill cache.
                if (configuration.isCache()) {
                    String cacheKey = makeCacheKey(prefix, k);
                    RestServer.logger.debug("fill cache with key = " + cacheKey);
                    LocalCache.getInstance().set(cacheKey, v);
                }
            }
            StatStore.getInstance().updateClock(StatStore.ClockFieldName.kReadHBaseColumn,
                    readHBaseServiceEndTimestamp - readHBaseServiceStartTimestamp);
        } else {
            // just fill the builder. don't save them to cache.
            for (KeyValue kv : keyValues) {
                String k = new String(kv.qualifier());
                byte[] value = kv.value();
                rdResKeyValue.setQualifier(k);
                rdResKeyValue.setContent(ByteString.copyFrom(value));
                rdRes.addKvs(rdResKeyValue.build());
                rdResKeyValue.clear();
            }
            StatStore.getInstance().updateClock(StatStore.ClockFieldName.kReadHBaseColumnFamily,
                    readHBaseServiceEndTimestamp - readHBaseServiceStartTimestamp);
        }

        code = Status.kReadResponse;
        run();
    }

    public void writeHBaseService() {
        RestServer.logger.debug("write hbase service");
        RestServer.logger.debug("tableName = " + tableName + ", rowKey = " + rowKey + ", columnFamily = " + columnFamily);

        byte[][] qualifiers = new byte[wrReq.getKvsCount()][];
        byte[][] values = new byte[wrReq.getKvsCount()][];
        for (int i = 0; i < wrReq.getKvsCount(); i++) {
            qualifiers[i] = Utility.toBytes(wrReq.getKvs(i).getQualifier());
            values[i] = wrReq.getKvs(i).getContent().toByteArray();
        }
        StatStore.getInstance().addMetric(StatStore.MetricFieldName.kWriteQualifierCount, wrReq.getKvsCount());
        PutRequest putRequest = new PutRequest(Utility.toBytes(tableName), Utility.toBytes(rowKey), Utility.toBytes(columnFamily), qualifiers, values);

        writeHBaseServiceStartTimestamp = System.currentTimeMillis();
        Exception except = null;
        int retryTime = 0;
        int qualifierCount = wrReq.getKvsCount();
        long allocateTime = requestTimeout;
        while (true) {
            long timeout = calcRequestTimeout(retryTime, allocateTime, qualifierCount);
            RestServer.logger.debug(String.format("timeout=%s, retryTime=%d", timeout, retryTime));
            Deferred<Object> deferred = HBaseService.getInstance().put(putRequest);
            // we don't use callback because of it's lack of controlling timeout.
            try {
                deferred.joinUninterruptibly(timeout + 1);
                writeHBaseServiceEndTimestamp = System.currentTimeMillis();
                code = Status.kWriteHBaseOver;
                except = null;
                break;
            } catch (Exception e) {
                except = e;
                if (timeout == 0) {
                    break;
                }
                if (except instanceof TimeoutException) {
                    retryTime += 1;
                    requestTimeout -= timeout;
                } else {  // other exception, we have nothing to do about it.
                    break;
                }
            }
        }
        if (except != null) {
            except.printStackTrace();
            if (except instanceof TimeoutException) {
                StatStore.getInstance().addMetric(StatStore.MetricFieldName.kWriteRequestTimeoutOfHBaseCount, 1);
            } else {
                StatStore.getInstance().addMetric(StatStore.MetricFieldName.kWriteRequestFailureOfHBaseCount, 1);
            }
            code = Status.kWriteResponse;
            requestStatus = RequestStatus.kException;
            requestMessage = except.toString();
        }
        run();
    }

    public void writeHBaseOver() {
        RestServer.logger.debug("write hbase over");
        StatStore.getInstance().updateClock(StatStore.ClockFieldName.kWriteRequest,
                writeHBaseServiceEndTimestamp - writeHBaseServiceStartTimestamp);
        code = Status.kWriteResponse;
        run();
    }

    public void readResponse() {
        RestServer.logger.debug("read response");
        if (requestStatus == RequestStatus.kOK) {
            readEndTimestamp = System.currentTimeMillis();
            StatStore.getInstance().updateClock(StatStore.ClockFieldName.kReadRequest,
                    readEndTimestamp - readStartTimestamp);
        }
        if (!subRequest) {
            if (requestStatus != RequestStatus.kOK) {
                if (configuration.isCloseOnFailure()) {
                    channel.close();
                    return;
                }
                rdRes.setError(true);
                rdRes.setMessage(requestMessage);
            }
            msg = rdRes.build();
            code = Status.kHttpResponse;
            run();
        } else {
            int count = parent.refCounter.decrementAndGet();
            if (count == 0) {
                for (AsyncClient client : parent.clients) {
                    // if any one fails, then it fails.
                    if (client.requestStatus != RequestStatus.kOK) {
                        if (configuration.isCloseOnFailure()) {
                            parent.channel.close();
                            return;
                        }
                        client.rdRes.setError(true);
                        client.rdRes.setMessage(client.requestMessage);
                    }
                    parent.mRdRes.addResponses(client.rdRes.build());
                }
                parent.msg = parent.mRdRes.build();
                parent.code = Status.kHttpResponse;
                parent.run();
            }
        }
    }

    public void writeResponse() {
        RestServer.logger.debug("write response");
        if (!subRequest) {
            if (requestStatus != RequestStatus.kOK) {
                if (configuration.isCloseOnFailure()) {
                    channel.close();
                    return;
                }
                wrRes.setError(true);
                wrRes.setMessage(requestMessage);
            }
            msg = wrRes.build();
            code = Status.kHttpResponse;
            run();
        } else {
            int count = parent.refCounter.decrementAndGet();
            if (count == 0) {
                parent.mWrRes = MessageProtos1.MultiWriteResponse.newBuilder();
                for (AsyncClient client : parent.clients) {
                    if (client.requestStatus != RequestStatus.kOK) {
                        if (configuration.isCloseOnFailure()) {
                            parent.channel.close();
                            return;
                        }
                        client.wrRes.setError(true);
                        client.wrRes.setMessage(client.requestMessage);
                    }
                    parent.mWrRes.addResponses(client.wrRes.build());
                }
                parent.msg = parent.mWrRes.build();
                parent.code = Status.kHttpResponse;
                parent.run();
            }
        }
    }

    public void handleHttpResponse() {
        RestServer.logger.debug("http response");

        HttpResponse response = new DefaultHttpResponse(
                HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
        int size = msg.getSerializedSize();
        response.setHeader("Content-Length", size);
        RestServer.logger.debug("http response content length = " + size);
        ByteArrayOutputStream os = new ByteArrayOutputStream(size);
        try {
            msg.writeTo(os);
            ChannelBuffer buffer = ChannelBuffers.copiedBuffer(os.toByteArray());
            response.setContent(buffer);
            channel.write(response); // write over.
            StatStore.getInstance().addMetric(StatStore.MetricFieldName.kRPCOutBytes, size);
        } catch (Exception e) {
            // just ignore it.
        } finally {
            channel.setReadable(true);
        }
    }
}
