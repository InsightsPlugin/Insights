package dev.frankheijden.insights.config;

import dev.frankheijden.insights.utils.CaseInsensitiveHashMap;

import java.util.Map;
import java.util.Set;

public abstract class AbstractLimit implements Limitable {

    private final String name;
    private final String permission;
    private CaseInsensitiveHashMap<Integer> materials = new CaseInsensitiveHashMap<>();
    private CaseInsensitiveHashMap<Integer> entities = new CaseInsensitiveHashMap<>();

    public AbstractLimit(String name, String permission) {
        this.name = name;
        this.permission = permission;
    }

    public String getName() {
        return name;
    }

    public String getPermission() {
        return permission;
    }

    public Set<String> getMaterials(String str) {
        return materials.keySet();
    }

    public void setMaterials(Map<String, Integer> materials) {
        this.materials = new CaseInsensitiveHashMap<>(materials);
    }

    public Set<String> getEntities(String str) {
        return entities.keySet();
    }

    public void setEntities(Map<String, Integer> entities) {
        this.entities = new CaseInsensitiveHashMap<>(entities);
    }

    @Override
    public Integer getLimit(String str) {
        Integer mLimit = materials.get(str);
        if (mLimit != null) return mLimit;
        return entities.get(str);
    }
}
