package com.dirlt.java.FastHBaseRest;

import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: dirlt
 * Date: 12/8/12
 * Time: 1:32 AM
 * To change this template use File | Settings | File Templates.
 */
public class Configuration {
    private String ip = "0.0.0.0";
    private int port = 12345;
    private int backlog = 128;
    private String quorumSpec = "localhost:2181";
    private int cpuThreadNumber = 16;
    private int cpuQueueSize = 4096;
    private int cacheExpireTime = 3600;
    private int cacheMaxCapacity = 2 * 1024 * 1024;
    private int acceptIOThreadNumber = 4;
    private int acceptIOQueueSize = 16;
    private int ioThreadNumber = 16;
    private int ioQueueSize = 4096;
    private int retry = 3;
    private int timeout = 10 * 1000; // ms.
    private String serviceName = "FastHBaseRest";
    private boolean closeOnFailure = false;
    private boolean stat = true;
    private boolean cache = true;
    private boolean debug = true;
    private Map<String, String> kv = new HashMap<String, String>();

    public boolean parse(String[] args) {
        for (String arg : args) {
            if (arg.startsWith("--ip=")) {
                ip = arg.substring("--ip=".length());
            } else if (arg.startsWith("--port=")) {
                port = Integer.valueOf(arg.substring("--port=".length())).intValue();
            } else if (arg.startsWith("--backlog=")) {
                backlog = Integer.valueOf(arg.substring("--backlog=".length())).intValue();
            } else if (arg.startsWith("--hbase-quorum-spec=")) {
                quorumSpec = arg.substring("--hbase-quorum-spec=".length());
            } else if (arg.startsWith("--cpu-thread-number=")) {
                cpuThreadNumber = Integer.valueOf(arg.substring("--cpu-thread-number=".length()));
            } else if (arg.startsWith("--cpu-queue-size=")) {
                cpuQueueSize = Integer.valueOf(arg.substring("--cpu-queue-size=".length()));
            } else if (arg.startsWith("--cache-expire-time=")) {
                cacheExpireTime = Integer.valueOf(arg.substring("--cache-expire-time=".length()));
            } else if (arg.startsWith("--cache-max-capacity=")) {
                cacheMaxCapacity = Integer.valueOf(arg.substring("--cache-max-capacity=".length()));
            } else if (arg.startsWith("--accept-io-thread-number=")) {
                acceptIOThreadNumber = Integer.valueOf(arg.substring("--accept-io-thread-number=".length()));
            } else if (arg.startsWith("--accept-io-queue-size=")) {
                acceptIOQueueSize = Integer.valueOf(arg.substring("--accept-io-queue-size=".length()));
            } else if (arg.startsWith("--io-thread-number=")) {
                ioThreadNumber = Integer.valueOf(arg.substring("--io-thread-number=".length()));
            } else if (arg.startsWith("--io-queue-size=")) {
                ioQueueSize = Integer.valueOf(arg.substring("--io-queue-size=".length()));
            } else if (arg.startsWith("--retry=")) {
                retry = Integer.valueOf(arg.substring("--retry=".length()));
            } else if (arg.startsWith("--timeout=")) {
                timeout = Integer.valueOf(arg.substring("--timeout=".length()));
            } else if (arg.startsWith("--service-name=")) {
                serviceName = arg.substring("--service-name=".length());
            } else if (arg.startsWith("--close-on-failure=")) {
                closeOnFailure = Boolean.valueOf(arg.substring("--close-on-failure=".length()));
            } else if (arg.startsWith("--stat=")) {
                stat = Boolean.valueOf(arg.substring("--stat=".length()));
            } else if (arg.startsWith("--cache=")) {
                cache = Boolean.valueOf(arg.substring("--cache=".length()));
            } else if (arg.startsWith("--debug=")) {
                debug = Boolean.valueOf(arg.substring("--debug=".length()));
            } else if (arg.startsWith("--kv=")) {
                String s = arg.substring("--kv=".length());
                String[] ss = s.split(":");
                kv.put(ss[0], ss[1]);
            } else {
                return false;
            }
        }
        return true;
    }

    public static void usage() {
        System.out.println("Fast HBase Rest");
        System.out.println("\t--ip # default 0.0.0.0");
        System.out.println("\t--port # default 12345");
        System.out.println("\t--backlog # default 128");
        System.out.println("\t--hbase-quorum-spec # zookeeper address list. eg. \"host1,host2,host3\". default \"localhost\"");
        System.out.println("\t--cpu-thread-number # default 16");
        System.out.println("\t--cpu-queue-size # default 4096");
        System.out.println("\t--cache-expire-time # default 3600(s)");
        System.out.println("\t--cache-max-capacity # default 2 * 1024 * 1024");
        System.out.println("\t--accept-io-thread-number # default 4");
        System.out.println("\t--accept-io-queue-size # default 16");
        System.out.println("\t--io-thread-number # default 16");
        System.out.println("\t--io-queue-size # default 4096");
        System.out.println("\t--retry # default 3");
        System.out.println("\t--timeout # default 10 * 1000(ms)");
        System.out.println("\t--service-name # set service name");
        System.out.println("\t--close-on-failure # default false");
        System.out.println("\t--stat # default true");
        System.out.println("\t--cache # default true");
        System.out.println("\t--debug # default true");
        System.out.println("\t--kv=<key>:<value> # key value pair");
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append(String.format("stat=%s, cache=%s, debug=%s\n", isStat(), isCache(), isDebug()));
        sb.append(String.format("ip=%s, port=%d, backlog=%d\n", getIp(), getPort(), getBacklog()));
        sb.append(String.format("accept-io-thread-number=%d, accept-io-queue-size=%d\n", getAcceptIOThreadNumber(), getAcceptIOQueueSize()));
        sb.append(String.format("io-thread-number=%d, io-queue-size=%d\n", getIOThreadNumber(), getIOQueueSize()));
        sb.append(String.format("cpu-thread-number=%d, cpu-queue-size=%d\n", getCpuThreadNumber(), getCpuQueueSize()));
        sb.append(String.format("timeout=%d(ms), retry=%d\n", getTimeout(), getRetry()));
        sb.append(String.format("hbase-quorum-spec=%s\n", getQuorumSpec()));
        sb.append(String.format("cache-expire-time=%d(s), cache-max-capacity=%d\n", getCacheExpireTime(), getCacheMaxCapacity()));
        sb.append(String.format("close-on-failure=%s\n", isCloseOnFailure()));
        for (String key : kv.keySet()) {
            sb.append(String.format("kv = %s:%s\n", key, kv.get(key)));
        }
        return sb.toString();
    }

    public String getIp() {
        return ip;
    }

    public int getPort() {
        return port;
    }

    public int getBacklog() {
        return backlog;
    }

    public boolean isCloseOnFailure() {
        return closeOnFailure;
    }

    public String getQuorumSpec() {
        return quorumSpec;
    }

    public int getCpuThreadNumber() {
        return cpuThreadNumber;
    }

    public int getCpuQueueSize() {
        return cpuQueueSize;
    }

    public int getCacheExpireTime() {
        return cacheExpireTime;
    }

    public int getCacheMaxCapacity() {
        return cacheMaxCapacity;
    }


    public int getAcceptIOThreadNumber() {
        return acceptIOThreadNumber;
    }

    public int getAcceptIOQueueSize() {
        return acceptIOQueueSize;
    }

    public int getIOThreadNumber() {
        return ioThreadNumber;
    }

    public int getIOQueueSize() {
        return ioQueueSize;
    }

    public String getServiceName() {
        return serviceName;
    }

    public int getRetry() {
        return retry;
    }

    public int getTimeout() {
        return timeout;
    }

    public boolean isDebug() {
        return debug;
    }

    public boolean isStat() {
        return stat;
    }

    public boolean isCache() {
        return cache;
    }

    public Map<String, String> getKv() {
        return kv;
    }
}
