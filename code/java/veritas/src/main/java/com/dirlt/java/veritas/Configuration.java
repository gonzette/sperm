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
    private int port = 12347;
    private int backlog = 128;
    private String backendNodes = "localhost:12345";
    private int cpuThreadNumber = 16;
    private int cpuQueueSize = 4096;
    private int acceptIOThreadNumber = 4;
    private int ioThreadNumber = 16;
    //    private int readTimeout = 500; // ms
//    private int writeTimeout = 500; // ms
    private int timeout = 10 * 1000; // ms.
    private int proxyAcceptIOThreadNumber = 4;
    private int proxyIOThreadNumber = 16;
    private int proxyConnectionTimeout = 10; // ms
    private int proxyConnectionNumberPerNode = 32;
    private String serviceName = "veritas";
    private boolean debug = true;
    private boolean stat = true;
    private boolean responseWithBestEffort = true;

    // table info.
    private String deviceIdMappingTable = "device_id_mapping";
    private String deviceIdMappingColumnFamily = "mapping";
    private String userInfoTable = "userinfo";
    private String userInfoColumnFamily = "info";

    // kv.
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
            } else if (arg.startsWith("--timeout=")) {
                timeout = Integer.valueOf(arg.substring("--timeout=".length())).intValue();
            } else if (arg.startsWith("--proxy-accept-io-thread-number=")) {
                proxyAcceptIOThreadNumber = Integer.valueOf(arg.substring("--proxy-accept-io-thread-number=".length())).intValue();
            } else if (arg.startsWith("--proxy-io-thread-number=")) {
                proxyIOThreadNumber = Integer.valueOf(arg.substring("--proxy-io-thread-number=".length())).intValue();
            } else if (arg.startsWith("--proxy-connection-timeout=")) {
                proxyConnectionTimeout = Integer.valueOf(arg.substring("--proxy-connection-timeout=".length())).intValue();
            } else if (arg.startsWith("--proxy-connection-per-node=")) {
                proxyConnectionNumberPerNode = Integer.valueOf(arg.substring("--proxy-connection-per-node=".length())).intValue();
            } else if (arg.startsWith("--service-name=")) {
                serviceName = arg.substring("--service-name=".length());
            } else if (arg.startsWith("--device-id-mapping-table=")) {
                deviceIdMappingTable = arg.substring("--device-id-mapping-table=".length());
            } else if (arg.startsWith("--device-id-mapping-column-family=")) {
                deviceIdMappingColumnFamily = arg.substring("--device-id-mapping-column-family=".length());
            } else if (arg.startsWith("--user-info-table=")) {
                userInfoTable = arg.substring("--user-info-table=".length());
            } else if (arg.startsWith("--user-info-column-family=")) {
                userInfoColumnFamily = arg.substring("--user-info-column-family=".length());
            } else if (arg.startsWith("--debug=")) {
                debug = Boolean.valueOf(arg.substring("--debug=".length()));
            } else if (arg.startsWith("--stat=")) {
                stat = Boolean.valueOf(arg.substring("--stat=".length()));
            } else if (arg.startsWith("--response-with-best-effort=")) {
                responseWithBestEffort = Boolean.valueOf(arg.substring("--response-with-best-effort=".length()));
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
        System.out.println("\t--port # default 12347");
        System.out.println("\t--backlog # default 128");
        System.out.println("\t--backend-nodes # default localhost:12345");
        System.out.println("\t--cpu-thread-number # default 16");
        System.out.println("\t--cpu-queue-size # default 4096");
        System.out.println("\t--accept-io-thread-number # default 4");
        System.out.println("\t--io-thread-number # default 16");
//        System.out.println("\t--read-timeout # default 500(ms)");
//        System.out.println("\t--write-timeout # default 500(ms)");
        System.out.println("\t--timeout # default 10 * 1000(ms)");
        System.out.println("\t--proxy-queue-size # default 256");
        System.out.println("\t--proxy-accept-io-thread-number # default 4");
        System.out.println("\t--proxy-io-thread-number # default 16");
        System.out.println("\t--proxy-read-timeout # default 200(ms)");
        System.out.println("\t--proxy-write-timeout # default 200(ms)");
        System.out.println("\t--proxy-connection-timeout # default 10(ms)");
        System.out.println("\t--proxy-connection-per-node # default 32");
        System.out.println("\t--service-name # set service name");
        System.out.println("\t--device-id-mapping-table # default device_id_mapping");
        System.out.println("\t--device-id-mapping-column-family # default mapping");
        System.out.println("\t--user-info-table # default userinfo");
        System.out.println("\t--user-info-column-family # default info");
        System.out.println("\t--response-with-best-effort # default true");
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
        sb.append(String.format("timeout=%d(ms)\n", getTimeout()));
        sb.append(String.format("proxy-accept-io-thread-number=%d, proxy-io-thread-number=%d\n",
                getProxyAcceptIOThreadNumber(), getProxyIOThreadNumber()));
        sb.append(String.format("proxy-connection-per-node=%d\n", getProxyConnectionNumberPerNode()));
        sb.append(String.format("proxy-connection-timeout=%d(ms)\n", getProxyConnectionTimeout()));
        sb.append(String.format("device-id-mapping-table=%s, device-id-mapping-column-family=%s\n",
                getDeviceIdMappingTable(), getDeviceIdMappingColumnFamily()));
        sb.append(String.format("user-info-table=%s, user-info-column-family=%s\n",
                getUserInfoTable(), getUserInfoColumnFamily()));
        sb.append(String.format("return-with-best-effort=%s\n", isResponseWithBestEffort()));
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

    public int getTimeout() {
        return timeout;
    }

    public int getProxyAcceptIOThreadNumber() {
        return proxyAcceptIOThreadNumber;
    }

    public int getProxyIOThreadNumber() {
        return proxyIOThreadNumber;
    }

    public int getProxyConnectionNumberPerNode() {
        return proxyConnectionNumberPerNode;
    }

    public int getProxyConnectionTimeout() {
        return proxyConnectionTimeout;
    }

    public String getServiceName() {
        return serviceName;
    }

    public String getDeviceIdMappingTable() {
        return deviceIdMappingTable;
    }

    public String getDeviceIdMappingColumnFamily() {
        return deviceIdMappingColumnFamily;
    }

    public String getUserInfoTable() {
        return userInfoTable;
    }

    public String getUserInfoColumnFamily() {
        return userInfoColumnFamily;
    }

    public boolean isResponseWithBestEffort() {
        return responseWithBestEffort;
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
