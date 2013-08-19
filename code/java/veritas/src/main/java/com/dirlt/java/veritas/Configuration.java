package com.dirlt.java.veritas;

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
    private int port = 12346;
    private int backlog = 128;
    private String backendNodes = "localhost:12345";
    private int cpuThreadNumber = 16;
    private int cpuQueueSize = 4096;
    private int acceptIOThreadNumber = 4;
    private int ioThreadNumber = 16;
//    private int readTimeout = 500; // 500ms.
//    private int writeTimeout = 500; // 500ms.
    private int proxyQueueSize = 256;
    private int proxyAcceptIOThreadNumber = 4;
    private int proxyIOThreadNumber = 16;
    private int proxyReadTimeout = 200; // 100 ms
    private int proxyWriteTimeout = 200; // 100 ms
    private int proxyMaxConnectionNumber = 32;
    private int proxyMinConnectionNumber = 4;
    private int proxyAddConnectionNumberStep = 4;
    private int proxyTimerTickInterval = 1000; // 1000ms
    private int proxyRecoveryTickNumber = 6; // 6 * 1000ms = 6s.

    private String serviceName = "veritas";
    private boolean debug = true;
    private boolean stat = true;
    private Map<String, String> kv = new HashMap<String, String>();

    public boolean parse(String[] args) {
        for (String arg : args) {
            if (arg.startsWith("--ip=")) {
                ip = arg.substring("--ip=".length());
            } else if (arg.startsWith("--port=")) {
                port = Integer.valueOf(arg.substring("--port=".length())).intValue();
            } else if (arg.startsWith("--backlog=")) {
                backlog = Integer.valueOf(arg.substring("--backlog=".length())).intValue();
            } else if (arg.startsWith("--backend-nodes=")) {
                backendNodes = arg.substring("--backend-nodes=".length());
            } else if (arg.startsWith("--cpu-thread-number=")) {
                cpuThreadNumber = Integer.valueOf(arg.substring("--cpu-thread-number=".length()));
            } else if (arg.startsWith("--cpu-queue-size=")) {
                cpuQueueSize = Integer.valueOf(arg.substring("--cpu-queue-size=".length()));
            } else if (arg.startsWith("--accept-io-thread-number=")) {
                acceptIOThreadNumber = Integer.valueOf(arg.substring("--accept-io-thread-number=".length()));
            } else if (arg.startsWith("--io-thread-number=")) {
                ioThreadNumber = Integer.valueOf(arg.substring("--io-thread-number=".length())).intValue();
//            } else if (arg.startsWith("--read-timeout=")) {
//                readTimeout = Integer.valueOf(arg.substring("--read-timeout=".length())).intValue();
//            } else if (arg.startsWith("--write-timeout=")) {
//                writeTimeout = Integer.valueOf(arg.substring("--write-timeout=".length())).intValue();
            } else if (arg.startsWith("--proxy-queue-size=")) {
                proxyQueueSize = Integer.valueOf(arg.substring("--proxy-queue-size=".length())).intValue();
            } else if (arg.startsWith("--proxy-accept-io-thread-number=")) {
                proxyAcceptIOThreadNumber = Integer.valueOf(arg.substring("--proxy-accept-io-thread-number=".length())).intValue();
            } else if (arg.startsWith("--proxy-io-thread-number=")) {
                proxyIOThreadNumber = Integer.valueOf(arg.substring("--proxy-io-thread-number=".length())).intValue();
            } else if (arg.startsWith("--proxy-read-timeout=")) {
                proxyReadTimeout = Integer.valueOf(arg.substring("--proxy-read-timeout=".length())).intValue();
            } else if (arg.startsWith("--proxy-write-timeout=")) {
                proxyWriteTimeout = Integer.valueOf(arg.substring("--proxy-write-timeout=".length())).intValue();
            } else if (arg.startsWith("--proxy-max-connection-number=")) {
                proxyMaxConnectionNumber = Integer.valueOf(arg.substring("--proxy-max-connection-number=".length())).intValue();
            } else if (arg.startsWith("--proxy-min-connection-number=")) {
                proxyMinConnectionNumber = Integer.valueOf(arg.substring("--proxy-min-connection-number=".length())).intValue();
            } else if (arg.startsWith("--proxy-add-connection-number-step=")) {
                proxyAddConnectionNumberStep = Integer.valueOf(arg.substring("--proxy-add-connection-number-step=".length())).intValue();
            } else if (arg.startsWith("--proxy-timer-tick-interval=")) {
                proxyTimerTickInterval = Integer.valueOf(arg.substring("--proxy-timer-tick-interval=".length())).intValue();
            } else if (arg.startsWith("--proxy-recovery-tick-number=")) {
                proxyRecoveryTickNumber = Integer.valueOf(arg.substring("--proxy-recovery-tick-number=".length())).intValue();
            } else if (arg.startsWith("--service-name=")) {
                serviceName = arg.substring("--service-name=".length());
            } else if (arg.startsWith("--debug=")) {
                debug = Boolean.valueOf(arg.substring("--debug=".length()));
            } else if (arg.startsWith("--stat=")) {
                stat = Boolean.valueOf(arg.substring("--stat=".length()));
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
        System.out.println("veritas");
        System.out.println("\t--ip # default 0.0.0.0");
        System.out.println("\t--port # default 12346");
        System.out.println("\t--backlog # default 128");
        System.out.println("\t--backend-nodes # default localhost:12345");
        System.out.println("\t--cpu-thread-number # default 16");
        System.out.println("\t--cpu-queue-size # default 4096");
        System.out.println("\t--accept-io-thread-number # default 4");
        System.out.println("\t--io-thread-number # default 16");
//        System.out.println("\t--read-timeout # default 500(ms)");
//        System.out.println("\t--write-timeout # default 500(ms)");
        System.out.println("\t--proxy-queue-size # default 256");
        System.out.println("\t--proxy-accept-io-thread-number # default 4");
        System.out.println("\t--proxy-io-thread-number # default 16");
        System.out.println("\t--proxy-read-timeout # default 100(ms)");
        System.out.println("\t--proxy-write-timeout # default 100(ms)");
        System.out.println("\t--proxy-max-connection-number # default 32");
        System.out.println("\t--proxy-min-connection-number # default 4");
        System.out.println("\t--proxy-add-connection-number-step # default 4");
        System.out.println("\t--proxy-timer-tick-interval # default 1000(ms)");
        System.out.println("\t--proxy-recovery-tick-number # default 6");
        System.out.println("\t--service-name # set service name");
        System.out.println("\t--debug # default true");
        System.out.println("\t--stat # default true");
        System.out.println("\t--kv=<key>:<value> # key value pair");
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append(String.format("stat=%s, debug=%s\n", isStat(), isDebug()));
        sb.append(String.format("ip=%s, port=%d, backlog=%d\n", getIp(), getPort(), getBacklog()));
        sb.append(String.format("backend-nodes=%s\n", getBackendNodes()));
        sb.append(String.format("cpu-thread-number=%d, cpu-queue-size=%d\n", getCpuThreadNumber(), getCpuQueueSize()));
        sb.append(String.format("service-name=%s\n", getServiceName()));
        sb.append(String.format("accept-io-thread-number=%d, io-thread-number=%d\n",
                getAcceptIOThreadNumber(), getIoThreadNumber()));
//        sb.append(String.format("read-timeout=%d(ms), write-timeout=%d(ms)\n", getReadTimeout(), getWriteTimeout()));
        sb.append(String.format("proxy-queue-size=%d\n", getProxyQueueSize()));
        sb.append(String.format("proxy-accept-io-thread-number=%d, proxy-io-thread-number=%d\n",
                getProxyAcceptIOThreadNumber(), getProxyIOThreadNumber()));
        sb.append(String.format("proxy-read-timeout=%d(ms), proxy-write-timeout=%d(ms)\n",
                getProxyReadTimeout(), getProxyWriteTimeout()));
        sb.append(String.format("proxy-min-connection-number=%d proxy-max-connection-number=%d proxy-add-connection-step=%d\n",
                getProxyMinConnectionNumber(), getProxyMaxConnectionNumber(), getProxyAddConnectionNumberStep()));
        sb.append(String.format("proxy-timer-tick-interval=%d(ms), proxy-recovery-tick-number=%d\n",
                getProxyTimerTickInterval(), getProxyRecoveryTickNumber()));
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

    public String getBackendNodes() {
        return backendNodes;
    }

    public int getCpuThreadNumber() {
        return cpuThreadNumber;
    }

    public int getCpuQueueSize() {
        return cpuQueueSize;
    }

    public int getAcceptIOThreadNumber() {
        return acceptIOThreadNumber;
    }

    public int getIoThreadNumber() {
        return ioThreadNumber;
    }

//    public int getReadTimeout() {
//        return readTimeout;
//    }
//
//    public int getWriteTimeout() {
//        return writeTimeout;
//    }

    public int getProxyQueueSize() {
        return proxyQueueSize;
    }

    public int getProxyAcceptIOThreadNumber() {
        return proxyAcceptIOThreadNumber;
    }

    public int getProxyIOThreadNumber() {
        return proxyIOThreadNumber;
    }

    public int getProxyReadTimeout() {
        return proxyReadTimeout;
    }

    public int getProxyWriteTimeout() {
        return proxyWriteTimeout;
    }

    public int getProxyMinConnectionNumber() {
        return proxyMinConnectionNumber;
    }

    public int getProxyMaxConnectionNumber() {
        return proxyMaxConnectionNumber;
    }

    public int getProxyTimerTickInterval() {
        return proxyTimerTickInterval;
    }

    public int getProxyRecoveryTickNumber() {
        return proxyRecoveryTickNumber;
    }

    public int getProxyAddConnectionNumberStep() {
        return proxyAddConnectionNumberStep;
    }

    public String getServiceName() {
        return serviceName;
    }

    public boolean isDebug() {
        return debug;
    }

    public boolean isStat() {
        return stat;
    }

    public Map<String, String> getKv() {
        return kv;
    }
}
