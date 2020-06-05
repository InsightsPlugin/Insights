package net.frankheijden.insights.utils;

import net.frankheijden.insights.config.Limit;
import net.frankheijden.insights.entities.*;
import net.frankheijden.insights.managers.NMSManager;
import org.bukkit.*;
import org.bukkit.entity.Entity;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class ChunkUtils {

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

    public static List<PartialChunk> getPartialChunks(Location l1, Location l2) {
        Location min = LocationUtils.min(l1, l2);
        ChunkVector minV = ChunkVector.from(min);
        Chunk minChunk = min.getChunk();
        int minX = minChunk.getX();
        int minZ = minChunk.getZ();

        Location max = LocationUtils.max(l1, l2);
        ChunkVector maxV = ChunkVector.from(max);
        Chunk maxChunk = max.getChunk();
        int maxX = maxChunk.getX();
        int maxZ = maxChunk.getZ();

        List<PartialChunk> partials = new ArrayList<>();
        for (int x = minX; x <= maxX; x++) {
            int xmin = (x == minX) ? minV.getX() : 0;
            int ymin = minV.getY();
            int xmax = (x == maxX) ? maxV.getX() : 15;

            for (int z = minZ; z <= maxZ; z++) {
                int zmin = (z == minZ) ? minV.getZ() : 0;
                int ymax = maxV.getY();
                int zmax = (z == maxZ) ? maxV.getZ() : 15;

                ChunkLocation loc = new ChunkLocation(x, z);
                ChunkVector vmin = new ChunkVector(xmin, ymin, zmin);
                ChunkVector vmax = new ChunkVector(xmax, ymax, zmax);
                partials.add(new PartialChunk(loc, vmin, vmax));
            }
        }
        return partials;
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

    private static class Cache {
        Method getBlockType = null;
        Method getBlockTypeId = null;
        Method getMaterial = null;
    }

    private static Cache cache = null;
    private static void initialiseCache() {
        cache = new Cache();

        try {
            Class<?> chunkSnapshotClass = ChunkSnapshot.class;
            if (NMSManager.getInstance().isPost(13)) {
                cache.getBlockType = chunkSnapshotClass.getDeclaredMethod("getBlockType", int.class, int.class, int.class);
            } else {
                cache.getBlockTypeId = chunkSnapshotClass.getDeclaredMethod("getBlockTypeId", int.class, int.class, int.class);
                cache.getMaterial = Material.class.getDeclaredMethod("getMaterial", int.class);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static Material getMaterial(ChunkSnapshot chunkSnapshot, int x, int y, int z) {
        if (chunkSnapshot == null) return null;
        if (cache == null) initialiseCache();

        try {
            if (NMSManager.getInstance().isPost(13)) {
                return (Material) cache.getBlockType.invoke(chunkSnapshot, x, y, z);
            } else {
                int id = (int) cache.getBlockTypeId.invoke(chunkSnapshot, x, y, z);
                return (Material) cache.getMaterial.invoke(Material.class, id);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }
}
