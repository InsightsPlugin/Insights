package dev.frankheijden.insights.api.concurrent.containers;

import dev.frankheijden.insights.api.concurrent.ScanOptions;
import dev.frankheijden.insights.api.concurrent.storage.DistributionStorage;
import dev.frankheijden.insights.api.exceptions.ChunkIOException;
import dev.frankheijden.insights.api.objects.chunk.ChunkCuboid;
import dev.frankheijden.insights.api.objects.chunk.ChunkVector;
import dev.frankheijden.insights.api.utils.ChunkUtils;
import net.minecraft.world.level.chunk.LevelChunkSection;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_19_R1.util.CraftMagicNumbers;
import org.bukkit.entity.EntityType;
import java.io.IOException;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.Consumer;

public abstract class ChunkContainer implements SupplierContainer<DistributionStorage> {

    protected final World world;
    protected final int chunkX;
    protected final int chunkZ;
    protected final ChunkCuboid cuboid;
    protected final ScanOptions options;
    protected final Map<Material, Long> materialMap;
    protected final Map<EntityType, Long> entityMap;

    /**
     * Constructs a new ChunkSnapshotContainer, with the area to be scanned as a cuboid.
     */
    protected ChunkContainer(World world, int chunkX, int chunkZ, ChunkCuboid cuboid, ScanOptions options) {
        this.world = world;
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
        this.cuboid = cuboid;
        this.options = options;
        this.materialMap = new EnumMap<>(Material.class);
        this.entityMap = new EnumMap<>(EntityType.class);
    }

    public World getWorld() {
        return world;
    }

    public long getChunkKey() {
        return ChunkUtils.getKey(chunkX, chunkZ);
    }

    public int getX() {
        return chunkX;
    }

    public int getZ() {
        return chunkZ;
    }

    public ChunkCuboid getChunkCuboid() {
        return cuboid;
    }

    public abstract LevelChunkSection[] getChunkSections() throws IOException;

    public abstract void getChunkEntities(Consumer<ChunkEntity> entityConsumer) throws IOException;

    @Override
    public DistributionStorage get() {
        ChunkVector min = cuboid.getMin();
        ChunkVector max = cuboid.getMax();
        int minX = min.getX();
        int maxX = max.getX();
        int minZ = min.getZ();
        int maxZ = max.getZ();
        int blockMinY = Math.max(min.getY(), 0);
        int blockMaxY = Math.abs(Math.min(min.getY(), 0)) + max.getY();

        if (options.materials()) {
            LevelChunkSection[] chunkSections;
            try {
                chunkSections = getChunkSections();
            } catch (IOException ex) {
                throw new ChunkIOException(ex);
            }

            int minSectionY = blockMinY >> 4;
            int maxSectionY = blockMaxY >> 4;

            for (int sectionY = minSectionY; sectionY <= maxSectionY; sectionY++) {
                int minY = sectionY == minSectionY ? blockMinY & 15 : 0;
                int maxY = sectionY == maxSectionY ? blockMaxY & 15 : 15;

                LevelChunkSection section = chunkSections[sectionY];
                if (section == null) {
                    // Section is empty, count everything as air
                    long count = (maxX - minX + 1L) * (maxY - minY + 1L) * (maxZ - minZ + 1L);
                    materialMap.merge(Material.AIR, count, Long::sum);
                } else if (minX == 0 && maxX == 15 && minY == 0 && maxY == 15 && minZ == 0 && maxZ == 15) {
                    // Section can be counted as a whole
                    section.getStates().count((state, count) -> {
                        try {
                            materialMap.merge(
                                    CraftMagicNumbers.getMaterial(state.getBlock()),
                                    (long) count,
                                    Long::sum
                            );
                        } catch (Throwable th) {
                            th.printStackTrace();
                        }
                    });
                } else {
                    // Section must be scanned block by block
                    for (int x = minX; x <= maxX; x++) {
                        for (int y = minY; y <= maxY; y++) {
                            for (int z = minZ; z <= maxZ; z++) {
                                materialMap.merge(
                                        CraftMagicNumbers.getMaterial(section.getBlockState(x, y, z).getBlock()),
                                        1L,
                                        Long::sum
                                );
                            }
                        }
                    }
                }
            }
        }

        if (options.entities()) {
            try {
                getChunkEntities(entity -> {
                    int x = entity.x & 15;
                    int y = entity.y;
                    int z = entity.z & 15;
                    if (minX <= x && x <= maxX && blockMinY <= y && y <= blockMaxY && minZ <= z && z <= maxZ) {
                        entityMap.merge(entity.type, 1L, Long::sum);
                    }
                });
            } catch (IOException ex) {
                throw new ChunkIOException(ex);
            }
        }

        return DistributionStorage.of(materialMap, entityMap);
    }

    public record ChunkEntity(EntityType type, int x, int y, int z) {

    }
}
