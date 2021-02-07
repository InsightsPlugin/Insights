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

    protected GroupLimit(Info info, Set<Material> materials, Set<EntityType> entities) {
        super(LimitType.GROUP, info);
        this.materials = Collections.unmodifiableSet(materials);
        this.entities = Collections.unmodifiableSet(entities);
    }

    /**
     * Parses a GroupLimit.
     */
    public static GroupLimit parse(YamlParser parser, Info info) throws YamlParseException {
        List<Material> materials = parser.getEnums("limit.materials", Material.class, "material");
        List<EntityType> entities = parser.getEnums("limit.entities", EntityType.class, "entity");
        return new GroupLimit(
                info,
                materials.isEmpty() ? Collections.emptySet() : EnumSet.copyOf(materials),
                entities.isEmpty() ? Collections.emptySet() : EnumSet.copyOf(entities)
        );
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
