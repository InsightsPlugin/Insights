package dev.frankheijden.insights.api.concurrent;

import dev.frankheijden.insights.api.InsightsPlugin;
import dev.frankheijden.insights.api.concurrent.containers.ChunkContainer;
import dev.frankheijden.insights.api.concurrent.containers.RunnableContainer;
import dev.frankheijden.insights.api.concurrent.containers.SupplierContainer;
import dev.frankheijden.insights.api.concurrent.storage.Distribution;
import dev.frankheijden.insights.api.concurrent.storage.DistributionStorage;
import dev.frankheijden.insights.api.concurrent.storage.Storage;
import dev.frankheijden.insights.api.concurrent.storage.WorldStorage;
import dev.frankheijden.insights.api.concurrent.tracker.WorldChunkScanTracker;
import dev.frankheijden.insights.api.objects.chunk.ChunkCuboid;
import dev.frankheijden.insights.api.utils.ChunkUtils;
import org.bukkit.Chunk;
import org.bukkit.entity.EntityType;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Decorator class for ContainerExecutor to add chunk scanning functionality.
 */
public class ChunkContainerExecutor implements ContainerExecutor {

    private final ContainerExecutor containerExecutor;
    private final WorldStorage worldStorage;
    private final WorldChunkScanTracker scanTracker;

    /**
     * Constructs a new ChunkContainerExecutor.
     */
    public ChunkContainerExecutor(
            ContainerExecutor containerExecutor,
            WorldStorage worldStorage,
            WorldChunkScanTracker scanTracker
    ) {
        this.containerExecutor = containerExecutor;
        this.worldStorage = worldStorage;
        this.scanTracker = scanTracker;
    }

    public CompletableFuture<Storage> submit(Chunk chunk) {
        return submit(chunk, ScanOptions.all());
    }

    public CompletableFuture<Storage> submit(Chunk chunk, ScanOptions options) {
        return submit(chunk, ChunkCuboid.MAX, options);
    }

    /**
     * Submits a chunk for scanning, which will be scanned within given cuboid, and using given options.
     * Note: this method MUST be called on the main thread!
     */
    public CompletableFuture<Storage> submit(Chunk chunk, ChunkCuboid cuboid, ScanOptions options) {
        Distribution<EntityType> entities = options.entities()
                ? ChunkUtils.countEntities(chunk)
                : new Distribution<>(new ConcurrentHashMap<>());
        return submit(chunk, entities, chunk.getWorld().getUID(), cuboid, options);
    }

    /**
     * Submits a ChunkSnapshot for scanning, returning a DistributionStorage object.
     * DistributionStorage is essentially merged from the scan result and given entities Distribution.
     */
    public CompletableFuture<Storage> submit(
            Chunk chunk,
            Distribution<EntityType> entities,
            UUID worldUid,
            ChunkCuboid cuboid,
            ScanOptions options
    ) {
        long chunkKey = ChunkUtils.getKey(chunk);
        if (options.track()) {
            scanTracker.set(worldUid, chunkKey, true);
        }

        return submit(new ChunkContainer(chunk, worldUid, cuboid)).thenApply(materials -> {
            DistributionStorage storage = DistributionStorage.of(materials, entities);
            if (options.save()) worldStorage.getWorld(worldUid).put(chunkKey, storage);
            if (options.track()) scanTracker.set(worldUid, chunkKey, false);
            InsightsPlugin.getInstance().getMetricsManager().getChunkScanMetric().increment();
            return storage;
        });
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
