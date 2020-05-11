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
    private final Map<Selection, ScanCache> caches;

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
        Insights.logger.info("[Insights] Successfully registered addon " + cache.getName() + " v" + cache.getVersion() + "!");
    }

    public Set<CacheAssistant> getLoadedAddons() {
        return this.cacheAssistants;
    }

    public boolean hasSelections(Location location) {
        return getSelections(location).count() != 0;
    }

    public Optional<ScanCache> getMaxCountCache(Location location, String what) {
        return this.getSelections(location)
                .map(this::getCache)
                .filter(Objects::nonNull)
                .max(Comparator.comparingInt(c -> c.getCount(what)));
    }

    public Stream<SelectionEntity> getSelections(Location location) {
        return cacheAssistants.stream()
                .map(c -> SelectionEntity.from(c.getSelection(location), c))
                .filter(Objects::nonNull);
    }

    public ScanCache getCache(Selection selection) {
        return caches.get(selection);
    }

    public void updateCache(Location location, Material from, Material to) {
        updateCache(location, from.name(), to.name());
    }

    public void updateCache(Location location, String from, String to) {
        if (from.equals(to)) return;
        getSelections(location)
                .map(this::getCache)
                .filter(Objects::nonNull)
                .forEach(c -> {
                    c.updateCache(from, -1);
                    c.updateCache(to, 1);
                });
    }

    public boolean updateCache(Selection selection, String what, int d) {
        ScanCache cache = getCache(selection);
        if (cache == null) return true;
        cache.updateCache(what, d);
        updateCache(cache);
        return false;
    }

    public void updateCache(ScanCache scanCache) {
        caches.put(scanCache.getSelectionEntity(), scanCache);
    }

    /**
     * Method which updates the cache at a specific location and returns
     * a set of selections which need to be scanned.
     * @param location The location to be used
     * @param what The cache variable which needs to be updated
     * @param d The difference for the cache variable
     * @return A set of selections which need to be scanned
     */
    public Set<SelectionEntity> updateCache(Location location, String what, int d) {
        return this.getSelections(location)
                .filter(s -> updateCache(s, what, d))
                .collect(Collectors.toSet());
    }
}
