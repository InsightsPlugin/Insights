package dev.frankheijden.insights.api.config.limits;

import dev.frankheijden.insights.api.config.parser.YamlParseException;
import dev.frankheijden.insights.api.config.parser.YamlParser;
import org.bukkit.Material;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Set;

public class PermissionLimit extends Limit {

    private final Map<Material, Integer> limitMap;

    protected PermissionLimit(Info info, Map<Material, Integer> limitMap) {
        super(LimitType.PERMISSION, info);
        this.limitMap = limitMap;
    }

    /**
     * Parses a PermissionLimit.
     */
    public static PermissionLimit parse(YamlParser parser, Info info) throws YamlParseException {
        Map<Material, Integer> limitMap = new EnumMap<>(Material.class);
        for (String key : parser.getKeys("limit.materials")) {
            String fullKey = "limit.materials." + key;
            Material material = parser.checkEnum(fullKey, key, Material.class, null, "material");
            int limit = parser.getInt(fullKey, -1, 0, Integer.MAX_VALUE);
            limitMap.put(material, limit);
        }

        return new PermissionLimit(info, limitMap);
    }

    public Map<Material, Integer> getLimitMap() {
        return limitMap;
    }

    /**
     * Returns the list of materials that are associated to the given material.
     * For PermissionLimit's, a block is not associated to any other block (individual limit).
     * If the given material is not in this limit, an empty set is returned.
     */
    @Override
    public Set<Material> getMaterials(Material m) {
        return limitMap.containsKey(m) ? Collections.singleton(m) : Collections.emptySet();
    }
}
