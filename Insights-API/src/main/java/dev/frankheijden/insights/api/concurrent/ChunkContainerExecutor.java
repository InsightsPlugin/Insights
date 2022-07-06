package dev.frankheijden.insights.api.concurrent;

import dev.frankheijden.insights.api.InsightsPlugin;
import dev.frankheijden.insights.api.concurrent.containers.ChunkContainer;
import dev.frankheijden.insights.api.concurrent.containers.LoadedChunkContainer;
import dev.frankheijden.insights.api.concurrent.containers.RunnableContainer;
import dev.frankheijden.insights.api.concurrent.containers.SupplierContainer;
import dev.frankheijden.insights.api.concurrent.containers.UnloadedChunkContainer;
import dev.frankheijden.insights.api.concurrent.storage.Storage;
import dev.frankheijden.insights.api.exceptions.ChunkCuboidOutOfBoundsException;
import dev.frankheijden.insights.api.objects.chunk.ChunkCuboid;
import dev.frankheijden.insights.api.objects.chunk.ChunkLocation;
import dev.frankheijden.insights.api.objects.chunk.ChunkPart;
import dev.frankheijden.insights.api.region.ChunkRegion;
import dev.frankheijden.insights.api.utils.ChunkUtils;
import org.bukkit.Chunk;
import java.util.concurrent.CompletableFuture;

/**
 * Decorator class for ContainerExecutor to add chunk scanning functionality.
 */
public class ChunkContainerExecutor implements ContainerExecutor {

    private final InsightsPlugin plugin;

    /**
     * Constructs a new ChunkContainerExecutor.
     */
    public ChunkContainerExecutor(InsightsPlugin plugin) {
        this.plugin = plugin;
    }

    public CompletableFuture<Storage> submit(Chunk chunk) {
        return submit(chunk, ScanOptions.all());
    }

    public CompletableFuture<Storage> submit(ChunkLocation chunkLocation) {
        return submit(chunkLocation, ScanOptions.all());
    }

    public CompletableFuture<Storage> submit(Chunk chunk, ScanOptions options) {
        return submit(chunk, ChunkCuboid.maxCuboid(chunk.getWorld()), options);
    }

    public CompletableFuture<Storage> submit(ChunkLocation chunkLocation, ScanOptions options) {
        return submit(chunkLocation, ChunkCuboid.maxCuboid(chunkLocation.world()), options);
    }

    public CompletableFuture<Storage> submit(Chunk chunk, ChunkCuboid cuboid, ScanOptions options) {
        return submit(new LoadedChunkContainer(chunk, cuboid, options), options);
    }

    public CompletableFuture<Storage> submit(ChunkLocation chunkLocation, ChunkCuboid cuboid, ScanOptions options) {
        return submit(new UnloadedChunkContainer(chunkLocation, cuboid, options), options);
    }

    /**
     * Submits a ChunkContainer for scanning, returning a DistributionStorage object.
     * DistributionStorage is essentially merged from the scan result and given entities Distribution.
     */
    public CompletableFuture<Storage> submit(ChunkContainer container, ScanOptions options) {
        var world = container.chunkLocation().world();
        var maxCuboid = ChunkCuboid.maxCuboid(world);
        if (!maxCuboid.contains(container.chunkCuboid())) {
            return CompletableFuture.failedFuture(new ChunkCuboidOutOfBoundsException(
                    container.chunkCuboid(),
                    maxCuboid
            ));
        }

        ChunkRegion chunkRegion = new ChunkRegion(
                container.chunkLocation().world().getUID(),
                ChunkUtils.uuidFromChunkKey(container.chunkLocation().key()),
                new ChunkPart(container.chunkLocation(), container.chunkCuboid())
        );
        if (options.track()) {
            plugin.regionManager().regionScanTracker().setQueued(chunkRegion, true);
        }

        return submit(container).thenApply(storage -> {
            if (options.save()) plugin.regionManager().regionStorage().put(chunkRegion, storage);
            if (options.track()) plugin.regionManager().regionScanTracker().setQueued(chunkRegion, false);

            var metricsManager = InsightsPlugin.getInstance().metricsManager();
            metricsManager.getChunkScanMetric().increment();
            metricsManager.getTotalBlocksScanned().add(container.chunkCuboid().getVolume());

            return storage;
        });
    }

    @Override
    public <T> CompletableFuture<T> submit(SupplierContainer<T> container) {
        return plugin.chunkContainerExecutor().submit(container);
    }

    @Override
    public CompletableFuture<Void> submit(RunnableContainer container) {
        return plugin.chunkContainerExecutor().submit(container);
    }

    @Override
    public void shutdown() {
        plugin.chunkContainerExecutor().shutdown();
    }
}
