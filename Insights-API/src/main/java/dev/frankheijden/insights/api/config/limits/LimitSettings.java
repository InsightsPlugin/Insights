package dev.frankheijden.insights.api.config.limits;

import java.util.Set;
import java.util.UUID;

public class LimitSettings {

    private final Set<UUID> worlds;
    private final boolean worldWhitelist;
    private final Set<String> addons;
    private final boolean addonWhitelist;

    /**
     * Constructs a new LimitSettings object.
     */
    public LimitSettings(Set<UUID> worlds, boolean worldWhitelist, Set<String> addons, boolean addonWhitelist) {
        this.worlds = worlds;
        this.worldWhitelist = worldWhitelist;
        this.addons = addons;
        this.addonWhitelist = addonWhitelist;
    }

    public Set<UUID> getWorlds() {
        return worlds;
    }

    public boolean isWorldWhitelist() {
        return worldWhitelist;
    }

    public Set<String> getAddons() {
        return addons;
    }

    public boolean isAddonWhitelist() {
        return addonWhitelist;
    }
}
