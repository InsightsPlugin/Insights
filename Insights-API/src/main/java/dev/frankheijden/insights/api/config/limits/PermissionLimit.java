package dev.frankheijden.insights.api.config.limits;

import dev.frankheijden.insights.api.concurrent.ScanOptions;
import dev.frankheijden.insights.api.config.parser.YamlParseException;
import dev.frankheijden.insights.api.config.parser.YamlParser;
import dev.frankheijden.insights.api.objects.wrappers.ScanObject;
import dev.frankheijden.insights.api.utils.EnumUtils;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Set;

public class PermissionLimit extends Limit {

    private final Map<Material, Integer> materials;
    private final Map<EntityType, Integer> entities;
    private final Set<ScanObject<?>> scanObjects;
    private final ScanOptions scanOptions;

    protected PermissionLimit(Info info, Map<Material, Integer> materials, Map<EntityType, Integer> entities) {
        super(LimitType.PERMISSION, info);
        this.materials = Collections.unmodifiableMap(materials);
        this.entities = Collections.unmodifiableMap(entities);
        this.scanObjects = Collections.unmodifiableSet(ScanObject.of(materials.keySet(), entities.keySet()));
        this.scanOptions = determineScanOptions();
    }

    /**
     * Parses a PermissionLimit.
     */
    public static PermissionLimit parse(YamlParser parser, Info info) throws YamlParseException {
        Map<Material, Integer> materials = new EnumMap<>(Material.class);
        for (String key : parser.getKeys("limit.materials")) {
            String fullKey = "limit.materials." + key;
            Material material = parser.checkEnum(fullKey, key, Material.class, null, "material");
            int limit = parser.getInt(fullKey, -1, 0, Integer.MAX_VALUE);
            materials.put(material, limit);
        }

        Map<EntityType, Integer> entities = new EnumMap<>(EntityType.class);
        for (String key : parser.getKeys("limit.entities")) {
            String fullKey = "limit.entities." + key;
            EntityType entity = parser.checkEnum(fullKey, key, EntityType.class, null, "entity");
            int limit = parser.getInt(fullKey, -1, 0, Integer.MAX_VALUE);
            entities.put(entity, limit);
        }

        return new PermissionLimit(info, materials, entities);
    }

    @Override
    public LimitInfo limitInfo(Material m) {
        return new LimitInfo(EnumUtils.pretty(m), materials.getOrDefault(m, -1));
    }

    @Override
    public LimitInfo limitInfo(EntityType e) {
        return new LimitInfo(EnumUtils.pretty(e), entities.getOrDefault(e, -1));
    }

    @Override
    public Set<Material> materials() {
        return materials.keySet();
    }

    public Set<EntityType> entities() {
        return entities.keySet();
    }

    @Override
    public Set<? extends ScanObject<?>> scanObjects() {
        return scanObjects;
    }

    @Override
    public ScanOptions getScanOptions() {
        return scanOptions;
    }
}
