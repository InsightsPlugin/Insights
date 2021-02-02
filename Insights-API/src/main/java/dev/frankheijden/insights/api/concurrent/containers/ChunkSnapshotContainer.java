package dev.frankheijden.insights.api.concurrent.containers;

import dev.frankheijden.insights.api.objects.chunk.ChunkVector;
import dev.frankheijden.insights.api.utils.ChunkUtils;
import org.bukkit.ChunkSnapshot;
import org.bukkit.Material;
import java.util.EnumMap;
import java.util.UUID;

public class ChunkSnapshotContainer extends DistributionContainer<Material> {

    private final ChunkSnapshot chunkSnapshot;
    private final UUID worldUid;
    private final ChunkVector min;
    private final ChunkVector max;

    public ChunkSnapshotContainer(ChunkSnapshot chunkSnapshot, UUID worldUid) {
        this(chunkSnapshot, worldUid, ChunkVector.MIN, ChunkVector.MAX);
    }

    /**
     * Constructs a new ChunkSnapshotContainer, with the area to be scanned as a boundingbox between min and max.
     */
    public ChunkSnapshotContainer(ChunkSnapshot chunkSnapshot, UUID worldUid, ChunkVector min, ChunkVector max) {
        super(new EnumMap<>(Material.class));
        this.chunkSnapshot = chunkSnapshot;
        this.worldUid = worldUid;
        this.min = min;
        this.max = max;
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
        for (int x = min.getX(); x < max.getX(); x++) {
            for (int y = min.getY(); y < max.getY(); y++) {
                for (int z = min.getZ(); z < max.getZ(); z++) {
                    distributionMap.merge(chunkSnapshot.getBlockType(x, y, z), 1, Integer::sum);
                }
            }
        }
    }
}
