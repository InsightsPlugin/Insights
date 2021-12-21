package dev.frankheijden.insights.api.config;

import static java.util.Comparator.comparingInt;

import dev.frankheijden.insights.api.InsightsPlugin;
import dev.frankheijden.insights.api.config.limits.Limit;
import dev.frankheijden.insights.api.objects.wrappers.ScanObject;
import dev.frankheijden.insights.api.utils.SetUtils;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import java.util.ArrayList;
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

    private final List<Limit> limits;
    private final Map<String, Limit> limitsByFileName;
    private final Map<Material, TreeSet<Limit>> materialLimits;
    private final Map<EntityType, TreeSet<Limit>> entityLimits;

    /**
     * Constructs a new Limits object, holding a data structure for fast lookup of limits.
     */
    public Limits() {
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
        this.limitsByFileName.put(limit.getFile().getName(), limit);
        for (Material m : limit.getMaterials()) {
            materialLimits.computeIfAbsent(m, k -> new TreeSet<>(
                    comparingInt(l -> l.getLimit(k).getLimit())
            )).add(limit);
        }
        for (EntityType e : limit.getEntities()) {
            entityLimits.computeIfAbsent(e, k -> new TreeSet<>(
                    comparingInt(l -> l.getLimit(k).getLimit())
            )).add(limit);
        }
    }

    public List<Limit> getLimits() {
        return new ArrayList<>(limits);
    }

    /**
     * Retrieves the first limit (sorted ascending on limit, such that the smallest limit is applied).
     * Item must be of type Material or EntityType.
     */
    public Optional<Limit> getFirstLimit(ScanObject<?> item, Predicate<Limit> limitPredicate) {
        switch (item.getType()) {
            case MATERIAL: return getFirstLimit((Material) item.getObject(), limitPredicate);
            case ENTITY: return getFirstLimit((EntityType) item.getObject(), limitPredicate);
            default: throw new IllegalArgumentException("Item is of unsupported limit type '" + item.getClass() + "'");
        }
    }

    /**
     * Retrieves the first limit (sorted ascending on limit, such that the smallest limit is applied).
     */
    public Optional<Limit> getFirstLimit(Material material, Predicate<Limit> limitPredicate) {
        InsightsPlugin.getInstance().getMetricsManager().getLimitMetric().increment();
        Set<Limit> set = materialLimits.get(material);
        return set == null ? Optional.empty() : Optional.ofNullable(SetUtils.findFirst(set, limitPredicate));
    }

    /**
     * Retrieves the first limit (sorted ascending on limit, such that the smallest limit is applied).
     */
    public Optional<Limit> getFirstLimit(EntityType entity, Predicate<Limit> limitPredicate) {
        InsightsPlugin.getInstance().getMetricsManager().getLimitMetric().increment();
        Set<Limit> set = entityLimits.get(entity);
        return set == null ? Optional.empty() : Optional.ofNullable(SetUtils.findFirst(set, limitPredicate));
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
