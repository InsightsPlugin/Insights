package dev.frankheijden.insights.api.addons;

import dev.frankheijden.insights.api.objects.math.Cuboid;
import dev.frankheijden.insights.api.objects.math.Vector3;
import org.bukkit.World;

public class AddonCuboid extends Cuboid {

    private final String addon;
    private final String key;

    /**
     * Constructs a new AddonCuboid, containing basic information about the cuboid and the respective addon.
     * Note: the key must be unique for this addon!
     */
    public AddonCuboid(World world, Vector3 min, Vector3 max, String addon, String key) {
        super(world, min, max);
        this.addon = addon;
        this.key = key;
    }

    public String getAddon() {
        return addon;
    }

    public String getKey() {
        return key;
    }
}
