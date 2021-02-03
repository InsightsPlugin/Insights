package dev.frankheijden.insights.api.config.limits;

import dev.frankheijden.insights.api.config.parser.SensitiveYamlParser;
import dev.frankheijden.insights.api.config.parser.YamlParseException;
import dev.frankheijden.insights.api.config.parser.YamlParser;
import org.bukkit.Bukkit;
import org.bukkit.World;
import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class Limit {

    private static final String LIMIT_SECTION = "limit";

    private final LimitType type;
    private final String bypassPermission;
    private final int limit;
    private final LimitSettings settings;

    protected Limit(LimitType type, Info info) {
        this(type, info.getBypassPermission(), info.getLimit(), info.getSettings());
    }

    protected Limit(LimitType type, String bypassPermission, int limit, LimitSettings settings) {
        this.type = type;
        this.bypassPermission = bypassPermission;
        this.limit = limit;
        this.settings = settings;
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

        String bypassPermission = parser.getString("limit.bypass-permission", null, false);
        int limit = parser.getInt("limit.limit", -1, 0, Integer.MAX_VALUE);

        boolean worldWhitelist = parser.getBoolean("limit.settings.enabled-worlds.whitelist", false, false);
        Set<String> allowedWorlds = Bukkit.getWorlds().stream()
                .map(World::getName)
                .collect(Collectors.toSet());
        Set<UUID> worlds = parser.getSet("limit.settings.enabled-worlds.worlds", allowedWorlds, "world").stream()
                .map(Bukkit::getWorld)
                .filter(Objects::nonNull)
                .map(World::getUID)
                .collect(Collectors.toSet());

        boolean addonWhitelist = parser.getBoolean("limit.settings.enabled-addons.whitelist", false, false);
        Set<String> addons = parser.getSet("limit.settings.enabled-addons.addons");

        boolean worldEdit = parser.getBoolean("limit.settings.worldedit", true, false);

        LimitSettings settings = new LimitSettings(worlds, worldWhitelist, addons, addonWhitelist, worldEdit);

        Info info = new Info(bypassPermission, limit, settings);
        switch (type) {
            case TILE: return TileLimit.parse(parser, info);
            case GROUP: return GroupLimit.parse(parser, info);
            case PERMISSION: return PermissionLimit.parse(parser, info);
            default: throw new LimitParseException("Limit implementation is missing!");
        }
    }

    public LimitType getType() {
        return type;
    }

    public int getLimit() {
        return limit;
    }

    public String getBypassPermission() {
        return bypassPermission;
    }

    public LimitSettings getSettings() {
        return settings;
    }

    public static class Info {

        private final String bypassPermission;
        private final int limit;
        private final LimitSettings settings;

        /**
         * Constructs an Info object holding basic information of a limit.
         */
        public Info(String bypassPermission, int limit, LimitSettings settings) {
            this.bypassPermission = bypassPermission;
            this.limit = limit;
            this.settings = settings;
        }

        public String getBypassPermission() {
            return bypassPermission;
        }

        public int getLimit() {
            return limit;
        }

        public LimitSettings getSettings() {
            return settings;
        }
    }
}
