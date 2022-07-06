package dev.frankheijden.insights.api.config;

import static java.util.Comparator.comparingInt;

import dev.frankheijden.insights.api.InsightsPlugin;
import dev.frankheijden.insights.api.concurrent.storage.Storage;
import dev.frankheijden.insights.api.config.limits.Limit;
import dev.frankheijden.insights.api.config.limits.LimitInfo;
import dev.frankheijden.insights.api.objects.wrappers.ScanObject;
import dev.frankheijden.insights.api.region.Region;
import dev.frankheijden.insights.api.util.Pair;
import dev.frankheijden.insights.api.util.Triplet;
import dev.frankheijden.insights.api.utils.SetUtils;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

public class Limits {

    private final InsightsPlugin plugin;
    private final List<Limit> limits;
    private final Map<String, Limit> limitsByFileName;
    private final Map<Material, TreeSet<Limit>> materialLimits;
    private final Map<EntityType, TreeSet<Limit>> entityLimits;

    /**
     * Constructs a new Limits object, holding a data structure for fast lookup of limits.
     */
    public Limits(InsightsPlugin plugin) {
        this.plugin = plugin;
        limits = new ArrayList<>();
        limitsByFileName = new ConcurrentHashMap<>();
        materialLimits = new EnumMap<>(Material.class);
        entityLimits = new EnumMap<>(EntityType.class);
    }

    /**
     * Adds the given limit to the data structure.
     */
    public void addLimit(Limit limit) {
        this.limits.add(limit);
        this.limitsByFileName.put(limit.file().getName(), limit);
        for (Material m : limit.materials()) {
            materialLimits.computeIfAbsent(m, k -> new TreeSet<>(
                    comparingInt(l -> l.limitInfo(k).limit())
            )).add(limit);
        }
        for (EntityType e : limit.entities()) {
            entityLimits.computeIfAbsent(e, k -> new TreeSet<>(
                    comparingInt(l -> l.limitInfo(k).limit())
            )).add(limit);
        }
    }

    public List<Limit> limits() {
        return new ArrayList<>(limits);
    }

    /**
     * Attempts to find the smallest limit (least amount of placeable blocks).
     */
    public @Nullable Triplet<Region, Limit, Storage> smallestLimit(
            Player player,
            Collection<? extends Region> regions,
            ScanObject<?> item,
            int delta
    ) {
        Pair<Triplet<Region, Limit, Storage>, Long> smallestLimit = null;
        for (Region region : regions) {
            LimitEnvironment env = new LimitEnvironment(player, Collections.singletonList(region));
            Limit limit = plugin.limits().firstLimit(item, env);
            if (limit == null) continue;

            Storage storage = plugin.regionManager().regionStorage().get(region);
            if (storage == null) continue;

            LimitInfo limitInfo = limit.limitInfo(item);
            long count = storage.count(limit, item) + delta;
            long left = limitInfo.limit() - count;
            if (smallestLimit == null || left < smallestLimit.b()) {
                smallestLimit = new Pair<>(new Triplet<>(region, limit, storage), left);
            }
        }
        return smallestLimit == null ? null : smallestLimit.a();
    }

    /**
     * Retrieves the first limit (sorted ascending on limit, such that the smallest limit is applied).
     * Item must be of type Material or EntityType.
     */
    public @Nullable Limit firstLimit(ScanObject<?> item, Predicate<Limit> limitPredicate) {
        return switch (item.getType()) {
            case MATERIAL -> firstLimit((Material) item.getObject(), limitPredicate);
            case ENTITY -> firstLimit((EntityType) item.getObject(), limitPredicate);
        };
    }

    public @Nullable Limit firstLimit(Material material, Predicate<Limit> limitPredicate) {
        return firstLimit(materialLimits.get(material), limitPredicate);
    }

    public @Nullable Limit firstLimit(EntityType entity, Predicate<Limit> limitPredicate) {
        return firstLimit(entityLimits.get(entity), limitPredicate);
    }

    /**
     * Retrieves the first limit (sorted ascending on limit, such that the smallest limit is applied).
     */
    public @Nullable Limit firstLimit(Set<Limit> limits, Predicate<Limit> limitPredicate) {
        plugin.metricsManager().getLimitMetric().increment();
        return limits == null ? null : SetUtils.findFirst(limits, limitPredicate);
    }

    /**
     * Retrieves the limit by their filename.
     */
    public Optional<Limit> getLimitByFileName(String fileName) {
        return Optional.ofNullable(limitsByFileName.get(fileName));
    }

    /**
     * Lists the filenames of all limits.
     */
    public Set<String> getLimitFileNames() {
        return new HashSet<>(limitsByFileName.keySet());
    }
}
