package net.frankheijden.insights.managers;

import net.frankheijden.insights.entities.*;
import org.bukkit.Location;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CacheManager {

    private static CacheManager instance;

    private final Set<CacheAssistant> cacheAssistants;
    private final Map<Selection, ScanCache> caches;

    public CacheManager() {
        instance = this;
        cacheAssistants = new HashSet<>();
        caches = new HashMap<>();
    }

    public static CacheManager getInstance() {
        return instance;
    }

    public void addCacheAssistant(CacheAssistant cache) {
        this.cacheAssistants.add(cache);
    }

    public boolean hasSelections(Location location) {
        return getSelections(location).count() != 0;
    }

    public Stream<Selection> getSelections(Location location) {
        return cacheAssistants.stream()
                .map(c -> c.getSelection(location))
                .filter(Objects::nonNull);
    }

    public ScanCache getCache(Selection selection) {
        return caches.get(selection);
    }

    public boolean updateCache(Selection selection, String what, int d) {
        ScanCache cache = getCache(selection);
        if (cache == null) return true;
        cache.updateCache(what, d);
        updateCache(cache);
        return false;
    }

    public void updateCache(ScanCache scanCache) {
        caches.put(scanCache.getSelection(), scanCache);
    }

    /**
     * Method which updates the cache at a specific location and returns
     * a set of selections which need to be scanned.
     * @param location The location to be used
     * @param what The cache variable which needs to be updated
     * @param d The difference for the cache variable
     * @return A set of selections which need to be scanned
     */
    public Set<Selection> updateCache(Location location, String what, int d) {
        return this.getSelections(location)
                .filter(s -> updateCache(s, what, d))
                .collect(Collectors.toSet());
    }
}
