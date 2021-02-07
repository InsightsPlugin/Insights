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

    /**
     * Retrieves the distribution for given item.
     * Item must be of type Material or EntityType.
     */
    @SuppressWarnings("rawtypes")
    public Distribution distribution(Object item) {
        if (item instanceof Material) {
            return materials;
        } else if (item instanceof EntityType) {
            return entities;
        }
        throw new IllegalArgumentException("Item is of unsupported limit type '" + item.getClass() + "'");
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
     * Counts the distribution for given limit and item.
     * Item must be of type Material or EntityType.
     */
    public int count(Limit limit, Object item) {
        if (item instanceof Material) {
            return count(limit, (Material) item);
        } else if (item instanceof EntityType) {
            return count(limit, (EntityType) item);
        }
        throw new IllegalArgumentException("Item is of unsupported limit type '" + item.getClass() + "'");
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
