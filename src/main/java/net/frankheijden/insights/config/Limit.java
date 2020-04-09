package net.frankheijden.insights.config;

import java.util.List;

public class Limit {

    private String name;
    private String permission;
    private int limit;
    private List<String> materials;
    private List<String> entities;

    public Limit(String name, String permission, int limit, List<String> materials, List<String> entities) {
        this.name = name;
        this.permission = permission;
        this.limit = limit;
        this.materials = materials;
        this.entities = entities;
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

    public List<String> getMaterials() {
        return materials;
    }

    public List<String> getEntities() {
        return entities;
    }
}
