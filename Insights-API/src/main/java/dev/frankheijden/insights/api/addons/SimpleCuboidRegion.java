package dev.frankheijden.insights.api.addons;

import dev.frankheijden.insights.api.objects.math.Vector3;
import org.bukkit.World;
import java.util.Objects;

public class SimpleCuboidRegion extends CuboidRegion {

    protected final String addon;
    protected final String key;

    /**
     * Constructs a new cuboid in given world, with given minimum and maximum vectors.
     * The `addon` parameter is simply the addon plugin, and the `key` must be a unique string to identify this cache.
     */
    public SimpleCuboidRegion(World world, Vector3 min, Vector3 max, String addon, String key) {
        super(world, min, max);
        this.addon = addon;
        this.key = key;
    }

    @Override
    public String getAddon() {
        return addon;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SimpleCuboidRegion that = (SimpleCuboidRegion) o;
        return addon.equals(that.addon) && key.equals(that.key);
    }

    @Override
    public int hashCode() {
        return Objects.hash(addon, key);
    }
}
