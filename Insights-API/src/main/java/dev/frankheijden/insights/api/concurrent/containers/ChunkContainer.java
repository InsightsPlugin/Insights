package dev.frankheijden.insights.api.concurrent.containers;

import dev.frankheijden.insights.api.concurrent.ScanOptions;
import dev.frankheijden.insights.api.concurrent.storage.DistributionStorage;
import dev.frankheijden.insights.api.objects.chunk.ChunkCuboid;
import dev.frankheijden.insights.api.objects.chunk.ChunkVector;
import dev.frankheijden.insights.api.reflection.RChunk;
import dev.frankheijden.insights.api.reflection.RChunkSection;
import dev.frankheijden.insights.api.reflection.RCraftChunk;
import dev.frankheijden.insights.api.reflection.REntity;
import dev.frankheijden.insights.api.reflection.RUnsafeList;
import dev.frankheijden.insights.api.utils.ChunkUtils;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.util.NumberConversions;
import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;

public class ChunkContainer implements SupplierContainer<DistributionStorage> {

    private final org.bukkit.Chunk bukkitChunk;
    private final UUID worldUid;
    private final ChunkCuboid cuboid;
    private final ScanOptions options;
    private final Map<Material, Integer> materialMap;
    private final Map<EntityType, Integer> entityMap;

    public ChunkContainer(org.bukkit.Chunk chunk, UUID worldUid) {
        this(chunk, worldUid, ScanOptions.all());
    }

    public ChunkContainer(org.bukkit.Chunk chunk, UUID worldUid, ScanOptions options) {
        this(chunk, worldUid, ChunkCuboid.MAX, options);
    }

    /**
     * Constructs a new ChunkSnapshotContainer, with the area to be scanned as a cuboid.
     */
    @SuppressWarnings("LineLength")
    public ChunkContainer(org.bukkit.Chunk bukkitChunk, UUID worldUid, ChunkCuboid cuboid, ScanOptions options) {
        this.bukkitChunk = bukkitChunk;
        this.worldUid = worldUid;
        this.cuboid = cuboid;
        this.options = options;
        this.materialMap = new EnumMap<>(Material.class);
        this.entityMap = new EnumMap<>(EntityType.class);
    }

    public UUID getWorldUid() {
        return worldUid;
    }

    public long getChunkKey() {
        return ChunkUtils.getKey(getX(), getZ());
    }

    public int getX() {
        return bukkitChunk.getX();
    }

    public int getZ() {
        return bukkitChunk.getZ();
    }

    @Override
    public DistributionStorage get() {
        Object chunk = RCraftChunk.getReflection().invoke(bukkitChunk, "getHandle");

        ChunkVector min = cuboid.getMin();
        ChunkVector max = cuboid.getMax();
        int minX = min.getX();
        int maxX = max.getX();
        int minZ = min.getZ();
        int maxZ = max.getZ();
        int blockMinY = min.getY();
        int blockMaxY = max.getY();

        if (options.materials()) {
            Object[] sections = RChunk.getReflection().invoke(chunk, "getSections");

            int minSectionY = blockMinY >> 4;
            int maxSectionY = blockMaxY >> 4;

            try {
                for (int sectionY = minSectionY; sectionY <= maxSectionY; sectionY++) {
                    int minY = sectionY == minSectionY ? blockMinY & 15 : 0;
                    int maxY = sectionY == maxSectionY ? blockMaxY & 15 : 15;

                    Object section = sections[sectionY];
                    if (RChunkSection.isEmpty(section)) {
                        int count = (maxX - minX + 1) * (maxY - minY + 1) * (maxZ - minZ + 1);
                        materialMap.merge(Material.AIR, count, Integer::sum);
                    } else {
                        for (int x = minX; x <= maxX; x++) {
                            for (int y = minY; y <= maxY; y++) {
                                for (int z = minZ; z <= maxZ; z++) {
                                    materialMap.merge(RChunkSection.getType(section, x, y, z), 1, Integer::sum);
                                }
                            }
                        }
                    }
                }
            } catch (Throwable th) {
                //
            }
        }

        if (options.entities()) {
            Object[] entitySlices = RChunk.getReflection().invoke(chunk, "getEntitySlices");

            try {
                for (int i = 0; i < entitySlices.length; i++) {
                    Object entitySlice = entitySlices[i];
                    Object[] entities = RUnsafeList.getData(entitySlice);

                    for (int j = 0; j < RUnsafeList.size(entitySlice); j++) {
                        Object entity = entities[j];
                        if (entity == null) continue;

                        int x = NumberConversions.floor(REntity.locX(entity)) & 15;
                        int y = NumberConversions.floor(REntity.locY(entity));
                        int z = NumberConversions.floor(REntity.locZ(entity)) & 15;
                        if (minX <= x && x <= maxX && blockMinY <= y && y <= blockMaxY && minZ <= z && z <= maxZ) {
                            entityMap.merge(REntity.getBukkitEntity(entity).getType(), 1, Integer::sum);
                        }
                    }
                }
            } catch (Throwable th) {
                //
            }
        }

        return DistributionStorage.of(materialMap, entityMap);
    }
}
