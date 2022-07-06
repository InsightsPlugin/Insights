package dev.frankheijden.insights.api.config.limits;

import dev.frankheijden.insights.api.concurrent.ScanOptions;
import dev.frankheijden.insights.api.config.parser.YamlParseException;
import dev.frankheijden.insights.api.config.parser.YamlParser;
import dev.frankheijden.insights.api.objects.wrappers.ScanObject;
import dev.frankheijden.insights.api.reflection.RTileEntityTypes;
import dev.frankheijden.insights.api.utils.EnumUtils;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class TileLimit extends Limit {

    private final String name;
    private final int limit;
    private final Set<Material> effectiveMaterials;
    private final Set<? extends ScanObject<?>> effectiveScanObjects;
    private final ScanOptions scanOptions;

    protected TileLimit(Info info, String name, int limit, Set<Material> excludedMaterials) {
        super(LimitType.TILE, info);
        this.name = name;
        this.limit = limit;
        this.effectiveMaterials = EnumUtils.difference(RTileEntityTypes.getTileEntityMaterials(), excludedMaterials);

        Set<ScanObject<?>> tileEntityScanObjects = new HashSet<>(RTileEntityTypes.getTileEntities());
        tileEntityScanObjects.removeAll(ScanObject.of(excludedMaterials));
        this.effectiveScanObjects = Collections.unmodifiableSet(tileEntityScanObjects);
        this.scanOptions = determineScanOptions();
    }

    /**
     * Parses a TileLimit.
     */
    public static TileLimit parse(YamlParser parser, Info info) throws YamlParseException {
        String name = parser.getString("limit.name", null, true);
        int limit = parser.getInt("limit.limit", -1, 0, Integer.MAX_VALUE);
        Set<Material> excludedMaterials = new HashSet<>(parser.getEnums("limit.excluded-materials", Material.class));
        return new TileLimit(info, name, limit, excludedMaterials);
    }

    public String getName() {
        return name;
    }

    public int limit() {
        return limit;
    }

    @Override
    public LimitInfo limitInfo(Material m) {
        return new LimitInfo(name, limit);
    }

    @Override
    public LimitInfo limitInfo(EntityType e) {
        return new LimitInfo(name, -1);
    }

    @Override
    public Set<Material> materials() {
        return effectiveMaterials;
    }

    @Override
    public Set<EntityType> entities() {
        return Collections.emptySet();
    }

    @Override
    public Set<? extends ScanObject<?>> scanObjects() {
        return effectiveScanObjects;
    }

    @Override
    public ScanOptions getScanOptions() {
        return scanOptions;
    }
}
