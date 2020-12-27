package net.frankheijden.insights.managers;

import net.frankheijden.insights.Insights;
import net.frankheijden.insights.entities.*;
import net.frankheijden.insights.entities.Error;
import org.bukkit.*;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CacheManager {

    private static final Insights plugin = Insights.getInstance();
    private static CacheManager instance;

    private Set<CacheAssistant> addonCacheAssistants;
    private final Set<CacheAssistant> cacheAssistants;
    private final Map<String, ScanCache> caches;

    public CacheManager() {
        instance = this;
        this.addonCacheAssistants = new HashSet<>();
        this.cacheAssistants = new HashSet<>();
        this.caches = new HashMap<>();
    }

    public static CacheManager getInstance() {
        return instance;
    }

    public void unregisterAllAddons() {
        this.cacheAssistants.removeAll(addonCacheAssistants);
        this.addonCacheAssistants = new HashSet<>();
    }

    public void registerAllAddons(List<Error> errors, Set<CacheAssistant> cacheAssistants) {
        for (CacheAssistant c : cacheAssistants) {
            if (plugin.isAvailable(c.getPluginName())) {
                addCacheAssistant(c);
            } else {
                errors.add(new AddonError("Error while registering addon: plugin " + c.getName() + " is not enabled!"));
            }
        }
        this.addonCacheAssistants = cacheAssistants;
    }

    public void addCacheAssistant(CacheAssistant cache) {
        this.cacheAssistants.add(cache);
        Insights.logger.info("Successfully registered addon " + cache.getName() + " v" + cache.getVersion() + "!");
    }

    public Set<CacheAssistant> getLoadedAddons() {
        return this.cacheAssistants;
    }

    public boolean hasSelections(Location location) {
        return getSelections(location).count() != 0;
    }

    public Optional<ScanCache> getMaxCountCache(Location location, String what) {
        return this.getCache(location)
                .max(Comparator.comparingInt(c -> c.getCount(what)));
    }

    public Stream<Area> getSelections(Location location) {
        return cacheAssistants.stream()
                .map(c -> c.getArea(location))
                .filter(Objects::nonNull);
    }

    public Stream<ScanCache> getCache(Location location) {
        return getSelections(location)
                .map(this::getCache)
                .filter(Objects::nonNull);
    }

    public ScanCache getCache(Area area) {
        return caches.get(area.getId());
    }

    public ScanCache getCache(String key) {
        return caches.get(key);
    }

    public void updateCache(Location location, Material from, Material to) {
        updateCache(location, from.name(), to.name());
    }

    public void updateCache(Location location, String from, String to) {
        if (from.equals(to)) return;
        getCache(location).forEach(c -> {
            c.updateCache(from, -1);
            c.updateCache(to, 1);
        });
    }

    public boolean updateCache(String id, String what, int d) {
        ScanCache cache = getCache(id);
        if (cache == null) return true;
        cache.updateCache(what, d);
        updateCache(cache);
        return false;
    }

    public void updateCache(ScanCache scanCache) {
        caches.put(scanCache.getSelectionEntity().getId(), scanCache);
    }

    public boolean deleteCache(ScanCache scanCache) {
        return deleteCache(scanCache.getSelectionEntity());
    }

    public boolean deleteCache(Area area) {
        return caches.remove(area.getId()) != null;
    }

    public boolean deleteCache(String id) {
        return caches.remove(id) != null;
    }

    /**
     * Method which updates the cache at a specific location and returns
     * a set of selections which need to be scanned.
     * @param location The location to be used
     * @param what The cache variable which needs to be updated
     * @param d The difference for the cache variable
     * @return A set of selections which need to be scanned
     */
    public Set<Area> updateCache(Location location, String what, int d) {
        return this.getSelections(location)
                .filter(area -> updateCache(area.getId(), what, d))
                .collect(Collectors.toSet());
    }
}
