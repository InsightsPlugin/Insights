package dev.frankheijden.insights.api.tasks;

import dev.frankheijden.insights.api.InsightsPlugin;
import dev.frankheijden.insights.api.concurrent.ChunkContainerExecutor;
import dev.frankheijden.insights.api.concurrent.ScanOptions;
import dev.frankheijden.insights.api.concurrent.storage.DistributionStorage;
import dev.frankheijden.insights.api.concurrent.storage.Storage;
import dev.frankheijden.insights.api.config.Messages;
import dev.frankheijden.insights.api.config.notifications.ProgressNotification;
import dev.frankheijden.insights.api.objects.chunk.ChunkLocation;
import dev.frankheijden.insights.api.objects.chunk.ChunkPart;
import dev.frankheijden.insights.api.objects.wrappers.ScanObject;
import dev.frankheijden.insights.api.util.TriConsumer;
import dev.frankheijden.insights.api.utils.EnumUtils;
import dev.frankheijden.insights.api.utils.StringUtils;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;
import java.time.Duration;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.logging.Level;

public class ScanTask<R> implements Runnable {

    private static final Set<UUID> scanners = new HashSet<>();

    private final InsightsPlugin plugin;
    private final ChunkContainerExecutor executor;
    private final Iterator<? extends ChunkPart> scanQueue;
    private final ScanOptions options;
    private final int chunksPerIteration;
    private final Consumer<Info> infoConsumer;
    private final long infoTimeout;
    private final R result;
    private final TriConsumer<Storage, ChunkLocation, R> resultMerger;
    private final Consumer<R> resultConsumer;
    private final AtomicInteger iterationChunks;
    private final AtomicInteger chunks = new AtomicInteger(0);
    private final AtomicBoolean completedExceptionally = new AtomicBoolean();
    private final int chunkCount;
    private long lastInfo = 0;
    private BukkitTask task;

    /**
     * Creates a new ScanTask to scan a collection of ChunkPart's.
     * When this task completes, the consumer is called on the main thread.
     */
    private ScanTask(
            InsightsPlugin plugin,
            Iterable<? extends ChunkPart> chunkParts,
            int chunkCount,
            ScanOptions options,
            int chunksPerIteration,
            Consumer<Info> infoConsumer,
            long infoTimeoutMillis,
            Supplier<R> resultSupplier,
            TriConsumer<Storage, ChunkLocation, R> resultMerger,
            Consumer<R> resultConsumer
    ) {
        this.plugin = plugin;
        this.executor = plugin.getChunkContainerExecutor();
        this.scanQueue = chunkParts.iterator();
        this.options = options;
        this.chunksPerIteration = chunksPerIteration;
        this.infoConsumer = infoConsumer;
        this.infoTimeout = infoTimeoutMillis * 1000000L; // Convert to nanos
        this.result = resultSupplier.get();
        this.resultMerger = resultMerger;
        this.resultConsumer = resultConsumer;
        this.iterationChunks = new AtomicInteger(chunksPerIteration);
        this.chunkCount = chunkCount;
    }

    /**
     * Creates a new ScanTask to scan a collection of ChunkPart's.
     * When this task completes, the consumer is called on the main thread.
     */
    public static void scan(
            InsightsPlugin plugin,
            Iterable<? extends ChunkPart> chunkParts,
            int chunkCount,
            ScanOptions options,
            Consumer<Info> infoConsumer,
            Consumer<DistributionStorage> distributionConsumer
    ) {
        new ScanTask<>(
                plugin,
                chunkParts,
                chunkCount,
                options,
                plugin.getSettings().SCANS_CHUNKS_PER_ITERATION,
                infoConsumer,
                plugin.getSettings().SCANS_INFO_INTERVAL_MILLIS,
                DistributionStorage::new,
                (storage, loc, acc) -> storage.mergeRight(acc),
                distributionConsumer
        ).start();
    }

    /**
     * Creates a new ScanTask to scan a collection of ChunkPart's.
     * Notifies the user with a ProgressNotification for the task.
     * When this task completes, the consumer is called on the main thread.
     */
    public static <R> void scan(
            InsightsPlugin plugin,
            Player player,
            Iterable<? extends ChunkPart> chunkParts,
            int chunkCount,
            ScanOptions options,
            boolean notify,
            Supplier<R> resultSupplier,
            TriConsumer<Storage, ChunkLocation, R> resultMerger,
            Consumer<R> resultConsumer
    ) {
        // Create a notification for the task
        ProgressNotification notification = plugin.getNotifications().getCachedProgress(
                player.getUniqueId(),
                Messages.Key.SCAN_PROGRESS
        );
        if (notify) {
            notification.add(player);
        }

        new ScanTask<>(
                plugin,
                chunkParts,
                chunkCount,
                options,
                plugin.getSettings().SCANS_CHUNKS_PER_ITERATION,
                info -> {
                    if (!notify) return;

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
                resultSupplier,
                resultMerger,
                resultConsumer
        ).start();
    }

    /**
     * Scans the defined chunks for a given player, looking for materials.
     * The output of the task (when it completes) will be displayed to the user.
     */
    public static void scanAndDisplay(
            InsightsPlugin plugin,
            Player player,
            Iterable<? extends ChunkPart> chunkParts,
            int chunkCount,
            ScanOptions options,
            Set<? extends ScanObject<?>> items,
            boolean displayZeros
    ) {
        long start = System.nanoTime();

        scanAndDisplay(
                plugin,
                player,
                chunkParts,
                chunkCount,
                options,
                DistributionStorage::new,
                (storage, loc, acc) -> storage.mergeRight(acc),
                storage -> {
                    // The time it took to generate the results
                    @SuppressWarnings("VariableDeclarationUsageDistance")
                    long millis = (System.nanoTime() - start) / 1000000L;

                    var messages = plugin.getMessages();

                    // Check which items we need to display & sort them based on their name.
                    ScanObject<?>[] displayItems = (items == null ? storage.keys() : items).stream()
                            .filter(item -> storage.count(item) != 0 || displayZeros)
                            .sorted(Comparator.comparing(ScanObject::name))
                            .toArray(ScanObject[]::new);

                    var footer = messages.getMessage(Messages.Key.SCAN_FINISH_FOOTER).replace(
                            "chunks", StringUtils.pretty(chunkCount),
                            "blocks", StringUtils.pretty(storage.count(s -> s.getType() == ScanObject.Type.MATERIAL)),
                            "entities", StringUtils.pretty(storage.count(s -> s.getType() == ScanObject.Type.ENTITY)),
                            "time", StringUtils.pretty(Duration.ofMillis(millis))
                    );

                    var message = messages.createPaginatedMessage(
                            messages.getMessage(Messages.Key.SCAN_FINISH_HEADER),
                            Messages.Key.SCAN_FINISH_FORMAT,
                            footer,
                            displayItems,
                            storage::count,
                            item -> EnumUtils.pretty(item.getObject())
                    );

                    plugin.getScanHistory().setHistory(player.getUniqueId(), message);
                    message.sendTo(player, 0);
                }
        );
    }

    /**
     * Scans the defined chunks for a given player, looking for materials.
     * The output of the task (when it completes) will be displayed to the user.
     */
    public static <R> void scanAndDisplay(
            InsightsPlugin plugin,
            Player player,
            Iterable<? extends ChunkPart> chunkParts,
            int chunkCount,
            ScanOptions options,
            Supplier<R> resultSupplier,
            TriConsumer<Storage, ChunkLocation, R> resultMerger,
            Consumer<R> resultConsumer
    ) {
        var uuid = player.getUniqueId();

        // If the player is already scanning, tell them they can't run two scans.
        if (scanners.contains(uuid)) {
            plugin.getMessages().getMessage(Messages.Key.SCAN_ALREADY_SCANNING).color().sendTo(player);
            return;
        }

        // Add the player to the scanners
        scanners.add(uuid);

        // Notify about scan start
        plugin.getMessages().getMessage(Messages.Key.SCAN_START)
                .replace(
                        "count", StringUtils.pretty(chunkCount)
                )
                .color()
                .sendTo(player);

        // Start the scan
        ScanTask.scan(
                plugin,
                player,
                chunkParts,
                chunkCount,
                options,
                true,
                resultSupplier,
                resultMerger,
                resultConsumer.andThen(r -> scanners.remove(uuid))
        );
    }

    /**
     * Scans the defined chunks for a given player, looking for materials.
     * The output of the task (when it completes) will be displayed to the user.
     */
    public static void scanAndDisplayGroupedByChunk(
            InsightsPlugin plugin,
            Player player,
            Iterable<? extends ChunkPart> chunkParts,
            int chunkCount,
            ScanOptions options,
            Set<? extends ScanObject<?>> items,
            boolean displayZeros
    ) {
        long start = System.nanoTime();

        scanAndDisplay(
                plugin,
                player,
                chunkParts,
                chunkCount,
                options,
                (Supplier<ConcurrentHashMap<ChunkLocation, Storage>>) ConcurrentHashMap::new,
                (storage, loc, map) -> map.put(loc, storage),
                map -> {
                    // The time it took to generate the results
                    @SuppressWarnings("VariableDeclarationUsageDistance")
                    long millis = (System.nanoTime() - start) / 1000000L;

                    var messages = plugin.getMessages();

                    // Check which items we need to display & sort them based on their name.
                    ChunkLocation[] keys = map.entrySet().stream()
                            .filter(entry -> {
                                var storage = entry.getValue();
                                return displayZeros || storage.count(items == null ? storage.keys() : items) != 0;
                            })
                            .sorted(Comparator.<Map.Entry<ChunkLocation, Storage>>comparingInt(entry -> {
                                var storage = entry.getValue();
                                return storage.count(items == null ? storage.keys() : items);
                            }).reversed())
                            .map(Map.Entry::getKey)
                            .toArray(ChunkLocation[]::new);

                    int blockCount = map.values()
                            .stream()
                            .mapToInt(storage -> storage.count(i -> i.getType() == ScanObject.Type.MATERIAL))
                            .sum();
                    int entityCount = map.values()
                            .stream()
                            .mapToInt(storage -> storage.count(i -> i.getType() == ScanObject.Type.ENTITY))
                            .sum();

                    var footer = messages.getMessage(Messages.Key.SCAN_FINISH_FOOTER).replace(
                            "chunks", StringUtils.pretty(chunkCount),
                            "blocks", StringUtils.pretty(blockCount),
                            "entities", StringUtils.pretty(entityCount),
                            "time", StringUtils.pretty(Duration.ofMillis(millis))
                    );

                    var message = messages.<ChunkLocation>createPaginatedMessage(
                            messages.getMessage(Messages.Key.SCAN_FINISH_HEADER),
                            Messages.Key.SCAN_FINISH_FORMAT,
                            footer,
                            keys,
                            key -> {
                                var storage = map.get(key);
                                return storage.count(items == null ? storage.keys() : items);
                            },
                            key -> {
                                var worldName = key.getWorld().getName();
                                var x = Integer.toString(key.getX());
                                var z = Integer.toString(key.getZ());

                                return messages.getMessage(Messages.Key.SCAN_FINISH_CHUNK_FORMAT).replace(
                                        "world", worldName,
                                        "chunk-x", x,
                                        "chunk-z", z
                                ).getMessage().orElse(worldName + " @ " + x + ", " + z);
                            }
                    );

                    plugin.getScanHistory().setHistory(player.getUniqueId(), message);
                    message.sendTo(player, 0);
                }
        );
    }

    private void start() {
        BukkitScheduler scheduler = plugin.getServer().getScheduler();
        task = scheduler.runTaskTimer(plugin, this, 0, plugin.getSettings().SCANS_ITERATION_INTERVAL_TICKS);
    }

    private void cancel() {
        if (task != null) {
            task.cancel();
            if (completedExceptionally.get()) {
                resultConsumer.accept(null);
            } else {
                sendInfo();
                resultConsumer.accept(result);
            }
        }
    }

    @Override
    public void run() {
        // Check if we can send an information notification
        checkNotify();

        // If the amount of chunks done equals the chunk count, we're done
        // Or if the task has an exception
        if (chunks.get() == chunkCount || completedExceptionally.get()) {
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
        for (var i = 0; i < chunkIterations; i++) {
            // Note: we can't cancel the task here just yet,
            // because some chunks might still need scanning (after loading).
            if (!scanQueue.hasNext()) break;

            // Load the chunk
            var chunkPart = scanQueue.next();
            var loc = chunkPart.getChunkLocation();
            var world = loc.getWorld();

            CompletableFuture<Storage> storageFuture;
            if (world.isChunkLoaded(loc.getX(), loc.getZ())) {
                storageFuture = executor.submit(
                        world.getChunkAt(loc.getX(), loc.getZ()),
                        chunkPart.getChunkCuboid(),
                        options
                );
            } else {
                storageFuture = executor.submit(
                        loc.getWorld(),
                        loc.getX(),
                        loc.getZ(),
                        chunkPart.getChunkCuboid(),
                        options
                );
            }

            storageFuture
                    .thenAccept(storage -> resultMerger.accept(storage, loc, result))
                    .thenRun(() -> {
                        iterationChunks.incrementAndGet();
                        chunks.incrementAndGet();
                    })
                    .exceptionally(th -> {
                        if (!completedExceptionally.getAndSet(true)) {
                            plugin.getLogger().log(Level.SEVERE, th, th::getMessage);
                        }
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
