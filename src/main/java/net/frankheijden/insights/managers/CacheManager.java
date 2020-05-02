package net.frankheijden.insights.managers;

import net.frankheijden.insights.entities.*;

import java.util.*;

public class CacheManager {

    private static CacheManager instance;

    private final Set<Cache> caches;
    private final Map<Selection, ScanCache> cacheMap;

    public CacheManager() {
        instance = this;
        caches = new HashSet<>();
        cacheMap = new HashMap<>();
    }

    public static CacheManager getInstance() {
        return instance;
    }

    public void addCache(Cache cache) {
        this.caches.add(cache);
    }

    public ScanCache getCache(Selection selection) {
        ScanCache cache = cacheMap.get(selection);
        if (cache == null) return new ScanCache(selection);
        return cache;
    }

    public void updateCache(ScanCache scanCache) {
        cacheMap.put(scanCache.getSelection(), scanCache);
    }
}
