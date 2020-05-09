package net.frankheijden.insights.entities;

import net.frankheijden.insights.interfaces.Selectable;
import net.frankheijden.insights.managers.CacheManager;

import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CacheAssistant assistant = (CacheAssistant) o;
        return name.equals(assistant.name) &&
                areaName.equals(assistant.areaName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, areaName);
    }
}
