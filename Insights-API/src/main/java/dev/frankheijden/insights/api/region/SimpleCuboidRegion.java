package dev.frankheijden.insights.api.region;

import dev.frankheijden.insights.api.objects.math.Vector3;
import org.bukkit.World;
import org.checkerframework.checker.nullness.qual.NonNull;
import java.util.UUID;

public class SimpleCuboidRegion extends CuboidRegion {

    protected final UUID regionUuid;
    protected final String name;

    /**
     * Constructs a new cuboid in given world, with given minimum and maximum vectors.
     */
    public SimpleCuboidRegion(
            @NonNull World world,
            @NonNull Vector3 min,
            @NonNull Vector3 max,
            @NonNull UUID regionUuid,
            @NonNull String name
    ) {
        super(world, min, max);
        this.regionUuid = regionUuid;
        this.name = name;
    }

    @Override
    public @NonNull UUID regionUuid() {
        return regionUuid;
    }

    @Override
    public @NonNull String name() {
        return name;
    }
}
