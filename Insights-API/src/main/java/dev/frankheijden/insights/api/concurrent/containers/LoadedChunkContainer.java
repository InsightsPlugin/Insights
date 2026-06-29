package dev.frankheijden.insights.api.concurrent.containers;

import dev.frankheijden.insights.api.InsightsPlugin;
import dev.frankheijden.insights.api.concurrent.ScanOptions;
import dev.frankheijden.insights.api.objects.chunk.ChunkCuboid;
import dev.frankheijden.insights.nms.core.ChunkEntity;
import dev.frankheijden.insights.nms.core.ChunkSection;
import dev.frankheijden.insights.nms.core.InsightsNMS;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.concurrent.CompletableFuture;
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
        runOnOwningRegion(() -> nms.getLoadedChunkSections(chunk, sectionConsumer));
    }

    @Override
    public void getChunkEntities(Consumer<@NotNull ChunkEntity> entityConsumer) {
        runOnOwningRegion(() -> nms.getLoadedChunkEntities(chunk, entityConsumer));
    }

    /**
     * Runs the given chunk-reading task on the region thread which owns this chunk, blocking the
     * calling (worker) thread until it has completed.
     *
     * <p>This synchronization is required: {@link ChunkContainer#get()} reads the populated
     * material/entity maps immediately after invoking these methods. If the read were merely
     * scheduled on the region thread (fire-and-forget), {@code get()} would observe still-empty
     * maps and return a distribution counting 0, causing limits to be bypassed on every chunk
     * that is rescanned after being (re)loaded.
     */
    private void runOnOwningRegion(Runnable task) {
        if (Bukkit.isOwnedByCurrentRegion(chunk.getWorld(), chunkX, chunkZ)) {
            task.run();
            return;
        }

        CompletableFuture<Void> future = new CompletableFuture<>();
        Bukkit.getRegionScheduler().execute(InsightsPlugin.getInstance(), chunk.getWorld(), chunkX, chunkZ, () -> {
            try {
                task.run();
                future.complete(null);
            } catch (Throwable th) {
                future.completeExceptionally(th);
            }
        });
        future.join();
    }
}
