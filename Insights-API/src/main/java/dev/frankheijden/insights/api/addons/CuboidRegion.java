package dev.frankheijden.insights.api.addons;

import dev.frankheijden.insights.api.objects.math.Cuboid;
import dev.frankheijden.insights.api.objects.math.Vector3;
import org.bukkit.World;

public abstract class CuboidRegion extends Cuboid implements Region {

    /**
     * Constructs a new cuboid in given world, with given minimum and maximum vectors.
     */
    protected CuboidRegion(World world, Vector3 min, Vector3 max) {
        super(world, min, max);
    }
}
