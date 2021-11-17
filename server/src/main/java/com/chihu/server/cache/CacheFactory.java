package com.chihu.server.cache;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import java.time.Duration;

public class CacheFactory {
    private static final int INITIAL_CAPACITY = 100;
    private static final long MAX_CAPACITY = 1000L;
    private static final long CACHE_TTL_IN_DAYS = 7;

    public static <K, V> Cache<K, V> createCacheInstance() {
        return CacheBuilder.newBuilder()
                .initialCapacity(INITIAL_CAPACITY)
                .maximumSize(MAX_CAPACITY)
                .expireAfterWrite(Duration.ofDays(CACHE_TTL_IN_DAYS))
                .build();
    }

    public static <K, V> Cache<K, V> createCacheInstance(Duration ttl, long maxSize) {
        return CacheBuilder.newBuilder()
                .initialCapacity(INITIAL_CAPACITY)
                .maximumSize(maxSize)
                .expireAfterWrite(ttl)
                .build();
    }
}
