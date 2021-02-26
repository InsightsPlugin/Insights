package dev.frankheijden.insights.api.tasks;

import dev.frankheijden.insights.api.InsightsPlugin;
import dev.frankheijden.insights.api.concurrent.ChunkContainerExecutor;
import dev.frankheijden.insights.api.concurrent.ScanOptions;
import dev.frankheijden.insights.api.concurrent.storage.DistributionStorage;
import dev.frankheijden.insights.api.config.Messages;
import dev.frankheijden.insights.api.config.notifications.ProgressNotification;
import dev.frankheijden.insights.api.objects.chunk.ChunkCuboid;
import dev.frankheijden.insights.api.objects.chunk.ChunkPart;
import dev.frankheijden.insights.api.objects.chunk.ChunkLocation;
import dev.frankheijden.insights.api.objects.wrappers.ScanObject;
import dev.frankheijden.insights.api.utils.EnumUtils;
import dev.frankheijden.insights.api.utils.StringUtils;
import io.papermc.lib.PaperLib;
import org.bukkit.Chunk;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class ScanTask implements Runnable {

    private static final Set<UUID> scanners = new HashSet<>();
    private static final ScanOptions options = ScanOptions.newBuilder().entities().build();

    private final InsightsPlugin plugin;
    private final ChunkContainerExecutor executor;
    private final Queue<ChunkPart> scanQueue;
    private final Queue<LoadedChunk> chunkQueue;
    private final DistributionStorage distributionStorage;
    private final int chunksPerIteration;
    private final Consumer<Info> infoConsumer;
    private final long infoTimeout;
    private final Consumer<DistributionStorage> distributionConsumer;
    private final AtomicInteger iterationChunks;
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
            Consumer<DistributionStorage> distributionConsumer
    ) {
        this.plugin = plugin;
        this.executor = plugin.getChunkContainerExecutor();
        this.scanQueue = new LinkedList<>(chunkParts);
        this.chunkQueue = new ConcurrentLinkedQueue<>();
        this.distributionStorage = new DistributionStorage();
        this.chunksPerIteration = chunksPerIteration;
        this.infoConsumer = infoConsumer;
        this.infoTimeout = infoTimeoutMillis * 1000000L; // Convert to nanos
        this.distributionConsumer = distributionConsumer;
        this.iterationChunks = new AtomicInteger(chunksPerIteration);
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
            Consumer<DistributionStorage> distributionConsumer
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

    /**
     * Creates a new ScanTask to scan a collection of ChunkPart's.
     * Notifies the user with a ProgressNotification for the task.
     * When this task completes, the consumer is called on the main thread.
     */
    public static void scan(
            InsightsPlugin plugin,
            Player player,
            Collection<? extends ChunkPart> chunkParts,
            Consumer<DistributionStorage> distributionConsumer
    ) {
        // Create a notification for the task
        ProgressNotification notification = plugin.getNotifications().getCachedProgress(
                player.getUniqueId(),
                Messages.Key.SCAN_PROGRESS
        );
        notification.add(player);

        new ScanTask(
                plugin,
                chunkParts,
                plugin.getSettings().SCANS_CHUNKS_PER_ITERATION,
                info -> {
                    // Update the notification with progress
                    double progress = (double) info.getChunksDone() / (double) info.getChunks();
                    notification.progress(progress)
                            .create()
                            .replace(
                                    "percentage", StringUtils.prettyOneDecimal(progress * 100.),
                                    "count", StringUtils.pretty(info.getChunksDone()),
                                    "total", StringUtils.pretty(info.getChunks())
                            )
                            .color()
                            .send();
                },
                plugin.getSettings().SCANS_INFO_INTERVAL_MILLIS,
                distributionConsumer
        ).start();
    }

    /**
     * Scans the defined chunks for a given player, looking for materials.
     * The output of the task (when it completes) will be displayed to the user.
     */
    public static void scanAndDisplay(
            InsightsPlugin plugin,
            Player player,
            Collection<? extends ChunkPart> chunkParts,
            Set<? extends ScanObject<?>> items,
            boolean displayZeros
    ) {
        UUID uuid = player.getUniqueId();

        // If the player is already scanning, tell them they can't run two scans.
        if (scanners.contains(uuid)) {
            plugin.getMessages().getMessage(Messages.Key.SCAN_ALREADY_SCANNING).color().sendTo(player);
            return;
        }

        // Add the player to the scanners
        scanners.add(uuid);

        int chunkCount = chunkParts.size();

        // Notify about scan start
        plugin.getMessages().getMessage(Messages.Key.SCAN_START)
                .replace(
                        "count", StringUtils.pretty(chunkCount)
                )
                .color()
                .sendTo(player);

        // Start the scan
        final long start = System.nanoTime();
        ScanTask.scan(plugin, player, chunkParts, storage -> {
            // The time it took to generate the results
            @SuppressWarnings("VariableDeclarationUsageDistance")
            long millis = (System.nanoTime() - start) / 1000000L;

            // Send header
            Messages messages = plugin.getMessages();
            messages.getMessage(Messages.Key.SCAN_FINISH_HEADER).color().sendTo(player);

            // Check which items we need to display & sort them based on their name.
            List<ScanObject<?>> displayItems = new ArrayList<>(items == null ? storage.keys() : items);
            displayItems.sort(Comparator.comparing(ScanObject::name));

            // Send each entry
            for (ScanObject<?> item : displayItems) {
                // Only display format if nonzero, or displayZeros is set to true.
                int count = storage.count(item);
                if (count == 0 && !displayZeros) continue;

                messages.getMessage(Messages.Key.SCAN_FINISH_FORMAT)
                        .replace(
                                "entry", EnumUtils.pretty(item.getObject()),
                                "count", StringUtils.pretty(count)
                        )
                        .color()
                        .sendTo(player);
            }

            // Send the footer
            messages.getMessage(Messages.Key.SCAN_FINISH_FOOTER)
                    .replace(
                            "chunks", StringUtils.pretty(chunkCount),
                            "blocks", StringUtils.pretty(storage.materials().count()),
                            "entities", StringUtils.pretty(storage.entities().count()),
                            "time", StringUtils.pretty(Duration.ofMillis(millis))
                    )
                    .color()
                    .sendTo(player);

            // Remove player from scanners
            scanners.remove(uuid);
        });
    }

    private void start() {
        BukkitScheduler scheduler = plugin.getServer().getScheduler();
        task = scheduler.runTaskTimer(plugin, this, 0, plugin.getSettings().SCANS_ITERATION_INTERVAL_TICKS);
    }

    private void cancel() {
        if (task != null) {
            task.cancel();
            sendInfo();
            distributionConsumer.accept(distributionStorage);
        }
    }

    @Override
    public void run() {
        // Empty the queue with available chunks (after loading)
        while (!chunkQueue.isEmpty()) {
            LoadedChunk loadedChunk = chunkQueue.poll();

            // Scan each chunk, merging the result with the distributionMap.
            executor.submit(loadedChunk.chunk, loadedChunk.chunkCuboid, options).thenAccept(storage -> {
                storage.mergeRight(distributionStorage);
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

        // Check how many chunks we did previous iteration,
        // and do as many chunks as 'chunksPerIteration' allows us to do.
        int previouslyDone = iterationChunks.get();
        int chunkIterations = Math.min(previouslyDone, chunksPerIteration);
        if (chunkIterations == 0) return;
        iterationChunks.addAndGet(-chunkIterations);

        // Iterate 'chunkIterations' times
        for (int i = 0; i < chunkIterations; i++) {
            // Note: we can't cancel the task here just yet,
            // because some chunks might still need scanning (after loading).
            if (scanQueue.isEmpty()) break;

            // Load the chunk
            ChunkPart chunkPart = scanQueue.poll();
            ChunkLocation loc = chunkPart.getChunkLocation();
            PaperLib.getChunkAtAsync(loc.getWorld(), loc.getX(), loc.getZ(), false).thenAccept(chunk -> {
                // Add the chunk to the scan queue for the main thread to fetch the ChunkSnapshot.
                if (chunk != null) {
                    chunkQueue.add(new LoadedChunk(chunk, chunkPart.getChunkCuboid()));
                } else {
                    chunks.incrementAndGet();
                }
                iterationChunks.incrementAndGet();
            }).exceptionally(err -> {
                // When the chunk couldn't be loaded (e.g. not generated), just skip it
                iterationChunks.incrementAndGet();
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

    private static final class LoadedChunk {

        private final Chunk chunk;
        private final ChunkCuboid chunkCuboid;

        private LoadedChunk(Chunk chunk, ChunkCuboid chunkCuboid) {
            this.chunk = chunk;
            this.chunkCuboid = chunkCuboid;
        }
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
