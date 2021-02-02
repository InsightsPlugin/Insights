package dev.frankheijden.insights.api.utils;

import org.bukkit.Chunk;

public class ChunkUtils {

    private ChunkUtils() {}

    public static int getX(long key) {
        return (int) (key & 0xffffffffL);
    }

    public static int getZ(long key) {
        return (int) (key >> 32);
    }

    public static long getKey(Chunk chunk) {
        return getKey(chunk.getX(), chunk.getZ());
    }

    public static long getKey(int x, int z) {
        return (long) x & 0xffffffffL | ((long) z & 0xffffffffL) << 32;
    }
}
