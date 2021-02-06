package dev.frankheijden.insights.api.concurrent.containers;

import dev.frankheijden.insights.api.objects.chunk.ChunkCuboid;
import dev.frankheijden.insights.api.objects.chunk.ChunkVector;
import dev.frankheijden.insights.api.utils.ChunkUtils;
import org.bukkit.ChunkSnapshot;
import org.bukkit.Material;
import java.util.EnumMap;
import java.util.UUID;

public class ChunkSnapshotContainer extends DistributionContainer<Material> {

    private final ChunkSnapshot chunkSnapshot;
    private final UUID worldUid;
    private final ChunkCuboid cuboid;

    public ChunkSnapshotContainer(ChunkSnapshot chunkSnapshot, UUID worldUid) {
        this(chunkSnapshot, worldUid, ChunkCuboid.MAX);
    }

    /**
     * Constructs a new ChunkSnapshotContainer, with the area to be scanned as a cuboid.
     */
    public ChunkSnapshotContainer(ChunkSnapshot chunkSnapshot, UUID worldUid, ChunkCuboid cuboid) {
        super(new EnumMap<>(Material.class));
        this.chunkSnapshot = chunkSnapshot;
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
        return chunkSnapshot.getX();
    }

    public int getZ() {
        return chunkSnapshot.getZ();
    }

    @Override
    public void run() {
        ChunkVector min = cuboid.getMin();
        ChunkVector max = cuboid.getMax();
        for (int x = min.getX(); x < max.getX(); x++) {
            for (int y = min.getY(); y < max.getY(); y++) {
                for (int z = min.getZ(); z < max.getZ(); z++) {
                    distributionMap.merge(chunkSnapshot.getBlockType(x, y, z), 1, Integer::sum);
                }
            }
        }
    }
}
