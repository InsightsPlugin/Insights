package dev.frankheijden.insights.api.config.limits;

import java.util.Set;

public class LimitSettings {

    private final Set<String> worlds;
    private final boolean worldWhitelist;
    private final Set<String> addons;
    private final boolean addonWhitelist;

    /**
     * Constructs a new LimitSettings object.
     */
    public LimitSettings(Set<String> worlds, boolean worldWhitelist, Set<String> addons, boolean addonWhitelist) {
        this.worlds = worlds;
        this.worldWhitelist = worldWhitelist;
        this.addons = addons;
        this.addonWhitelist = addonWhitelist;
    }

    public Set<String> getWorlds() {
        return worlds;
    }

    public boolean isWorldWhitelist() {
        return worldWhitelist;
    }

    /**
     * Checks whether this limit can be applied on given world.
     */
    public boolean appliesToWorld(String worldName) {
        if (worldWhitelist) {
            return worlds.contains(worldName);
        } else {
            return !worlds.contains(worldName);
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
}
