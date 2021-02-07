package dev.frankheijden.insights.api.config.limits;

import dev.frankheijden.insights.api.config.parser.YamlParseException;
import dev.frankheijden.insights.api.config.parser.YamlParser;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Set;

public class PermissionLimit extends Limit {

    private final Map<Material, Integer> materials;
    private final Map<EntityType, Integer> entities;

    protected PermissionLimit(Info info, Map<Material, Integer> materials, Map<EntityType, Integer> entities) {
        super(LimitType.PERMISSION, info);
        this.materials = Collections.unmodifiableMap(materials);
        this.entities = Collections.unmodifiableMap(entities);
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
    public Set<Material> getMaterials() {
        return materials.keySet();
    }

    public Set<EntityType> getEntities() {
        return entities.keySet();
    }
}
