package dev.frankheijden.insights.api.config.limits;

import dev.frankheijden.insights.api.config.parser.YamlParseException;
import dev.frankheijden.insights.api.config.parser.YamlParser;
import dev.frankheijden.insights.api.reflection.RTileEntityTypes;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import java.util.Collections;
import java.util.Set;

public class TileLimit extends Limit {

    protected TileLimit(Info info) {
        super(LimitType.TILE, info);
    }

    /**
     * Parses a TileLimit.
     */
    public static TileLimit parse(YamlParser parser, Info info) throws YamlParseException {
        return new TileLimit(info);
    }

    @Override
    public Set<Material> getMaterials() {
        return RTileEntityTypes.getTileEntityMaterials();
    }

    @Override
    public Set<EntityType> getEntities() {
        return Collections.emptySet();
    }
}
