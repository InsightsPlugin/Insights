package net.frankheijden.insights.managers;

import net.frankheijden.insights.Insights;
import net.frankheijden.insights.entities.AddonError;
import net.frankheijden.insights.entities.Area;
import net.frankheijden.insights.entities.CacheAssistant;
import net.frankheijden.insights.entities.Error;
import net.frankheijden.insights.entities.ScanCache;
import org.bukkit.Location;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
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

    public List<Area> getSelections(Location location) {
        List<Area> areas = new ArrayList<>(cacheAssistants.size());
        for (CacheAssistant assistant : cacheAssistants) {
            Area area = assistant.getArea(location);
            if (area != null) {
                areas.add(area);
            }
        }
        return areas;
    }

    public ScanCache getCache(Area area) {
        return caches.get(area.getId());
    }

    public ScanCache getCache(String key) {
        return caches.get(key);
    }

    public void updateCache(ScanCache scanCache) {
        caches.put(scanCache.getSelectionEntity().getId(), scanCache);
    }

    /**
     * Updates the cache and returns whether or not the area needs scanning.
     */
    public boolean updateCache(String id, String what, int d) {
        ScanCache cache = getCache(id);
        if (cache == null) return true;
        cache.updateCache(what, d);
        updateCache(cache);
        return false;
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

    public CacheLocation newCacheLocation(Location loc) {
        return new CacheLocation(loc, getSelections(loc));
    }

    public class CacheLocation {

        private final Location loc;
        private final List<Area> areas;

        private CacheLocation(Location loc, List<Area> areas) {
            this.loc = loc;
            this.areas = areas;
        }

        public boolean isEmpty() {
            return areas.isEmpty();
        }

        public Stream<ScanCache> getCache() {
            return areas.stream()
                    .map(CacheManager.this::getCache)
                    .filter(Objects::nonNull);
        }

        public Optional<ScanCache> getMaxCountCache(String what) {
            return this.getCache()
                    .max(Comparator.comparingInt(c -> c.getCount(what)));
        }

        public void updateCache(Material from, Material to) {
            updateCache(from.name(), to.name());
        }

        public void updateCache(String from, String to) {
            if (from.equals(to)) return;
            getCache().forEach(c -> {
                c.updateCache(from, -1);
                c.updateCache(to, 1);
            });
        }

        /**
         * Method which updates the cache at a specific location's areas and returns
         * a set of selections which need to be scanned.
         * @param what The cache variable which needs to be updated
         * @param d The difference for the cache variable
         * @return A set of selections which need to be scanned
         */
        public List<Area> updateCache(String what, int d) {
            List<Area> scanAreas = new ArrayList<>(areas.size());
            for (Area area : areas) {
                if (CacheManager.this.updateCache(area.getId(), what, d)) {
                    scanAreas.add(area);
                }
            }
            return scanAreas;
        }
    }
}
