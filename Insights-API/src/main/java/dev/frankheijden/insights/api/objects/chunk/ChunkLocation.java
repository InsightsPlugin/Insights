package dev.frankheijden.insights.api.objects.chunk;

import dev.frankheijden.insights.api.utils.ChunkUtils;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;

public record ChunkLocation(World world, int x, int z) {

    public static ChunkLocation of(Location loc) {
        return new ChunkLocation(loc.getWorld(), loc.getBlockX() >> 4, loc.getBlockZ() >> 4);
    }

    public static ChunkLocation of(Chunk chunk) {
        return new ChunkLocation(chunk.getWorld(), chunk.getX(), chunk.getZ());
    }

    public long key() {
        return ChunkUtils.getKey(x, z);
    }

    public ChunkPart toPart() {
        return new ChunkPart(this);
    }

    @Override
    public String toString() {
        return world.getName() + " @ " + x + ", " + z;
    }
}
