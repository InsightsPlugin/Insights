package dev.frankheijden.insights.api.config.limits;

import dev.frankheijden.insights.api.config.parser.YamlParseException;
import dev.frankheijden.insights.api.config.parser.YamlParser;
import dev.frankheijden.insights.api.reflection.RTileEntityTypes;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import java.util.Collections;
import java.util.Set;

public class TileLimit extends Limit {

    private final int limit;

    protected TileLimit(Info info, int limit) {
        super(LimitType.TILE, info);
        this.limit = limit;
    }

    /**
     * Parses a TileLimit.
     */
    public static TileLimit parse(YamlParser parser, Info info) throws YamlParseException {
        int limit = parser.getInt("limit.limit", -1, 0, Integer.MAX_VALUE);
        return new TileLimit(info, limit);
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
        return RTileEntityTypes.getTileEntityMaterials();
    }

    @Override
    public Set<EntityType> getEntities() {
        return Collections.emptySet();
    }
}
