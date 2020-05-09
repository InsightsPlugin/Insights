package net.frankheijden.insights.entities;

import net.frankheijden.insights.interfaces.Selectable;
import net.frankheijden.insights.managers.CacheManager;

import java.util.Objects;

public abstract class CacheAssistant implements Selectable {

    private static final CacheManager cacheManager = CacheManager.getInstance();
    private final String name;
    private final String areaName;
    private final String version;

    public CacheAssistant(String name, String areaName) {
        this(name, areaName, "1.0.0");
    }

    public CacheAssistant(String name, String areaName, String version) {
        this.name = name;
        this.areaName = areaName;
        this.version = version;
    }

    public String getName() {
        return name;
    }

    public String getAreaName() {
        return areaName;
    }

    public String getVersion() {
        return version;
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
                areaName.equals(assistant.areaName) &&
                version.equals(assistant.version);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, areaName, version);
    }
}
