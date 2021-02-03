package dev.frankheijden.insights.api.config.limits;

import dev.frankheijden.insights.api.config.parser.YamlParseException;
import dev.frankheijden.insights.api.config.parser.YamlParser;
import org.bukkit.Material;
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
}
