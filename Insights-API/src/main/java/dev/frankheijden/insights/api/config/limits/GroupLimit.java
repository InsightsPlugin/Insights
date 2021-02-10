package dev.frankheijden.insights.api.config.limits;

import dev.frankheijden.insights.api.config.parser.YamlParseException;
import dev.frankheijden.insights.api.config.parser.YamlParser;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public class GroupLimit extends Limit {

    private final Set<Material> materials;
    private final Set<EntityType> entities;
    private final int limit;

    protected GroupLimit(Info info, Set<Material> materials, Set<EntityType> entities, int limit) {
        super(LimitType.GROUP, info);
        this.materials = Collections.unmodifiableSet(materials);
        this.entities = Collections.unmodifiableSet(entities);
        this.limit = limit;
    }

    /**
     * Parses a GroupLimit.
     */
    public static GroupLimit parse(YamlParser parser, Info info) throws YamlParseException {
        boolean regex = parser.getBoolean("limit.regex", false, false);
        List<Material> materials = regex
                ? parser.getRegexEnums("limit.materials", Material.class)
                : parser.getEnums("limit.materials", Material.class, "material");
        List<EntityType> entities = regex
                ? parser.getRegexEnums("limit.entities", EntityType.class)
                : parser.getEnums("limit.entities", EntityType.class, "entity");
        int limit = parser.getInt("limit.limit", -1, 0, Integer.MAX_VALUE);
        return new GroupLimit(
                info,
                materials.isEmpty() ? Collections.emptySet() : EnumSet.copyOf(materials),
                entities.isEmpty() ? Collections.emptySet() : EnumSet.copyOf(entities),
                limit
        );
    }

    @Override
    public int getLimit(Material m) {
        return limit;
    }

    @Override
    public int getLimit(EntityType e) {
        return limit;
    }

    @Override
    public Set<Material> getMaterials() {
        return materials;
    }

    @Override
    public Set<EntityType> getEntities() {
        return entities;
    }
}
