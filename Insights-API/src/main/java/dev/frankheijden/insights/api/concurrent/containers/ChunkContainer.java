package dev.frankheijden.insights.api.concurrent.containers;

import dev.frankheijden.insights.api.objects.chunk.ChunkCuboid;
import dev.frankheijden.insights.api.objects.chunk.ChunkVector;
import dev.frankheijden.insights.api.reflection.RChunk;
import dev.frankheijden.insights.api.reflection.RChunkSection;
import dev.frankheijden.insights.api.reflection.RCraftChunk;
import dev.frankheijden.insights.api.utils.ChunkUtils;
import org.bukkit.Material;
import java.util.EnumMap;
import java.util.UUID;

public class ChunkContainer extends DistributionContainer<Material> {

    private final org.bukkit.Chunk bukkitChunk;
    private final UUID worldUid;
    private final ChunkCuboid cuboid;

    public ChunkContainer(org.bukkit.Chunk chunk, UUID worldUid) {
        this(chunk, worldUid, ChunkCuboid.MAX);
    }

    /**
     * Constructs a new ChunkSnapshotContainer, with the area to be scanned as a cuboid.
     */
    public ChunkContainer(org.bukkit.Chunk bukkitChunk, UUID worldUid, ChunkCuboid cuboid) {
        super(new EnumMap<>(Material.class));
        this.bukkitChunk = bukkitChunk;
        this.worldUid = worldUid;
        this.cuboid = cuboid;
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
    public void run() {
        Object chunk = RCraftChunk.getReflection().invoke(bukkitChunk, "getHandle");
        Object[] sections = RChunk.getReflection().invoke(chunk, "getSections");

        ChunkVector min = cuboid.getMin();
        ChunkVector max = cuboid.getMax();
        int minX = min.getX();
        int maxX = max.getX();
        int minZ = min.getZ();
        int maxZ = max.getZ();
        int minSectionY = min.getY() >> 4;
        int maxSectionY = max.getY() >> 4;

        try {
            for (int sectionY = minSectionY; sectionY <= maxSectionY; sectionY++) {
                int minY = sectionY == minSectionY ? min.getY() & 15 : 0;
                int maxY = sectionY == maxSectionY ? max.getY() & 15 : 15;

                Object section = sections[sectionY];
                if (RChunkSection.isEmpty(section)) {
                    int count = (maxX - minX + 1) * (maxY - minY + 1) * (maxZ - minZ + 1);
                    distributionMap.merge(Material.AIR, count, Integer::sum);
                } else {
                    for (int x = minX; x <= maxX; x++) {
                        for (int y = minY; y <= maxY; y++) {
                            for (int z = minZ; z <= maxZ; z++) {
                                distributionMap.merge(RChunkSection.getType(section, x, y, z), 1, Integer::sum);
                            }
                        }
                    }
                }
            }
        } catch (Throwable th) {
            //
        }
    }
}
