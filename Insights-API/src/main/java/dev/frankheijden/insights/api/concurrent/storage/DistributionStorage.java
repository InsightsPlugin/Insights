package dev.frankheijden.insights.api.concurrent.storage;

import dev.frankheijden.insights.api.config.limits.Limit;
import dev.frankheijden.insights.api.config.limits.LimitType;
import dev.frankheijden.insights.api.objects.wrappers.ScanObject;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import java.util.HashSet;
import java.util.Set;
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
    public Distribution distribution(ScanObject<?> item) {
        switch (item.getType()) {
            case MATERIAL: return materials;
            case ENTITY: return entities;
            default: throw new IllegalArgumentException("Item is of unsupported ScanObject '" + item.getClass() + "'");
        }
    }

    /**
     * Returns the union of all material keys and entity keys.
     */
    public Set<ScanObject<?>> keys() {
        Set<Material> materialSet = materials.keys();
        Set<EntityType> entitySet = entities.keys();
        Set<ScanObject<?>> keys = new HashSet<>(materialSet.size() + entitySet.size());
        for (Material material : materialSet) {
            keys.add(ScanObject.of(material));
        }
        for (EntityType entity : entitySet) {
            keys.add(ScanObject.of(entity));
        }
        return keys;
    }

    public void modify(Material material, int amount) {
        materials.modify(material, amount);
    }

    public void modify(EntityType entityType, int amount) {
        entities.modify(entityType, amount);
    }

    /**
     * Modifies the distribution for a specific item with an amount.
     */
    public void modify(ScanObject<?> item, int amount) {
        switch (item.getType()) {
            case MATERIAL:
                modify((Material) item.getObject(), amount);
                break;
            case ENTITY:
                modify((EntityType) item.getObject(), amount);
                break;
            default: throw new IllegalArgumentException("Item is of unsupported ScanObject '" + item.getClass() + "'");
        }
    }

    protected int count(Limit limit) {
        return materials.count(limit.getMaterials()) + entities.count(limit.getEntities());
    }

    public int count(Material material) {
        return materials.count(material);
    }

    public int count(EntityType entityType) {
        return entities.count(entityType);
    }

    /**
     * Counts the distribution of a specific ScanObject.
     */
    public int count(ScanObject<?> item) {
        switch (item.getType()) {
            case MATERIAL: return count((Material) item.getObject());
            case ENTITY: return count((EntityType) item.getObject());
            default: throw new IllegalArgumentException("Item is of unsupported ScanObject '" + item.getClass() + "'");
        }
    }

    public int count(Limit limit, Material material) {
        return limit.getType() == LimitType.PERMISSION ? count(material) : count(limit);
    }

    public int count(Limit limit, EntityType entity) {
        return limit.getType() == LimitType.PERMISSION ? count(entity) : count(limit);
    }

    /**
     * Counts the distribution for given limit and item.
     * Item must be of type Material or EntityType.
     */
    public int count(Limit limit, ScanObject<?> item) {
        switch (item.getType()) {
            case MATERIAL: return count(limit, (Material) item.getObject());
            case ENTITY: return count(limit, (EntityType) item.getObject());
            default: throw new IllegalArgumentException("Item is of unsupported ScanObject '" + item.getClass() + "'");
        }
    }

    /**
     * Merges the current instance with another distribution.
     * Note: the merged values will be in the target.
     */
    public void mergeRight(DistributionStorage target) {
        this.materials.mergeRight(target.materials);
        this.entities.mergeRight(target.entities);
    }
}
