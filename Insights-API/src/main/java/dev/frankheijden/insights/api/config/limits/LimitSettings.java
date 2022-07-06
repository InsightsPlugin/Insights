package dev.frankheijden.insights.api.config.limits;

import java.util.Set;

public class LimitSettings {

    private final Set<String> worldNames;
    private final boolean worldWhitelist;
    private final Set<String> addonIds;
    private final boolean addonWhitelist;
    private final boolean disallowPlacementOutsideRegion;

    /**
     * Constructs a new LimitSettings object.
     */
    public LimitSettings(
            Set<String> worldNames,
            boolean worldWhitelist,
            Set<String> addonIds,
            boolean addonWhitelist,
            boolean disallowPlacementOutsideRegion
    ) {
        this.worldNames = worldNames;
        this.worldWhitelist = worldWhitelist;
        this.addonIds = addonIds;
        this.addonWhitelist = addonWhitelist;
        this.disallowPlacementOutsideRegion = disallowPlacementOutsideRegion;
    }

    public Set<String> worldNames() {
        return worldNames;
    }

    public boolean worldWhitelist() {
        return worldWhitelist;
    }

    /**
     * Checks whether this limit can be applied on given world.
     */
    public boolean appliesToWorld(String worldName) {
        if (worldWhitelist) {
            return worldNames.contains(worldName);
        } else {
            return !worldNames.contains(worldName);
        }
    }

    public Set<String> addonIds() {
        return addonIds;
    }

    public boolean addonWhitelist() {
        return addonWhitelist;
    }

    public boolean disallowedPlacementOutsideRegion() {
        return disallowPlacementOutsideRegion;
    }

    /**
     * Checks whether this limit can be applied on given addon.
     */
    public boolean appliesToAddon(String addonId) {
        if (addonWhitelist) {
            return addonIds.contains(addonId);
        } else {
            return !addonIds.contains(addonId);
        }
    }
}
