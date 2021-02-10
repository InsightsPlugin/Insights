package dev.frankheijden.insights.api.config.limits;

import dev.frankheijden.insights.api.config.parser.YamlParseException;
import dev.frankheijden.insights.api.config.parser.YamlParser;
import dev.frankheijden.insights.api.reflection.RTileEntityTypes;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import java.util.Collections;
import java.util.Set;

public class TileLimit extends Limit {

    private final String name;
    private final int limit;

    protected TileLimit(Info info, String name, int limit) {
        super(LimitType.TILE, info);
        this.name = name;
        this.limit = limit;
    }

    /**
     * Parses a TileLimit.
     */
    public static TileLimit parse(YamlParser parser, Info info) throws YamlParseException {
        String name = parser.getString("limit.name", null, true);
        int limit = parser.getInt("limit.limit", -1, 0, Integer.MAX_VALUE);
        return new TileLimit(info, name, limit);
    }

    public String getName() {
        return name;
    }

    public int getLimit() {
        return limit;
    }

    @Override
    public LimitInfo getLimit(Material m) {
        return new LimitInfo(name, limit);
    }

    @Override
    public LimitInfo getLimit(EntityType e) {
        return new LimitInfo(name, -1);
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
