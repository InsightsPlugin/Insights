package net.frankheijden.insights.entities;

import net.frankheijden.insights.interfaces.Selectable;
import net.frankheijden.insights.managers.CacheManager;

public abstract class CacheAssistant implements Selectable {

    private final CacheManager cacheManager = CacheManager.getInstance();

    public void initialise() {
        cacheManager.addCacheAssistant(this);
    }

    public CacheManager getCacheManager() {
        return cacheManager;
    }
}
