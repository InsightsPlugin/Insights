package dev.frankheijden.insights.api.concurrent.containers;

import dev.frankheijden.insights.api.concurrent.ScanOptions;
import dev.frankheijden.insights.api.concurrent.storage.DistributionStorage;
import dev.frankheijden.insights.api.exceptions.ChunkIOException;
import dev.frankheijden.insights.api.exceptions.ChunkReflectionException;
import dev.frankheijden.insights.api.objects.chunk.ChunkCuboid;
import dev.frankheijden.insights.api.objects.chunk.ChunkLocation;
import dev.frankheijden.insights.api.objects.chunk.ChunkVector;
import dev.frankheijden.insights.api.reflection.RPersistentEntitySectionManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.entity.EntityPersistentStorage;
import net.minecraft.world.level.entity.EntitySection;
import net.minecraft.world.level.entity.EntitySectionStorage;
import net.minecraft.world.level.entity.PersistentEntitySectionManager;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_19_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_19_R1.util.CraftMagicNumbers;
import org.bukkit.entity.EntityType;
import java.io.IOException;
import java.util.EnumMap;
import java.util.Map;
import java.util.stream.Stream;

public abstract class ChunkContainer implements SupplierContainer<DistributionStorage> {

    protected final ChunkLocation chunkLocation;
    protected final ChunkCuboid cuboid;
    protected final ScanOptions options;
    protected final Map<Material, Long> materialMap;
    protected final Map<EntityType, Long> entityMap;

    /**
     * Constructs a new ChunkSnapshotContainer, with the area to be scanned as a cuboid.
     */
    protected ChunkContainer(ChunkLocation chunkLocation, ChunkCuboid cuboid, ScanOptions options) {
        this.chunkLocation = chunkLocation;
        this.cuboid = cuboid;
        this.options = options;
        this.materialMap = new EnumMap<>(Material.class);
        this.entityMap = new EnumMap<>(EntityType.class);
    }

    public ChunkLocation chunkLocation() {
        return chunkLocation;
    }

    public ChunkCuboid chunkCuboid() {
        return cuboid;
    }

    public abstract LevelChunkSection[] chunkSections() throws IOException;

    @Override
    public DistributionStorage get() {
        ChunkVector min = cuboid.min();
        ChunkVector max = cuboid.max();
        int minX = min.x();
        int maxX = max.x();
        int minZ = min.z();
        int maxZ = max.z();
        int blockMinY = Math.max(min.y(), 0);
        int blockMaxY = Math.abs(Math.min(min.y(), 0)) + max.y();

        ServerLevel serverLevel = ((CraftWorld) chunkLocation.world()).getHandle();

        if (options.materials()) {
            LevelChunkSection[] chunkSections;
            try {
                chunkSections = chunkSections();
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
            PersistentEntitySectionManager<Entity> entityManager = serverLevel.entityManager;

            long chunkKey = chunkLocation.key();
            final Stream<Entity> entityStream;
            if (entityManager.areEntitiesLoaded(chunkKey)) {
                EntitySectionStorage<Entity> sectionStorage;
                try {
                    sectionStorage = RPersistentEntitySectionManager.getSectionStorage(entityManager);
                } catch (Throwable th) {
                    throw new ChunkReflectionException(th);
                }

                entityStream = sectionStorage
                        .getExistingSectionsInChunk(chunkKey)
                        .flatMap(EntitySection::getEntities);
            } else {
                EntityPersistentStorage<Entity> permanentStorage;
                try {
                    permanentStorage = RPersistentEntitySectionManager.getPermanentStorage(entityManager);
                } catch (Throwable th) {
                    throw new ChunkReflectionException(th);
                }

                entityStream = permanentStorage
                        .loadEntities(new ChunkPos(chunkLocation.x(), chunkLocation.z()))
                        .join()
                        .getEntities();
            }

            entityStream.filter(entity -> {
                int x = entity.getBlockX() & 15;
                int y = entity.getBlockY();
                int z = entity.getBlockZ() & 15;
                return minX <= x && x <= maxX && blockMinY <= y && y <= blockMaxY && minZ <= z && z <= maxZ;
            }).forEach(entity -> entityMap.merge(entity.getBukkitEntity().getType(), 1L, Long::sum));
        }

        return DistributionStorage.of(materialMap, entityMap);
    }
}
