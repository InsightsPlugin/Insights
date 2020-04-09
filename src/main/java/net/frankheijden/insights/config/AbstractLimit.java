package net.frankheijden.insights.config;

import java.util.Map;
import java.util.Set;

public abstract class AbstractLimit implements Limitable {

    private String name;
    private String permission;
    private Map<String, Integer> materials;
    private Map<String, Integer> entities;

    public AbstractLimit(String name, String permission) {
        this.name = name;
        this.permission = permission;
        this.materials = null;
        this.entities = null;
    }

    public String getName() {
        return name;
    }

    public String getPermission() {
        return permission;
    }

    public Set<String> getMaterials() {
        return materials.keySet();
    }

    public void setMaterials(Map<String, Integer> materials) {
        this.materials = materials;
    }

    public Set<String> getEntities() {
        return entities.keySet();
    }

    public void setEntities(Map<String, Integer> entities) {
        this.entities = entities;
    }

    @Override
    public Integer getLimit(String str) {
        Integer mLimit = materials.get(str);
        if (mLimit != null) return mLimit;
        return entities.get(str);
    }
}
