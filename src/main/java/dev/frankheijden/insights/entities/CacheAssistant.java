package dev.frankheijden.insights.entities;

import dev.frankheijden.insights.interfaces.Selectable;
import dev.frankheijden.insights.managers.CacheManager;

import java.util.Objects;

public abstract class CacheAssistant implements Selectable {

    private static final CacheManager cacheManager = CacheManager.getInstance();
    private final String pluginName;
    private final String name;
    private final String areaName;
    private final String version;

    public CacheAssistant(String pluginName, String name, String areaName, String version) {
        this.pluginName = pluginName;
        this.name = name;
        this.areaName = areaName;
        this.version = version;
    }

    public String getPluginName() {
        return pluginName;
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
