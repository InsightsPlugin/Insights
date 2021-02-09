package dev.frankheijden.insights.api.addons;

import dev.frankheijden.insights.api.objects.math.Cuboid;
import dev.frankheijden.insights.api.objects.math.Vector3;
import org.bukkit.World;
import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        AddonCuboid cuboid = (AddonCuboid) o;
        return addon.equals(cuboid.addon) && key.equals(cuboid.key);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), addon, key);
    }
}
