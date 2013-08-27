package com.dirlt.java.FastHBaseRest;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

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

    // some basic fields.
    class Metric {
        AtomicInteger value = new AtomicInteger();

        public void clear() {
            value.set(0);
        }

        public void add(int delta) {
            value.addAndGet(delta);
        }

        public int get() {
            return value.get();
        }
    }

    enum MetricFieldName {
        kRPCInBytes,
        kRPCOutBytes,
        kRPCReadCount,
        kRPCMultiReadCount,
        kReadRequestCount,
        kReadRequestOfColumnCount,
        kReadRequestOfColumnFamilyCount,
        kReadQualifierCount,
        kReadQualifierFromCacheCount,
        kReadQualifierFromHBaseCount,
        kRPCWriteCount,
        kRPCMultiWriteCount,
        kWriteRequestCount,
        kWriteQualifierCount,
        kEnd,
    }

    Metric metric[];

    private Configuration configuration;

    // global singleton.
    private static final int kReservedSize = 10;
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
        metric = new Metric[MetricFieldName.kEnd.ordinal()];
        for (MetricFieldName name : MetricFieldName.values()) {
            if (name == MetricFieldName.kEnd) {
                break;
            }
            metric[name.ordinal()] = new Metric();
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
        if (!configuration.isStat()) {
            return;
        }
        synchronized (counter) {
            if (counter.containsKey(name)) {
                counter.put(name, counter.get(name) + value);
            } else {
                counter.put(name, value);
            }
        }
    }

    public void addCounter(MetricFieldName name, int value) {
        metric[name.ordinal()].add(value);
    }

    public void clear() {
        if (configuration.isStat()) {
            synchronized (counter) {
                counter.clear();
            }
        }
        for (int i = 0; i < MetricFieldName.kEnd.ordinal(); i++) {
            metric[i].clear();
        }
    }

    // well a little too simple.:).
    private void getStat(StringBuffer sb) {
        if (configuration.isStat()) {
            synchronized (counter) {
                Set<Map.Entry<String, Long>> entries = counter.entrySet();
                for (Map.Entry<String, Long> entry : entries) {
                    sb.append(String.format("%s = %s\n", entry.getKey(), entry.getValue().toString()));
                }
            }
            sb.append("----------\n");
        }
        for (MetricFieldName name : MetricFieldName.values()) {
            if (name == MetricFieldName.kEnd) {
                break;
            }
            sb.append(String.format("%s = %d\n", name.name(), metric[name.ordinal()].get()));
        }
    }
}