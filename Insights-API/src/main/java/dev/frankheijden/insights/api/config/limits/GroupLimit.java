package dev.frankheijden.insights.api.config.limits;

import dev.frankheijden.insights.api.concurrent.ScanOptions;
import dev.frankheijden.insights.api.config.parser.YamlParseException;
import dev.frankheijden.insights.api.config.parser.YamlParser;
import dev.frankheijden.insights.api.objects.wrappers.ScanObject;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public class GroupLimit extends Limit {

    private final String name;
    private final int limit;
    private final Set<Material> materials;
    private final Set<EntityType> entities;
    private final Set<ScanObject<?>> scanObjects;
    private final ScanOptions scanOptions;

    protected GroupLimit(Info info, String name, int limit, Set<Material> materials, Set<EntityType> entities) {
        super(LimitType.GROUP, info);
        this.name = name;
        this.limit = limit;
        this.materials = Collections.unmodifiableSet(materials);
        this.entities = Collections.unmodifiableSet(entities);
        this.scanObjects = Collections.unmodifiableSet(ScanObject.of(materials, entities));
        this.scanOptions = determineScanOptions();
    }

    /**
     * Parses a GroupLimit.
     */
    public static GroupLimit parse(YamlParser parser, Info info) throws YamlParseException {
        String name = parser.getString("limit.name", null, true);
        int limit = parser.getInt("limit.limit", -1, 0, Integer.MAX_VALUE);
        boolean regex = parser.getBoolean("limit.regex", false, false);
        List<Material> materials = regex
                ? parser.getRegexEnums("limit.materials", Material.class)
                : parser.getEnums("limit.materials", Material.class, "material");
        List<EntityType> entities = regex
                ? parser.getRegexEnums("limit.entities", EntityType.class)
                : parser.getEnums("limit.entities", EntityType.class, "entity");
        return new GroupLimit(
                info,
                name,
                limit,
                materials.isEmpty() ? Collections.emptySet() : EnumSet.copyOf(materials),
                entities.isEmpty() ? Collections.emptySet() : EnumSet.copyOf(entities)
        );
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
        return new LimitInfo(name, limit);
    }

    @Override
    public Set<Material> getMaterials() {
        return materials;
    }

    @Override
    public Set<EntityType> getEntities() {
        return entities;
    }

    @Override
    public Set<? extends ScanObject<?>> getScanObjects() {
        return scanObjects;
    }

    @Override
    public ScanOptions getScanOptions() {
        return scanOptions;
    }
}
