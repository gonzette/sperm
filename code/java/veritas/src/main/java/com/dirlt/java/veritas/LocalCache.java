package com.dirlt.java.veritas;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import java.util.concurrent.TimeUnit;

/**
 * Created with IntelliJ IDEA.
 * User: dirlt
 * Date: 12/8/12
 * Time: 2:40 AM
 * To change this template use File | Settings | File Templates.
 */
public class LocalCache {
    // singleton.
    private static Cache<String, Object> cache = null;
    private static LocalCache instance = null;

    public static void init(Configuration configuration) {
        instance = new LocalCache(configuration);
    }

    private LocalCache(Configuration configuration) {
        CacheBuilder builder = CacheBuilder.newBuilder();
        builder.expireAfterWrite(configuration.getCacheExpireTime(), TimeUnit.SECONDS);
        builder.maximumSize(configuration.getCacheMaxCapacity());
        builder.recordStats();
        cache = builder.build();
    }

    public static LocalCache getInstance() {
        return instance;
    }

    public Object get(String k) {
        return cache.getIfPresent(k);
    }

    public void set(String k, Object b) {
        cache.put(k, b);
    }

    public void clear() {
        cache.invalidateAll();
    }

    public String getStat() {
        return cache.stats().toString() + "\n";
    }
}
