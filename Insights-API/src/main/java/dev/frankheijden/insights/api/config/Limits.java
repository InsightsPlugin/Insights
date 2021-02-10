package dev.frankheijden.insights.api.config;

import static java.util.Comparator.comparingInt;

import dev.frankheijden.insights.api.config.limits.Limit;
import dev.frankheijden.insights.api.config.limits.TileLimit;
import dev.frankheijden.insights.api.utils.BlockUtils;
import dev.frankheijden.insights.api.utils.SetUtils;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Predicate;

public class Limits {

    private final List<Limit> limits;
    private final TreeSet<TileLimit> tileLimits;
    private final Map<Material, TreeSet<Limit>> materialLimits;
    private final Map<EntityType, TreeSet<Limit>> entityLimits;

    /**
     * Constructs a new Limits object, holding a data structure for fast lookup of limits.
     */
    public Limits() {
        limits = new ArrayList<>();
        tileLimits = new TreeSet<>(comparingInt(tileLimit -> tileLimit.getLimit(Material.AIR)));
        materialLimits = new EnumMap<>(Material.class);
        entityLimits = new EnumMap<>(EntityType.class);
    }

    /**
     * Adds the given limit to the data structure.
     */
    public void addLimit(Limit limit) {
        this.limits.add(limit);
        if (limit instanceof TileLimit) {
            this.tileLimits.add((TileLimit) limit);
        } else {
            for (Material m : limit.getMaterials()) {
                materialLimits.computeIfAbsent(m, k -> new TreeSet<>(comparingInt(l -> l.getLimit(m)))).add(limit);
            }
            for (EntityType e : limit.getEntities()) {
                entityLimits.computeIfAbsent(e, k -> new TreeSet<>(comparingInt(l -> l.getLimit(e)))).add(limit);
            }
        }
    }

    public List<Limit> getLimits() {
        return new ArrayList<>(limits);
    }

    public boolean hasLimit(Material material) {
        return getFirstLimit(material, limit -> true).isPresent();
    }

    /**
     * Retrieves the first limit (sorted ascending on limit, such that the smallest limit is applied).
     * Item must be of type Material or EntityType.
     */
    public Optional<Limit> getFirstLimit(Object item, Predicate<Limit> limitPredicate) {
        if (item instanceof Material) {
            return getFirstLimit((Material) item, limitPredicate);
        } else if (item instanceof EntityType) {
            return getFirstLimit((EntityType) item, limitPredicate);
        }
        throw new IllegalArgumentException("Item is of unsupported limit type '" + item.getClass() + "'");
    }

    /**
     * Retrieves the first limit (sorted ascending on limit, such that the smallest limit is applied).
     */
    public Optional<Limit> getFirstLimit(Material material, Predicate<Limit> limitPredicate) {
        if (BlockUtils.isTileEntity(material)) {
            TileLimit limit = SetUtils.findFirst(tileLimits, limitPredicate);
            if (limit != null) {
                return Optional.of(limit);
            }
        }

        Set<Limit> set = materialLimits.get(material);
        return set == null ? Optional.empty() : Optional.ofNullable(SetUtils.findFirst(set, limitPredicate));
    }

    /**
     * Retrieves the first limit (sorted ascending on limit, such that the smallest limit is applied).
     */
    public Optional<Limit> getFirstLimit(EntityType entity, Predicate<Limit> limitPredicate) {
        Set<Limit> set = entityLimits.get(entity);
        return set == null ? Optional.empty() : Optional.ofNullable(SetUtils.findFirst(set, limitPredicate));
    }
}
