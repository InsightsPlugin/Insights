package dev.frankheijden.insights.config;

import dev.frankheijden.insights.utils.CaseInsensitiveHashSet;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

public class Limit {

    private final String name;
    private final String permission;
    private final int limit;
    private final Set<String> materials;
    private final Set<String> entities;

    public Limit(String name, String permission, int limit, Collection<? extends String> materials, Collection<? extends String> entities) {
        this.name = name;
        this.permission = permission;
        this.limit = limit;
        this.materials = materials == null ? Collections.emptySet() : new CaseInsensitiveHashSet(materials);
        this.entities = entities == null ? Collections.emptySet() : new CaseInsensitiveHashSet(entities);
    }

    public String getName() {
        return name;
    }

    public String getPermission() {
        return permission;
    }

    public int getLimit() {
        return limit;
    }

    public Set<String> getMaterials() {
        return materials;
    }

    public Set<String> getEntities() {
        return entities;
    }
}
