package dev.frankheijden.insights.api.concurrent.containers;

import dev.frankheijden.insights.api.InsightsPlugin;
import dev.frankheijden.insights.api.concurrent.ContainerExecutor;
import dev.frankheijden.insights.api.concurrent.ScanOptions;
import dev.frankheijden.insights.api.concurrent.storage.DistributionStorage;
import dev.frankheijden.insights.api.objects.chunk.ChunkCuboid;
import dev.frankheijden.insights.nms.core.ChunkEntity;
import dev.frankheijden.insights.nms.core.ChunkSection;
import dev.frankheijden.insights.nms.core.InsightsNMS;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class LoadedChunkContainer extends ChunkContainer {

    private final Chunk chunk;

    /**
     * Constructs a new LoadedChunkContainer, for scanning of a loaded chunk.
     */
    public LoadedChunkContainer(InsightsNMS nms, Chunk chunk, ChunkCuboid cuboid, ScanOptions options) {
        super(nms, chunk.getWorld(), chunk.getX(), chunk.getZ(), cuboid, options);
        this.chunk = chunk;
    }

    @Override
    public void getChunkSections(Consumer<@Nullable ChunkSection> sectionConsumer) {
        nms.getLoadedChunkSections(chunk, sectionConsumer);
    }

    @Override
    public void getChunkEntities(Consumer<@NotNull ChunkEntity> entityConsumer) {
        nms.getLoadedChunkEntities(chunk, entityConsumer);
    }

    @Override
    public CompletableFuture<DistributionStorage> scan(ContainerExecutor executor) {
        if (Bukkit.isOwnedByCurrentRegion(chunk.getWorld(), chunkX, chunkZ)) {
            return CompletableFuture.completedFuture(this.get());
        }

        CompletableFuture<DistributionStorage> future = new CompletableFuture<>();
        Bukkit.getRegionScheduler().execute(InsightsPlugin.getInstance(), chunk.getWorld(), chunkX, chunkZ, () -> {
            try {
                future.complete(this.get());
            } catch (Throwable th) {
                future.completeExceptionally(th);
            }
        });
        return future.orTimeout(executor.getTimeout(), TimeUnit.MILLISECONDS);
    }
}
