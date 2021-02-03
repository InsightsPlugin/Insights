package dev.frankheijden.insights.api.config.limits;

import dev.frankheijden.insights.api.config.parser.YamlParseException;
import dev.frankheijden.insights.api.config.parser.YamlParser;
import org.bukkit.Material;
import java.util.EnumMap;
import java.util.Map;

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
}
