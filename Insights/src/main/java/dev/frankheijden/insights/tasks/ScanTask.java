package dev.frankheijden.insights.tasks;

import dev.frankheijden.insights.api.InsightsPlugin;
import dev.frankheijden.insights.api.concurrent.ChunkContainerExecutor;
import dev.frankheijden.insights.api.objects.chunk.ChunkPart;
import dev.frankheijden.insights.api.objects.chunk.ChunkLocation;
import dev.frankheijden.insights.api.utils.MapUtils;
import io.papermc.lib.PaperLib;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class ScanTask implements Runnable {

    private final InsightsPlugin plugin;
    private final ChunkContainerExecutor executor;
    private final Queue<ChunkPart> scanQueue;
    private final Queue<Chunk> chunkQueue;
    private final Map<Material, Integer> distributionMap;
    private final int chunksPerIteration;
    private final Consumer<Info> infoConsumer;
    private final long infoTimeout;
    private final Consumer<Map<Material, Integer>> distributionConsumer;
    private final AtomicInteger iterationChunks = new AtomicInteger(0);
    private final AtomicInteger chunks = new AtomicInteger(0);
    private final int chunkCount;
    private long lastInfo = 0;
    private BukkitTask task;

    /**
     * Creates a new ScanTask to scan a collection of ChunkPart's.
     * When this task completes, the consumer is called on the main thread.
     */
    private ScanTask(
            InsightsPlugin plugin,
            Collection<? extends ChunkPart> chunkParts,
            int chunksPerIteration,
            Consumer<Info> infoConsumer,
            long infoTimeoutMillis,
            Consumer<Map<Material, Integer>> distributionConsumer
    ) {
        this.plugin = plugin;
        this.executor = plugin.getChunkContainerExecutor();
        this.scanQueue = new LinkedList<>(chunkParts);
        this.chunkQueue = new ConcurrentLinkedQueue<>();
        this.distributionMap = new ConcurrentHashMap<>();
        this.chunksPerIteration = chunksPerIteration;
        this.infoConsumer = infoConsumer;
        this.infoTimeout = infoTimeoutMillis * 1000000L; // Convert to nanos
        this.distributionConsumer = distributionConsumer;
        this.chunkCount = chunkParts.size();
    }

    /**
     * Creates a new ScanTask to scan a collection of ChunkPart's.
     * When this task completes, the consumer is called on the main thread.
     */
    public static void scan(
            InsightsPlugin plugin,
            Collection<? extends ChunkPart> chunkParts,
            Consumer<Info> infoConsumer,
            Consumer<Map<Material, Integer>> distributionConsumer
    ) {
        new ScanTask(
                plugin,
                chunkParts,
                plugin.getSettings().SCANS_CHUNKS_PER_ITERATION,
                infoConsumer,
                plugin.getSettings().SCANS_INFO_INTERVAL_MILLIS,
                distributionConsumer
        ).start();
    }

    private void start() {
        BukkitScheduler scheduler = plugin.getServer().getScheduler();
        task = scheduler.runTaskTimer(plugin, this, 0, plugin.getSettings().SCANS_ITERATION_INTERVAL_TICKS);
    }

    private void cancel() {
        if (task != null) {
            task.cancel();
            sendInfo();
            distributionConsumer.accept(distributionMap);
        }
    }

    @Override
    public void run() {
        // Empty the queue with available chunks (after loading)
        while (!chunkQueue.isEmpty()) {
            // Scan each chunk, merging the result with the distributionMap.
            executor.submit(chunkQueue.poll(), false).thenAccept(map -> {
                MapUtils.merge(distributionMap, map, Integer::sum);
                chunks.incrementAndGet();
            });
        }

        // Check if we can send an information notification
        checkNotify();

        // If the amount of chunks done equals the chunk count, we're done
        if (chunks.get() == chunkCount) {
            cancel();
            return;
        }

        // Check if we have gotten all chunks from previous iteration
        if (iterationChunks.compareAndSet(0, chunksPerIteration)) return;

        // Iterate 'chunksPerIteration' times
        for (int i = 0; i < chunksPerIteration; i++) {
            // Note: we can't cancel the task here just yet,
            // because some chunks might still need scanning (after loading).
            if (scanQueue.isEmpty()) break;

            // Load the chunk
            ChunkPart cuboidPart = scanQueue.poll();
            ChunkLocation loc = cuboidPart.getChunkLocation();
            PaperLib.getChunkAtAsync(loc.getWorld(), loc.getX(), loc.getZ(), false).thenAccept(chunk -> {
                // Add the chunk to the scan queue for the main thread to fetch the ChunkSnapshot.
                chunkQueue.add(chunk);
                iterationChunks.decrementAndGet();
            }).exceptionally(err -> {
                // When the chunk couldn't be loaded (e.g. not generated), just skip it
                iterationChunks.decrementAndGet();
                chunks.incrementAndGet();
                return null;
            });
        }
    }

    private void checkNotify() {
        long now = System.nanoTime();
        if (lastInfo + infoTimeout < now) {
            lastInfo = now;
            sendInfo();
        }
    }

    private void sendInfo() {
        infoConsumer.accept(new Info(chunks.get(), chunkCount));
    }

    public static final class Info {
        private final int chunksDone;
        private final int chunks;

        public Info(int chunksDone, int chunks) {
            this.chunksDone = chunksDone;
            this.chunks = chunks;
        }

        public int getChunksDone() {
            return chunksDone;
        }

        public int getChunks() {
            return chunks;
        }
    }
}
