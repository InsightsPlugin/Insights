package net.frankheijden.insights.entities;

import net.frankheijden.insights.interfaces.Selectable;
import net.frankheijden.insights.managers.CacheManager;
import org.bukkit.Location;

public abstract class Cache implements Selectable {

    private final CacheManager cacheManager = CacheManager.getInstance();

    public void initialise() {
        cacheManager.addCache(this);
    }

    public void updateCache(Location location, String name, int d) {
        updateCache(getSelection(location), name, d);
    }

    private void updateCache(Selection selection, String what, int d) {
        ScanCache cache = cacheManager.getCache(selection);
        cache.updateCache(what, d);
        cacheManager.updateCache(cache);
    }
}
