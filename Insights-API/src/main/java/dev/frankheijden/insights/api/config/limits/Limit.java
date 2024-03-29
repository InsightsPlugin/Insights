package dev.frankheijden.insights.api.config.limits;

import dev.frankheijden.insights.api.concurrent.ScanOptions;
import dev.frankheijden.insights.api.config.parser.SensitiveYamlParser;
import dev.frankheijden.insights.api.config.parser.YamlParseException;
import dev.frankheijden.insights.api.config.parser.YamlParser;
import dev.frankheijden.insights.api.objects.wrappers.ScanObject;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import java.io.File;
import java.io.IOException;
import java.util.Set;

public abstract class Limit {

    private static final String LIMIT_SECTION = "limit";

    private final LimitType type;
    private final Info info;

    protected Limit(LimitType type, Info info) {
        this.type = type;
        this.info = info;
    }

    /**
     * Parses given file into a Limit.
     */
    public static Limit parse(File file) throws IOException, YamlParseException {
        YamlParser parser = SensitiveYamlParser.load(file);
        if (!parser.checkExists(LIMIT_SECTION)) {
            throw new LimitParseException("Section '" + LIMIT_SECTION + "' does not exist!");
        }

        LimitType type = parser.getEnum("limit.type", LimitType.class);
        if (type == null) throw new LimitParseException("Invalid limit type!");
        var bypassPermission = parser.getString("limit.bypass-permission", null, false);

        var worldWhitelist = parser.getBoolean("limit.settings.enabled-worlds.whitelist", false, false);
        Set<String> worlds = parser.getSet("limit.settings.enabled-worlds.worlds");

        var addonWhitelist = parser.getBoolean("limit.settings.enabled-addons.whitelist", false, false);
        Set<String> addons = parser.getSet("limit.settings.enabled-addons.addons");

        var disallowPlacement = parser.getBoolean("limit.settings.disallow-placement-outside-region", false, false);

        var settings = new LimitSettings(worlds, worldWhitelist, addons, addonWhitelist, disallowPlacement);
        var info = new Info(file, bypassPermission, settings);

        return switch (type) {
            case TILE -> TileLimit.parse(parser, info);
            case GROUP -> GroupLimit.parse(parser, info);
            case PERMISSION -> PermissionLimit.parse(parser, info);
            default -> throw new LimitParseException("Limit implementation is missing!");
        };
    }

    public LimitType getType() {
        return type;
    }

    /**
     * Retrieves the limit of a limited object.
     * Note: item must be of type Material or EntityType!
     */
    public LimitInfo getLimit(ScanObject<?> item) {
        return switch (item.getType()) {
            case MATERIAL -> getLimit((Material) item.getObject());
            case ENTITY -> getLimit((EntityType) item.getObject());
            default -> throw new IllegalArgumentException("Unknown limited item: " + item);
        };
    }

    public abstract LimitInfo getLimit(Material m);

    public abstract LimitInfo getLimit(EntityType e);

    public File getFile() {
        return info.file;
    }

    public String getBypassPermission() {
        return info.bypassPermission;
    }

    public LimitSettings getSettings() {
        return info.settings;
    }

    public Info getInfo() {
        return info;
    }

    /**
     * Returns the set of materials that this limit consists of.
     */
    public abstract Set<Material> getMaterials();

    /**
     * Returns the set of entities that this limit consists of.
     */
    public abstract Set<EntityType> getEntities();

    /**
     * Returns the set of ScanObjects that this limit consists of.
     */
    public abstract Set<? extends ScanObject<?>> getScanObjects();

    /**
     * Determines the ScanOptions that are required for this limit.
     */
    protected ScanOptions determineScanOptions() {
        if (getMaterials().isEmpty()) {
            return ScanOptions.entitiesOnly();
        } else if (getEntities().isEmpty()) {
            return ScanOptions.materialsOnly();
        } else {
            return ScanOptions.scanOnly();
        }
    }

    /**
     * Returns the ScanOptions that are required for this limit.
     */
    public abstract ScanOptions getScanOptions();

    public static class Info {

        private final File file;
        private final String bypassPermission;
        private final LimitSettings settings;

        /**
         * Constructs an Info object holding basic information of a limit.
         */
        public Info(File file, String bypassPermission, LimitSettings settings) {
            this.file = file;
            this.bypassPermission = bypassPermission;
            this.settings = settings;
        }

        public String getBypassPermission() {
            return bypassPermission;
        }

        public LimitSettings getSettings() {
            return settings;
        }
    }
}
