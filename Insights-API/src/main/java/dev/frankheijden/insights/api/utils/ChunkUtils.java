package dev.frankheijden.insights.api.utils;

import net.minecraft.world.level.ChunkPos;
import org.bukkit.Chunk;
import org.bukkit.ChunkSnapshot;
import org.bukkit.Location;

public class ChunkUtils {

    private ChunkUtils() {}

    public static int getX(long key) {
        return ChunkPos.getX(key);
    }

    public static int getZ(long key) {
        return ChunkPos.getZ(key);
    }

    public static long getKey(Chunk chunk) {
        return ChunkPos.asLong(chunk.getX(), chunk.getZ());
    }

    public static long getKey(Location location) {
        return ChunkPos.asLong(location.getBlockX() >> 4, location.getBlockZ() >> 4);
    }

    public static long getKey(ChunkSnapshot snapshot) {
        return ChunkPos.asLong(snapshot.getX(), snapshot.getZ());
    }

    public static long getKey(int x, int z) {
        return ChunkPos.asLong(x, z);
    }
}
