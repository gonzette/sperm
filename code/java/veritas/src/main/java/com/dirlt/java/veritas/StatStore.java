package com.dirlt.java.veritas;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: dirlt
 * Date: 12/8/12
 * Time: 1:42 AM
 * To change this template use File | Settings | File Templates.
 */

public class StatStore {
    // private fields.
    // lock contention.
    private Map<String, Long> counter = new HashMap<String, Long>();

    // metric.
    class Metric {
        int value = 0;

        public void clear() {
            value = 0;
        }

        public void add(int delta) {
            value += delta;
        }

        public int get() {
            return value;
        }
    }

    enum MetricFieldName {
        kRPCInBytes,
        kRPCOutBytes,
        kRPCSingleRequestCount,
        kRPCMultiRequestCount,
        kRPCResponseCount,
        kRequestCount,
        kProxyIdRequestCount,
        kProxyIdRequestBytes,
        kProxyIdResponseBytes,
        kProxyInfoRequestCount,
        kProxyInfoRequestBytes,
        kProxyInfoResponseBytes,
        kMetricEnd,
    }

    Metric metric[];

    // clock
    // 1. excellent (0-4],(4-8]. we need to consider time precision.
    // 2. good (8-16],(16-32]
    // 4. acceptable (32,64],(64,128]
    // 5. intolerable (128,256],(256,...)
    private static final int[] kTimeDistribution = {4, 8, 16, 32, 64, 128, 256};

    class Clock {
        long time = 0L;
        int count = 0;
        int disCount[] = new int[kTimeDistribution.length + 1];
        long sMax = 0L;

        public void clear() {
            time = 0L;
            count = 0;
            for (int i = 0; i < disCount.length; i++) {
                disCount[i] = 0;
            }
            sMax = 0L;
        }

        public void update(long t) {
            time += t;
            int i = 0;
            for (i = 0; i < kTimeDistribution.length; i++) {
                if (kTimeDistribution[i] >= t) {
                    disCount[i] += 1;
                    break;
                }
            }
            if (i == kTimeDistribution.length) {
                disCount[disCount.length - 1] += 1;
            }
            count += 1;
            if (t > sMax) {
                sMax = t;
            }
        }

        public String get() {
            StringBuffer sb = new StringBuffer();
            int all = count;
            if (all == 0) {
                all += 1;
            }
            sb.append(String.format("time = %s(ms), count = %d, avg = %.2f(ms), max = %s(ms)\n",
                    String.valueOf(time), count, time * 1.0 / all, sMax));
            if (disCount[0] != 0) {
                sb.append(String.format("  (0, %d] = %d(%.2f)\n",
                        kTimeDistribution[0],
                        disCount[0],
                        disCount[0] * 1.0 / all));
            }
            for (int i = 1; i < kTimeDistribution.length; i++) {
                if (disCount[i] != 0) {
                    sb.append(String.format("  (%d, %d] = %d(%.2f)\n",
                            kTimeDistribution[i - 1],
                            kTimeDistribution[i], disCount[i],
                            disCount[i] * 1.0 / all));
                }
            }
            if (disCount[disCount.length - 1] != 0) {
                sb.append(String.format("  (%d, ...) = %d(%.2f)\n",
                        kTimeDistribution[kTimeDistribution.length - 1],
                        disCount[disCount.length - 1],
                        disCount[disCount.length - 1] * 1.0 / all));
            }
            return sb.toString();
        }
    }

    enum ClockFieldName {
        kCPUQueue,
        kRequest,
        kProxyId,
        kProxyInfo,
        kClockEnd
    }

    Clock clock[];


    private Configuration configuration;

    // global singleton.
    private static final int kReservedSize = 5;
    private static final int kTickInterval = 60 * 1000;
    private static Configuration gConfiguration;
    private static StatStore[] pool = new StatStore[kReservedSize];
    private static volatile int current = 0;

    public static StatStore getInstance() {
        return pool[current];
    }

    public static StatStore getInstance(int index) {
        return pool[index];
    }

    public static String getStat() {
        StringBuffer sb = new StringBuffer();
        sb.append(String.format("Service : %s\n", gConfiguration.getServiceName()));
        sb.append(String.format("=====configuration=====\n%s\n", gConfiguration.toString()));
        sb.append(String.format("=====connector=====\n%s\n", ProxyConnector.getInstance().getStat()));
        for (int i = 0; i < kReservedSize; i++) {
            int index = (current - i + kReservedSize) % kReservedSize;
            sb.append(String.format("=====last %d minutes=====\n", i));
            getInstance(index).getStat(sb);
            sb.append("\n");
        }
        return sb.toString();
    }

    public StatStore(Configuration configuration) {
        this.configuration = configuration;
        metric = new Metric[MetricFieldName.kMetricEnd.ordinal()];
        for (MetricFieldName name : MetricFieldName.values()) {
            if (name == MetricFieldName.kMetricEnd) {
                break;
            }
            metric[name.ordinal()] = new Metric();
        }
        clock = new Clock[ClockFieldName.kClockEnd.ordinal()];
        for (ClockFieldName name : ClockFieldName.values()) {
            if (name == ClockFieldName.kClockEnd) {
                break;
            }
            clock[name.ordinal()] = new Clock();
        }
    }

    public static void init(Configuration configuration) {
        gConfiguration = configuration;
        for (int i = 0; i < kReservedSize; i++) {
            pool[i] = new StatStore(configuration);
        }
        Timer tickTimer = new Timer(true);
        tickTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                int next = (current + 1) % kReservedSize;
                pool[next].clear();
                current = next;
            }
        }, 0, kTickInterval);
    }

    public void addCounter(String name, long value) {
        if (configuration.isDebug()) {
            synchronized (counter) {
                if (counter.containsKey(name)) {
                    counter.put(name, counter.get(name) + value);
                } else {
                    counter.put(name, value);
                }
            }
        }
    }

    public void addMetric(MetricFieldName name, int value) {
        metric[name.ordinal()].add(value);
    }

    public void updateClock(ClockFieldName name, long time) {
        clock[name.ordinal()].update(time);
    }

    public void clear() {
        if (configuration.isDebug()) {
            synchronized (counter) {
                counter.clear();
            }
        }
        for (int i = 0; i < metric.length; i++) {
            metric[i].clear();
        }

        for (int i = 0; i < clock.length; i++) {
            clock[i].clear();
        }
    }

    // well a little too simple.:).
    private void getStat(StringBuffer sb) {
        if (configuration.isDebug()) {
            sb.append(">>>>>counter<<<<<\n");
            synchronized (counter) {
                Set<Map.Entry<String, Long>> entries = counter.entrySet();
                for (Map.Entry<String, Long> entry : entries) {
                    sb.append(String.format("%s = %s\n", entry.getKey(), entry.getValue().toString()));
                }
            }

        }
        sb.append(">>>>>metric<<<<<\n");
        for (MetricFieldName name : MetricFieldName.values()) {
            if (name == MetricFieldName.kMetricEnd) {
                break;
            }
            sb.append(String.format("%s = %d\n", name.name(), metric[name.ordinal()].get()));
        }
        sb.append(">>>>>clock<<<<<\n");
        for (ClockFieldName name : ClockFieldName.values()) {
            if (name == ClockFieldName.kClockEnd) {
                break;
            }
            sb.append(String.format("%s : %s", name.name(), clock[name.ordinal()].get()));
        }
    }
}