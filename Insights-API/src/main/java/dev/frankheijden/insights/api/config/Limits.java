package dev.frankheijden.insights.api.config;

import dev.frankheijden.insights.api.config.limits.GroupLimit;
import dev.frankheijden.insights.api.config.limits.Limit;
import dev.frankheijden.insights.api.config.limits.PermissionLimit;
import dev.frankheijden.insights.api.config.limits.TileLimit;
import dev.frankheijden.insights.api.utils.BlockUtils;
import dev.frankheijden.insights.api.utils.SetUtils;
import org.bukkit.Material;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Predicate;

public class Limits {

    private static final Comparator<Limit> ascendingLimits = Comparator.comparingInt(Limit::getLimit);

    private final List<Limit> limits;
    private final TreeSet<TileLimit> tileLimits;
    private final Map<Material, TreeSet<Limit>> materialLimits;

    /**
     * Constructs a new Limits object, holding a data structure for fast lookup of limits.
     */
    public Limits() {
        limits = new ArrayList<>();
        tileLimits = new TreeSet<>(ascendingLimits);
        materialLimits = new EnumMap<>(Material.class);
    }

    /**
     * Adds the given limit to the data structure.
     */
    public void addLimit(Limit limit) {
        this.limits.add(limit);
        if (limit instanceof TileLimit) {
            this.tileLimits.add((TileLimit) limit);
        } else if (limit instanceof GroupLimit) {
            GroupLimit groupLimit = (GroupLimit) limit;
            for (Material m : groupLimit.getMaterials()) {
                materialLimits.computeIfAbsent(m, k -> new TreeSet<>(ascendingLimits)).add(groupLimit);
            }
        } else if (limit instanceof PermissionLimit) {
            PermissionLimit permissionLimit = (PermissionLimit) limit;
            for (Material m : permissionLimit.getLimitMap().keySet()) {
                materialLimits.computeIfAbsent(m, k -> new TreeSet<>(ascendingLimits)).add(permissionLimit);
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
}
