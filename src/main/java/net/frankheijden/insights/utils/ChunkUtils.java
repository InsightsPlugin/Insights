package net.frankheijden.insights.utils;

import net.frankheijden.insights.Insights;
import net.frankheijden.insights.api.entities.ChunkLocation;
import net.frankheijden.insights.config.Limit;
import org.bukkit.*;
import org.bukkit.entity.Entity;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class ChunkUtils {

    public static List<ChunkLocation> getChunkLocations(Chunk[] chunks) {
        ArrayList<ChunkLocation> chunkLocations = new ArrayList<>();
        for (Chunk chunk : chunks) {
            chunkLocations.add(new ChunkLocation(chunk));
        }
        return chunkLocations;
    }

    public static List<ChunkLocation> getChunkLocations(Chunk chunk, int radius) {
        int x = chunk.getX();
        int z = chunk.getZ();
        ArrayList<ChunkLocation> chunkLocations = new ArrayList<>();
        for (int xc = x-radius; xc <= x+radius; xc++) {
            for (int zc = z - radius; zc <= z + radius; zc++) {
                chunkLocations.add(new ChunkLocation(xc, zc));
            }
        }
        return chunkLocations;
    }

    public static int getAmountInChunk(Chunk chunk, ChunkSnapshot chunkSnapshot, Limit limit) {
        int count = 0;

        List<String> materials = limit.getMaterials();
        if (materials != null && !materials.isEmpty()) {
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    for (int y = 0; y < 256; y++) {
                        if (materials.contains(getMaterial(chunkSnapshot,x,y,z).name())) {
                            count++;
                        }
                    }
                }
            }
        }

        List<String> entities = limit.getEntities();
        if (entities != null && !entities.isEmpty()) {
            for (Entity entity : chunk.getEntities()) {
                if (entities.contains(entity.getType().name())) {
                    count++;
                }
            }
        }

        return count;
    }

    public static Material getMaterial(ChunkSnapshot chunkSnapshot, int x, int y, int z) {
        if (chunkSnapshot == null) return null;

        try {
            Class<?> chunkSnapshotClass = Class.forName("org.bukkit.ChunkSnapshot");
            Object chunkSnap = chunkSnapshotClass.cast(chunkSnapshot);
            if (Insights.getInstance().isPost1_13()) {
                Method m = chunkSnapshotClass.getDeclaredMethod("getBlockType", int.class, int.class, int.class);
                return (Material) m.invoke(chunkSnap, x, y, z);
            } else {
                Method m = chunkSnapshotClass.getDeclaredMethod("getBlockTypeId", int.class, int.class, int.class);
                int id = (int) m.invoke(chunkSnap, x, y, z);

                Class<?> materialClass = Class.forName("org.bukkit.Material");
                Method m1 = materialClass.getDeclaredMethod("getMaterial", int.class);
                return (Material) m1.invoke(materialClass, id);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }
}
