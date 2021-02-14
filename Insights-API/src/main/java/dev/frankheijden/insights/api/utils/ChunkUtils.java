package dev.frankheijden.insights.api.utils;

import dev.frankheijden.insights.api.concurrent.storage.Distribution;
import org.bukkit.Chunk;
import org.bukkit.ChunkSnapshot;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import java.util.EnumMap;
import java.util.Map;

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

    public static long getKey(Location location) {
        return getKey(location.getBlockX() >> 4, location.getBlockZ() >> 4);
    }

    public static long getKey(ChunkSnapshot snapshot) {
        return getKey(snapshot.getX(), snapshot.getZ());
    }

    public static long getKey(int x, int z) {
        return (long) x & 0xffffffffL | ((long) z & 0xffffffffL) << 32;
    }

    /**
     * Counts all entities in given chunk, returning its distribution.
     */
    public static Distribution<EntityType> countEntities(Chunk chunk) {
        Map<EntityType, Integer> entityMap = new EnumMap<>(EntityType.class);
        for (Entity entity : chunk.getEntities()) {
            entityMap.merge(entity.getType(), 1, Integer::sum);
        }
        return new Distribution<>(entityMap);
    }
}
