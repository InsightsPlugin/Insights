package dev.frankheijden.insights.api.concurrent;

import dev.frankheijden.insights.api.concurrent.containers.ChunkSnapshotContainer;
import dev.frankheijden.insights.api.concurrent.containers.RunnableContainer;
import dev.frankheijden.insights.api.concurrent.containers.SupplierContainer;
import dev.frankheijden.insights.api.concurrent.storage.WorldDistributionStorage;
import dev.frankheijden.insights.api.concurrent.tracker.WorldChunkScanTracker;
import org.bukkit.Chunk;
import org.bukkit.ChunkSnapshot;
import org.bukkit.Material;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Decorator class for ContainerExecutor to add chunk scanning functionality.
 */
public class ChunkContainerExecutor implements ContainerExecutor {

    private final ContainerExecutor containerExecutor;
    private final WorldDistributionStorage distributionStorage;
    private final WorldChunkScanTracker scanTracker;

    /**
     * Constructs a new ChunkContainerExecutor.
     */
    public ChunkContainerExecutor(ContainerExecutor containerExecutor,
                                  WorldDistributionStorage distributionStorage,
                                  WorldChunkScanTracker scanTracker) {
        this.containerExecutor = containerExecutor;
        this.distributionStorage = distributionStorage;
        this.scanTracker = scanTracker;
    }

    public CompletableFuture<Map<Material, Integer>> submit(Chunk chunk, boolean save) {
        return submit(chunk.getChunkSnapshot(), chunk.getWorld().getUID(), save);
    }

    public CompletableFuture<Map<Material, Integer>> submit(ChunkSnapshot chunkSnapshot, UUID worldUid, boolean save) {
        return submit(new ChunkSnapshotContainer(chunkSnapshot, worldUid), save);
    }

    /**
     * Submits a ChunkSnapshotContainer for scanning, optionally saving the result into storage.
     */
    public CompletableFuture<Map<Material, Integer>> submit(ChunkSnapshotContainer container, boolean save) {
        UUID worldUid = container.getWorldUid();
        long chunkKey = container.getChunkKey();
        scanTracker.set(worldUid, chunkKey, true);

        CompletableFuture<Map<Material, Integer>> future = submit(container);
        return save ? future.whenComplete((map, err) -> {
            distributionStorage.put(worldUid, chunkKey, map);
            scanTracker.set(worldUid, chunkKey, false);
        }) : future;
    }

    @Override
    public <T> CompletableFuture<T> submit(SupplierContainer<T> container) {
        return containerExecutor.submit(container);
    }

    @Override
    public CompletableFuture<Void> submit(RunnableContainer container) {
        return containerExecutor.submit(container);
    }
}
