package net.frankheijden.insights.api.entities;

import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class ScanResult implements Iterable<Map.Entry<String, Integer>> {
    private TreeMap<String, Integer> counts;

    /**
     * Initializes ScanResult with empty map
     */
    public ScanResult() {
        this.counts = new TreeMap<>();
    }

    /**
     * Initializes ScanResult with pre-initialised items.
     * All entries will be set to 0. This is for "Custom"
     * scan, to notify user item count was 0.
     *
     * @param materials Scan materials
     * @param entityTypes Scan entities
     */
    public ScanResult(List<Material> materials, List<EntityType> entityTypes) {
        this.counts = new TreeMap<>();

        if (materials != null) {
            for (Material material : materials) {
                counts.put(material.name(), 0);
            }
        }

        if (entityTypes != null) {
            for (EntityType entityType : entityTypes) {
                counts.put(entityType.name(), 0);
            }
        }
    }

    public void increment(Material material) {
        increment(material.name());
    }

    public void increment(Entity entity) {
        increment(entity.getType().name());
    }

    public void increment(String key) {
        counts.merge(key, 1, Integer::sum);
    }

    public TreeMap<String, Integer> getCounts() {
        return counts;
    }

    public int getSize() {
        return counts.size();
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public Iterator<Map.Entry<String, Integer>> iterator() {
        return counts.entrySet().iterator();
    }
}
