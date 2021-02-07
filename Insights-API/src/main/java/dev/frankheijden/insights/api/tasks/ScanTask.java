package dev.frankheijden.insights.api.tasks;

import dev.frankheijden.insights.api.InsightsPlugin;
import dev.frankheijden.insights.api.concurrent.ChunkContainerExecutor;
import dev.frankheijden.insights.api.concurrent.storage.Distribution;
import dev.frankheijden.insights.api.concurrent.storage.DistributionStorage;
import dev.frankheijden.insights.api.config.Messages;
import dev.frankheijden.insights.api.config.notifications.ProgressNotification;
import dev.frankheijden.insights.api.objects.chunk.ChunkPart;
import dev.frankheijden.insights.api.objects.chunk.ChunkLocation;
import dev.frankheijden.insights.api.utils.MaterialUtils;
import dev.frankheijden.insights.api.utils.StringUtils;
import io.papermc.lib.PaperLib;
import org.bukkit.Chunk;
import org.bukkit.Material;
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

    private final InsightsPlugin plugin;
    private final ChunkContainerExecutor executor;
    private final Queue<ChunkPart> scanQueue;
    private final Queue<Chunk> chunkQueue;
    private final DistributionStorage distributionStorage;
    private final int chunksPerIteration;
    private final Consumer<Info> infoConsumer;
    private final long infoTimeout;
    private final Consumer<DistributionStorage> distributionConsumer;
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
     * Scans the defined chunks for a given player, looking for materials.
     * The output of the task (when it completes) will be displayed to the user.
     */
    public static void scanAndDisplay(
            InsightsPlugin plugin,
            Player player,
            Collection<? extends ChunkPart> chunkParts,
            Set<Material> materials,
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

        // Create a notification for the task
        ProgressNotification notification = plugin.getNotifications().getCachedProgress(
                player.getUniqueId(),
                Messages.Key.SCAN_PROGRESS
        );
        notification.add(player);

        // Notify about scan start
        plugin.getMessages().getMessage(Messages.Key.SCAN_START)
                .replace(
                        "count", StringUtils.pretty(chunkCount)
                )
                .color()
                .sendTo(player);

        // Start the scan
        final long start = System.nanoTime();
        ScanTask.scan(plugin, chunkParts, info -> {
            // Update the notification with progress
            double progress = (double) info.getChunksDone() / (double) info.getChunks();
            notification.progress(progress)
                    .create()
                    .replace("percentage", StringUtils.prettyOneDecimal(progress * 100.))
                    .color()
                    .send();
        }, storage -> {
            // The time it took to generate the results
            @SuppressWarnings("VariableDeclarationUsageDistance")
            long millis = (System.nanoTime() - start) / 1000000L;

            // Send header
            Messages messages = plugin.getMessages();
            messages.getMessage(Messages.Key.SCAN_FINISH_HEADER).color().sendTo(player);

            Distribution<Material> distribution = storage.materials();

            // Check which materials we need to display & sort them based on their name.
            List<Material> displayMaterials = new ArrayList<>(materials == null ? distribution.keys() : materials);
            displayMaterials.sort(Comparator.comparing(Enum::name));

            // Send each entry
            for (Material material : displayMaterials) {
                // Only display format if nonzero, or displayZeros is set to true.
                int count = distribution.count(material);
                if (count == 0 && !displayZeros) continue;

                messages.getMessage(Messages.Key.SCAN_FINISH_FORMAT)
                        .replace(
                                "entry", MaterialUtils.pretty(material),
                                "count", StringUtils.pretty(count)
                        )
                        .color()
                        .sendTo(player);
            }

            // Send the footer
            messages.getMessage(Messages.Key.SCAN_FINISH_FOOTER)
                    .replace(
                            "chunks", StringUtils.pretty(chunkCount),
                            "blocks", StringUtils.pretty(chunkCount * 256 * 16 * 16),
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
            // Scan each chunk, merging the result with the distributionMap.
            executor.submit(chunkQueue.poll()).thenAccept(storage -> {
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
