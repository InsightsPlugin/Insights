package dev.frankheijden.insights.api.concurrent;

import dev.frankheijden.insights.api.InsightsPlugin;
import dev.frankheijden.insights.api.concurrent.containers.ChunkContainer;
import dev.frankheijden.insights.api.concurrent.containers.ContainerPriority;
import dev.frankheijden.insights.api.concurrent.containers.LoadedChunkContainer;
import dev.frankheijden.insights.api.concurrent.containers.RunnableContainer;
import dev.frankheijden.insights.api.concurrent.containers.SupplierContainer;
import dev.frankheijden.insights.api.concurrent.containers.UnloadedChunkContainer;
import dev.frankheijden.insights.api.concurrent.storage.Storage;
import dev.frankheijden.insights.api.concurrent.storage.WorldStorage;
import dev.frankheijden.insights.api.concurrent.tracker.WorldChunkScanTracker;
import dev.frankheijden.insights.api.objects.chunk.ChunkCuboid;
import org.bukkit.Chunk;
import org.bukkit.World;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

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

    public CompletableFuture<Storage> submit(World world, int x, int z) {
        return submit(world, x, z, ScanOptions.all());
    }

    public CompletableFuture<Storage> submit(Chunk chunk, ScanOptions options) {
        return submit(chunk, ChunkCuboid.maxCuboid(chunk.getWorld()), options);
    }

    public CompletableFuture<Storage> submit(World world, int x, int z, ScanOptions options) {
        return submit(world, x, z, ChunkCuboid.maxCuboid(world), options);
    }

    public CompletableFuture<Storage> submit(Chunk chunk, ChunkCuboid cuboid, ScanOptions options) {
        return submit(new LoadedChunkContainer(chunk, cuboid, options, ContainerPriority.MEDIUM), options);
    }

    public CompletableFuture<Storage> submit(
            Chunk chunk,
            ChunkCuboid cuboid,
            ScanOptions options,
            ContainerPriority priority
    ) {
        return submit(new LoadedChunkContainer(chunk, cuboid, options, priority), options);
    }

    public CompletableFuture<Storage> submit(World world, int x, int z, ChunkCuboid cuboid, ScanOptions options) {
        return submit(new UnloadedChunkContainer(world, x, z, cuboid, options, ContainerPriority.MEDIUM), options);
    }

    public CompletableFuture<Storage> submit(
            World world,
            int x,
            int z,
            ChunkCuboid cuboid,
            ScanOptions options,
            ContainerPriority priority
    ) {
        return submit(new UnloadedChunkContainer(world, x, z, cuboid, options, priority), options);
    }

    /**
     * Submits a ChunkContainer for scanning, returning a DistributionStorage object.
     * DistributionStorage is essentially merged from the scan result and given entities Distribution.
     */
    public CompletableFuture<Storage> submit(ChunkContainer container, ScanOptions options) {
        UUID worldUid = container.getWorld().getUID();
        long chunkKey = container.getChunkKey();
        if (options.track()) {
            scanTracker.set(worldUid, chunkKey, true);
        }

        return submit(container).thenApply(storage -> {
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

    @Override
    public void shutdown() {
        containerExecutor.shutdown();
    }
}
