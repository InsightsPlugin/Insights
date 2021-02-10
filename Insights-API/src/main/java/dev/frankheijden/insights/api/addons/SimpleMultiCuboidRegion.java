package dev.frankheijden.insights.api.addons;

import dev.frankheijden.insights.api.objects.math.Cuboid;
import java.util.List;

public class SimpleMultiCuboidRegion extends MultiCuboidRegion {

    protected final String addon;
    protected final String key;

    /**
     * Constructs a new multi cuboid (simply a list of cuboids).
     * The `addon` parameter is simply the addon plugin, and the `key` must be a unique string to identify this cache.
     */
    public SimpleMultiCuboidRegion(List<Cuboid> cuboids, String addon, String key) {
        super(cuboids);
        this.addon = addon;
        this.key = key;
    }

    @Override
    public String getAddon() {
        return null;
    }

    @Override
    public String getKey() {
        return null;
    }
}
