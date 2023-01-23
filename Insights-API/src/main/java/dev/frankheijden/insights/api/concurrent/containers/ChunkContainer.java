package dev.frankheijden.insights.api.concurrent.containers;

import dev.frankheijden.insights.api.concurrent.ScanOptions;
import dev.frankheijden.insights.api.concurrent.storage.DistributionStorage;
import dev.frankheijden.insights.api.exceptions.ChunkIOException;
import dev.frankheijden.insights.api.objects.chunk.ChunkCuboid;
import dev.frankheijden.insights.api.objects.chunk.ChunkVector;
import dev.frankheijden.insights.api.utils.ChunkUtils;
import dev.frankheijden.insights.nms.core.ChunkEntity;
import dev.frankheijden.insights.nms.core.ChunkSection;
import dev.frankheijden.insights.nms.core.InsightsNMS;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.NotNull;
import java.io.IOException;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.Consumer;

public abstract class ChunkContainer implements SupplierContainer<DistributionStorage> {

    protected final InsightsNMS nms;
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
    protected ChunkContainer(
            InsightsNMS nms,
            World world,
            int chunkX,
            int chunkZ,
            ChunkCuboid cuboid,
            ScanOptions options
    ) {
        this.nms = nms;
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

    public abstract void getChunkSections(Consumer<@NotNull ChunkSection> sectionConsumer) throws IOException;

    public abstract void getChunkEntities(Consumer<@NotNull ChunkEntity> entityConsumer) throws IOException;

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
            int minSectionY = blockMinY >> 4;
            int maxSectionY = blockMaxY >> 4;
            try {
                getChunkSections(section -> {
                    int sectionY = section.index();
                    if (sectionY < minSectionY || sectionY > maxSectionY) return;
                    int minY = sectionY == minSectionY ? blockMinY & 15 : 0;
                    int maxY = sectionY == maxSectionY ? blockMaxY & 15 : 15;

                    if (section.isNull()) {
                        // Section is empty, count everything as air
                        long count = (maxX - minX + 1L) * 16L * (maxZ - minZ + 1L);
                        materialMap.merge(Material.AIR, count, Long::sum);
                    } else if (minX == 0 && maxX == 15 && minY == 0 && maxY == 15 && minZ == 0 && maxZ == 15) {
                        // Section can be counted as a whole
                        section.countBlocks((material, count) -> {
                            try {
                                materialMap.merge(
                                        material,
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
                                            section.blockAt(x, y, z),
                                            1L,
                                            Long::sum
                                    );
                                }
                            }
                        }
                    }
                });
            } catch (IOException ex) {
                throw new ChunkIOException(ex);
            }
        }

        if (options.entities()) {
            try {
                getChunkEntities(entity -> {
                    int x = entity.x() & 15;
                    int y = entity.y() + Math.abs(world.getMinHeight());
                    int z = entity.z() & 15;
                    if (minX <= x && x <= maxX && blockMinY <= y && y <= blockMaxY && minZ <= z && z <= maxZ) {
                        entityMap.merge(entity.type(), 1L, Long::sum);
                    }
                });
            } catch (IOException ex) {
                throw new ChunkIOException(ex);
            }
        }

        return DistributionStorage.of(materialMap, entityMap);
    }
}
