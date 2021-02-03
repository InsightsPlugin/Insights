package dev.frankheijden.insights.api.config.limits;

import dev.frankheijden.insights.api.config.parser.YamlParseException;
import dev.frankheijden.insights.api.config.parser.YamlParser;
import org.bukkit.Material;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

public class GroupLimit extends Limit {

    private final Set<Material> materials;

    protected GroupLimit(Info info, Set<Material> materials) {
        super(LimitType.GROUP, info);
        this.materials = materials;
    }

    /**
     * Parses a GroupLimit.
     */
    public static GroupLimit parse(YamlParser parser, Info info) throws YamlParseException {
        Set<Material> materials = EnumSet.copyOf(parser.getEnums("limit.materials", Material.class, "material"));
        return new GroupLimit(info, materials);
    }

    public Set<Material> getMaterials() {
        return materials;
    }

    /**
     * Returns the list of materials that are associated to the given material.
     * For GroupLimit's, all blocks are associated to each other, acting as a single cluster.
     * If the given material is not in this limit, an empty set is returned.
     */
    @Override
    public Set<Material> getMaterials(Material m) {
        return materials.contains(m) ? materials : Collections.emptySet();
    }
}
