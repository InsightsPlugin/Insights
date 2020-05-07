package net.frankheijden.insights.entities;

import net.frankheijden.insights.interfaces.Selectable;
import net.frankheijden.insights.managers.CacheManager;

public abstract class CacheAssistant implements Selectable {

    private static final CacheManager cacheManager = CacheManager.getInstance();
    private final String name;

    public CacheAssistant(String name) {
        this.name = name;
    }

    public void initialise() {
        cacheManager.addCacheAssistant(this);
    }

    public String getName() {
        return name;
    }

    public CacheManager getCacheManager() {
        return cacheManager;
    }
}
