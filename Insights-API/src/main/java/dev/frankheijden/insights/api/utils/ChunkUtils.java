package dev.frankheijden.insights.api.utils;

import org.bukkit.Chunk;
import org.bukkit.ChunkSnapshot;
import org.bukkit.Location;

public class ChunkUtils {

    private ChunkUtils() {}

    public static int getX(long key) {
        return (int) (key & 4294967295L);
    }

    public static int getZ(long key) {
        return (int) (key >>> 32 & 4294967295L);
    }

    public static long getKey(Chunk chunk) {
        return getKey(chunk.getX(), chunk.getZ());
    }

    public static long getKey(Location location) {
        return getKey(location.getBlockX() >> 4, location.getBlockZ() >> 4);
    }

    public static long getKey(ChunkSnapshot snapshot) {
        return getKey(snapshot.getX(), snapshot.getZ());
    }

    public static long getKey(int x, int z) {
        return ((long) x & 4294967295L) | ((long) z & 4294967295L) << 32;
    }
}
