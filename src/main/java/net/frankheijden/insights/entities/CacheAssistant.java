package net.frankheijden.insights.entities;

import net.frankheijden.insights.interfaces.Selectable;
import net.frankheijden.insights.managers.CacheManager;

public abstract class CacheAssistant implements Selectable {

    private static final CacheManager cacheManager = CacheManager.getInstance();
    private final String name;
    private final String areaName;

    public CacheAssistant(String name, String areaName) {
        this.name = name;
        this.areaName = areaName;
    }

    public String getName() {
        return name;
    }

    public String getAreaName() {
        return areaName;
    }

    public CacheManager getCacheManager() {
        return cacheManager;
    }
}
