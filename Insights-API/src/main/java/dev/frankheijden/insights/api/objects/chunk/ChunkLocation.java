package dev.frankheijden.insights.api.objects.chunk;

import dev.frankheijden.insights.api.utils.ChunkUtils;
import org.bukkit.Chunk;
import org.bukkit.World;
import java.util.Objects;

public class ChunkLocation {

    private final World world;
    private final int x;
    private final int z;

    /**
     * Constructs a new ChunkLocation at given world, x and z coordinates of the chunk.
     */
    public ChunkLocation(World world, int x, int z) {
        this.world = world;
        this.x = x;
        this.z = z;
    }

    public static ChunkLocation of(Chunk chunk) {
        return new ChunkLocation(chunk.getWorld(), chunk.getX(), chunk.getZ());
    }

    public World getWorld() {
        return world;
    }

    public int getX() {
        return x;
    }

    public int getZ() {
        return z;
    }

    public long getKey() {
        return ChunkUtils.getKey(x, z);
    }

    public ChunkPart toPart() {
        return new ChunkPart(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChunkLocation that = (ChunkLocation) o;
        return x == that.x && z == that.z && world.equals(that.world);
    }

    @Override
    public int hashCode() {
        return Objects.hash(world, x, z);
    }

    @Override
    public String toString() {
        return world.getName() + " @ " + x + ", " + z;
    }
}
