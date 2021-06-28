package dev.frankheijden.insights.api.concurrent.containers;

import dev.frankheijden.insights.api.concurrent.ScanOptions;
import dev.frankheijden.insights.api.concurrent.storage.DistributionStorage;
import dev.frankheijden.insights.api.objects.chunk.ChunkCuboid;
import dev.frankheijden.insights.api.objects.chunk.ChunkVector;
import dev.frankheijden.insights.api.reflection.RCraftMagicNumbers;
import dev.frankheijden.insights.api.reflection.RCraftWorld;
import dev.frankheijden.insights.api.reflection.REntitySection;
import dev.frankheijden.insights.api.reflection.RPersistentEntitySectionManager;
import dev.frankheijden.insights.api.utils.ChunkUtils;
import net.minecraft.server.level.WorldServer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkCoordIntPair;
import net.minecraft.world.level.chunk.ChunkSection;
import net.minecraft.world.level.entity.EntitySection;
import net.minecraft.world.level.entity.PersistentEntitySectionManager;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.util.NumberConversions;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public abstract class ChunkContainer implements SupplierContainer<DistributionStorage> {

    protected final World world;
    protected final int chunkX;
    protected final int chunkZ;
    protected final ChunkCuboid cuboid;
    protected final ScanOptions options;
    protected final Map<Material, Integer> materialMap;
    protected final Map<EntityType, Integer> entityMap;

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

    public abstract ChunkSection[] getChunkSections() throws Throwable;

    @Override
    public DistributionStorage get() {
        ChunkVector min = cuboid.getMin();
        ChunkVector max = cuboid.getMax();
        int minX = min.getX();
        int maxX = max.getX();
        int minZ = min.getZ();
        int maxZ = max.getZ();
        int blockMinY = min.getY();
        int blockMaxY = max.getY();

        try {
            WorldServer serverLevel = RCraftWorld.getServerLevel(world);

            if (options.materials()) {
                ChunkSection[] chunkSections = getChunkSections();

                int minSectionY = blockMinY >> 4;
                int maxSectionY = blockMaxY >> 4;

                for (int sectionY = minSectionY; sectionY <= maxSectionY; sectionY++) {
                    int minY = sectionY == minSectionY ? blockMinY & 15 : 0;
                    int maxY = sectionY == maxSectionY ? blockMaxY & 15 : 15;

                    ChunkSection section = chunkSections[sectionY];
                    if (section == null) {
                        // Section is empty, count everything as air
                        int count = (maxX - minX + 1) * (maxY - minY + 1) * (maxZ - minZ + 1);
                        materialMap.merge(Material.AIR, count, Integer::sum);
                    } else if (minX == 0 && maxX == 15 && minY == 0 && maxY == 15 && minZ == 0 && maxZ == 15) {
                        // Section can be counted as a whole
                        section.getBlocks().a((state, count) -> {
                            try {
                                var material = RCraftMagicNumbers.getMaterial(state.getBlock());
                                materialMap.merge(material, count, Integer::sum);
                            } catch (Throwable th) {
                                th.printStackTrace();
                            }
                        });
                    } else {
                        // Section must be scanned block by block
                        for (int x = minX; x <= maxX; x++) {
                            for (int y = minY; y <= maxY; y++) {
                                for (int z = minZ; z <= maxZ; z++) {
                                    var material = RCraftMagicNumbers.getMaterial(section.getType(x, y, z).getBlock());
                                    materialMap.merge(material, 1, Integer::sum);
                                }
                            }
                        }
                    }
                }
            }

            if (options.entities()) {
                PersistentEntitySectionManager<Entity> entityManager = serverLevel.G;

                Consumer<Entity> entityConsumer = entity -> {
                    int x = NumberConversions.floor(entity.locX()) & 15;
                    int y = NumberConversions.floor(entity.locY());
                    int z = NumberConversions.floor(entity.locZ()) & 15;
                    if (minX <= x && x <= maxX && blockMinY <= y && y <= blockMaxY && minZ <= z && z <= maxZ) {
                        entityMap.merge(entity.getBukkitEntity().getType(), 1, Integer::sum);
                    }
                };

                if (entityManager.a(getChunkKey())) {
                    var entitySections = RPersistentEntitySectionManager.getSectionStorage(entityManager)
                            .b(getChunkKey())
                            .collect(Collectors.toList());

                    for (EntitySection<Entity> entitySection : entitySections) {
                        REntitySection.iterate(entitySection, entityConsumer);
                    }
                } else {
                    RPersistentEntitySectionManager.getPermanentStorage(entityManager)
                            .a(new ChunkCoordIntPair(chunkX, chunkZ)).join().b().forEach(entityConsumer);
                }
            }
        } catch (Throwable th) {
            th.printStackTrace();
        }

        return DistributionStorage.of(materialMap, entityMap);
    }
}
