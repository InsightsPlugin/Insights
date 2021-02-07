package dev.frankheijden.insights.api.concurrent.storage;

import dev.frankheijden.insights.api.config.limits.Limit;
import dev.frankheijden.insights.api.config.limits.LimitType;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import java.util.concurrent.ConcurrentHashMap;

public class DistributionStorage {

    private final Distribution<Material> materials;
    private final Distribution<EntityType> entities;

    public DistributionStorage() {
        this(new Distribution<>(new ConcurrentHashMap<>()), new Distribution<>(new ConcurrentHashMap<>()));
    }

    public DistributionStorage(Distribution<Material> materials, Distribution<EntityType> entities) {
        this.materials = materials.copy(new ConcurrentHashMap<>());
        this.entities = entities.copy(new ConcurrentHashMap<>());
    }

    public Distribution<Material> materials() {
        return materials;
    }

    public Distribution<EntityType> entities() {
        return entities;
    }

    protected int count(Limit limit) {
        return materials.count(limit.getMaterials()) + entities.count(limit.getEntities());
    }

    public int count(Limit limit, Material material) {
        return limit.getType() == LimitType.PERMISSION ? materials.count(material) : count(limit);
    }

    public int count(Limit limit, EntityType entity) {
        return limit.getType() == LimitType.PERMISSION ? entities.count(entity) : count(limit);
    }

    /**
     * Merges the current instance with another distribution.
     * Note: the merged values will be in the target.
     */
    public void merge(DistributionStorage target) {
        target.materials.merge(materials);
        target.entities.merge(entities);
    }
}
