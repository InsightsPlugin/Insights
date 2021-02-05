package dev.frankheijden.insights.api.config.limits;

import java.util.Set;
import java.util.UUID;

public class LimitSettings {

    private final Set<UUID> worlds;
    private final boolean worldWhitelist;
    private final Set<String> addons;
    private final boolean addonWhitelist;
    private final boolean worldEditIntegration;

    /**
     * Constructs a new LimitSettings object.
     */
    public LimitSettings(Set<UUID> worlds,
                         boolean worldWhitelist,
                         Set<String> addons,
                         boolean addonWhitelist,
                         boolean worldEditIntegration) {
        this.worlds = worlds;
        this.worldWhitelist = worldWhitelist;
        this.addons = addons;
        this.addonWhitelist = addonWhitelist;
        this.worldEditIntegration = worldEditIntegration;
    }

    public Set<UUID> getWorlds() {
        return worlds;
    }

    public boolean isWorldWhitelist() {
        return worldWhitelist;
    }

    /**
     * Checks whether this limit can be applied on given world.
     */
    public boolean appliesToWorld(UUID worldUid) {
        if (worldWhitelist) {
            return worlds.contains(worldUid);
        } else {
            return !worlds.contains(worldUid);
        }
    }

    public Set<String> getAddons() {
        return addons;
    }

    public boolean isAddonWhitelist() {
        return addonWhitelist;
    }

    /**
     * Checks whether this limit can be applied on given addon.
     */
    public boolean appliesToAddon(String addonName) {
        if (addonWhitelist) {
            return addons.contains(addonName);
        } else {
            return !addons.contains(addonName);
        }
    }

    public boolean isWorldEditIntegrated() {
        return worldEditIntegration;
    }
}
