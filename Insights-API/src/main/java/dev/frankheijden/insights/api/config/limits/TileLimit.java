package dev.frankheijden.insights.api.config.limits;

import dev.frankheijden.insights.api.config.parser.YamlParseException;
import dev.frankheijden.insights.api.config.parser.YamlParser;
import dev.frankheijden.insights.api.reflection.RTileEntityTypes;
import org.bukkit.Material;
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

    /**
     * Returns the list of materials that are associated to the given material.
     * For TileLimit's, a block is associated to all other tiles.
     * If the given material is not a Tile, an empty set is returned.
     */
    @Override
    public Set<Material> getMaterials(Material m) {
        return RTileEntityTypes.isTileEntity(m) ? RTileEntityTypes.getTileEntityMaterials() : Collections.emptySet();
    }
}
