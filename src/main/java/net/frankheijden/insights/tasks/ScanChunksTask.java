package net.frankheijden.insights.tasks;

import net.frankheijden.insights.Insights;
import net.frankheijden.insights.entities.ScanOptions;
import net.frankheijden.insights.entities.ScanResult;
import net.frankheijden.insights.enums.LogType;
import net.frankheijden.insights.enums.ScanType;
import net.frankheijden.insights.events.ScanCompleteEvent;
import net.frankheijden.insights.managers.*;
import net.frankheijden.insights.utils.*;
import org.bukkit.*;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.text.NumberFormat;
import java.util.*;
import java.util.concurrent.*;

public class ScanChunksTask implements Runnable {

    private static final Insights plugin = Insights.getInstance();
    private static final ScanManager scanManager = ScanManager.getInstance();
    private static final BossBarManager bossBarManager = BossBarManager.getInstance();

    private static final int NOTIFICATION_DELAY_SECONDS = 10;
    private static final int NOTIFICATION_DELAY_SPECIAL_MILLIS = 50;

    private final ScanOptions scanOptions;
    private final ScanResult scanResult;
    private final LoadChunksTask loadChunksTask;
    private final Queue<CompletableFuture<Chunk>> chunkQueue;

    private long startTime;
    private int taskID;
    private int chunksDone;
    private boolean run = true;

    private ScanChunksTaskSyncHelper scanChunksTaskSyncHelper = null;
    private final Queue<BlockState[]> blockStatesList;

    private long lastProgressMessageInChat;
    private long lastProgressMessageSpecial;
    private boolean isBossBar;
    private String progressMessage;
    private boolean canSendProgressMessage = false;

    private final boolean scanEntities;

    public ScanChunksTask(ScanOptions scanOptions, LoadChunksTask loadChunksTask) {
        this.scanOptions = scanOptions;
        this.loadChunksTask = loadChunksTask;
        if (scanOptions.getScanType() == ScanType.CUSTOM) {
            this.scanResult = new ScanResult(scanOptions.getMaterials(), scanOptions.getEntityTypes());
        } else {
            this.scanResult = new ScanResult();
        }

        this.chunkQueue = new ConcurrentLinkedQueue<>();
        this.blockStatesList = new ConcurrentLinkedQueue<>();
        this.chunksDone = 0;
        this.scanEntities = (scanOptions.getScanType() == ScanType.CUSTOM && scanOptions.getEntityTypes() != null)
                || scanOptions.getScanType() == ScanType.ALL
                || scanOptions.getScanType() == ScanType.ENTITY;
    }

    public void start(long startTime) {
        this.startTime = startTime;
        // Deprecated method because name could be misleading, its an asynchronous task.
        this.taskID = Bukkit.getServer().getScheduler().scheduleAsyncRepeatingTask(plugin, this, 0, 1);

        if (scanOptions.getScanType() == ScanType.TILE) {
            scanChunksTaskSyncHelper = new ScanChunksTaskSyncHelper(scanOptions, this);
            scanChunksTaskSyncHelper.start();
        }
    }

    private void stop() {
        forceStop();

        if (scanOptions.getUUID() != null) {
            Player player = Bukkit.getPlayer(scanOptions.getUUID());
            if (player != null || scanOptions.isConsole()) {
                if (scanResult.getSize() > 0) {
                    sendMessage(scanOptions.getPath() + ".end.header");

                    long totalCount = 0;
                    for (Map.Entry<String, Integer> entry : scanResult) {
                        totalCount = totalCount + entry.getValue();
                        String name = StringUtils.capitalizeName(entry.getKey().toLowerCase());
                        sendMessage( scanOptions.getPath() + ".end.format",
                                "%entry%", name,
                                "%count%", NumberFormat.getIntegerInstance().format(entry.getValue()));
                    }

                    sendMessage(scanOptions.getPath() + ".end.total",
                            "%chunks%", NumberFormat.getIntegerInstance().format(chunksDone),
                            "%blocks%", NumberFormat.getIntegerInstance().format(chunksDone * 16 * 16 * 256),
                            "%time%", TimeUtils.getDHMS(startTime),
                            "%world%", scanOptions.getWorld().getName());

                    sendMessage(scanOptions.getPath() + ".end.footer");
                } else {
                    sendMessage(scanOptions.getPath() + ".end.no_entries");
                }
            }
        }

        if (scanOptions.getListener() != null) {
            ScanCompleteEvent scanCompleteEvent = new ScanCompleteEvent(scanOptions, scanResult);
            scanOptions.getListener().onScanComplete(scanCompleteEvent);
        }

        System.gc();
    }

    public void forceStop() {
        Bukkit.getScheduler().cancelTask(this.taskID);

        if (scanOptions.hasUUID()) {
            scanManager.remove(scanOptions.getUUID());
            if (bossBarManager != null) {
                bossBarManager.removePersistentBossBar(scanOptions.getUUID());
            }
        }

        if (scanChunksTaskSyncHelper != null) {
            scanChunksTaskSyncHelper.stop();
        }
    }

    private void sendMessage(String path, String... placeholders) {
        if (scanOptions.isConsole()) {
            MessageUtils.sendMessage(Bukkit.getConsoleSender(), path, placeholders);
        } else if (scanOptions.hasUUID()) {
            MessageUtils.sendMessage(scanOptions.getUUID(), path, placeholders);
        }
    }

    public void setupNotification(Player player) {
        canSendProgressMessage = true;
        if (plugin.getConfiguration().GENERAL_NOTIFICATION_TYPE.toUpperCase().equals("BOSSBAR") && NMSManager.getInstance().isPost1_9()) {
            isBossBar = true;
        }

        progressMessage = plugin.getMessages().getString("messages.scan_notification");
        if (progressMessage == null || progressMessage.isEmpty()) {
            System.err.println("[Insights] Missing locale in messages.yml at path 'messages.scan_notification'!");
            if (player != null) {
                player.sendMessage("[Insights] Missing locale in messages.yml at path 'messages.scan_notification'!");
            }
            canSendProgressMessage = false;
        }
    }

    public int getChunksDone() {
        return chunksDone;
    }

    public void addChunk(CompletableFuture<Chunk> completableFuture) {
        chunkQueue.add(completableFuture);
    }

    public void addBlockStates(BlockState[] blockStates) {
        blockStatesList.add(blockStates);
    }

    @Override
    public void run() {
        // Notify player about progress in actionbar/bossbar
        tryNotifySpecial(false);

        // Notify player about progress every 10 seconds in chat
        // Regardless if we may continue scanning the next round of chunks.
        tryNotifyInChat();

        // Simple trick to only allow one loop over the dataset at a time.
        if (!this.run) {
            return;
        }
        this.run = false;

        while (!chunkQueue.isEmpty()) {
            // Try to get the chunk from chunk queue
            // Should never throw exception, as we can assume all CompletableFuture's are finished by now.
            Chunk chunk;
            try {
                chunk = chunkQueue.peek().get();
            } catch (InterruptedException | ExecutionException ex) {
                ex.printStackTrace();
                continue;
            }

            // Scan entities
            if (scanEntities) {
                for (Entity entity : chunk.getEntities()) {
                    if (shouldIncementEntity(entity)) {
                        scanResult.increment(entity.getType().name());
                    }
                }
            }

            // Scan tiles or proceed to scanning the chunk for blocks
            if (scanOptions.getScanType() == ScanType.TILE) {
                scanChunksTaskSyncHelper.addChunk(chunk);
            } else if (scanOptions.getScanType() == ScanType.CUSTOM || scanOptions.getScanType() == ScanType.ALL) {
                scanChunk(chunk, scanOptions, scanResult);
            }

            // Remove the chunk from queue (above we only peeked, we can only remove chunk if it's done)
            chunkQueue.poll();

            // Tiles are counted differently, this must be done in sync
            if (scanOptions.getScanType() != ScanType.TILE) {
                chunksDone++;
            }
        }

        // Count newly scanned tile chunks
        if (scanOptions.getScanType() == ScanType.TILE) {
            while (!blockStatesList.isEmpty()) {
                for (BlockState blockState : blockStatesList.poll()) {
                    scanResult.increment(blockState.getType().name());
                }
                chunksDone++;
            }
        }

        if (isFinished()) {
            tryNotifySpecial(true);
            stop();
        }

        this.run = true;
    }

    private boolean canNotifyInChat() {
        return System.currentTimeMillis() > (lastProgressMessageInChat + NOTIFICATION_DELAY_SECONDS * 1000);
    }

    private void tryNotifyInChat() {
        if (!canNotifyInChat()) return;
        lastProgressMessageInChat = System.currentTimeMillis();

        if (chunksDone > 0) {
            String chunksDoneScanningString = NumberFormat.getIntegerInstance().format(chunksDone);
            int chunksDoneLoading = scanOptions.getChunkCount() - scanOptions.getChunkLocations().size();
            String totalChunksString = NumberFormat.getIntegerInstance().format(scanOptions.getChunkCount());

            if (scanOptions.isDebug()) {
                if (chunksDoneLoading != scanOptions.getChunkCount()) {
                    String chunksDoneLoadingString = NumberFormat.getIntegerInstance().format(chunksDoneLoading);
                    LogManager.log(LogType.DEBUG, "Loaded " + chunksDoneLoadingString
                            + "/" + totalChunksString
                            + " and scanned " + chunksDoneScanningString + "/" + totalChunksString
                            + " " + (scanOptions.getChunkCount() == 1 ? "chunk" : "chunks")
                            + "...", loadChunksTask.getTaskID());
                } else {
                    LogManager.log(LogType.DEBUG, "Scanned " + chunksDoneScanningString
                            + "/" + totalChunksString
                            + " " + (scanOptions.getChunkCount() == 1 ? "chunk" : "chunks")
                            + "...", loadChunksTask.getTaskID());
                }
            }

            sendMessage(scanOptions.getPath() + ".progress",
                    "%count%", chunksDoneScanningString,
                    "%total%", totalChunksString,
                    "%world%", scanOptions.getWorld().getName());
        }
    }

    private boolean canNotifySpecial() {
        return System.currentTimeMillis() > (lastProgressMessageSpecial + NOTIFICATION_DELAY_SPECIAL_MILLIS);
    }

    private void tryNotifySpecial(boolean finished) {
        if (!canNotifySpecial()) return;
        lastProgressMessageSpecial = System.currentTimeMillis();

        if (canSendProgressMessage && scanOptions.hasUUID()) {
            Player player = Bukkit.getPlayer(scanOptions.getUUID());
            if (player != null) {
                sendSpecialNotification(player, finished);
            }
        }
    }

    private void sendSpecialNotification(Player player, boolean finished) {
        if (scanOptions.getChunkCount() == 0) return;
        String done = NumberFormat.getIntegerInstance().format(chunksDone);
        String total = NumberFormat.getIntegerInstance().format(scanOptions.getChunkCount());
        double progressDouble = finished ? 1 : ((double) chunksDone)/((double) scanOptions.getChunkCount());
        if (progressDouble < 0) {
            progressDouble = 0;
        } else if (progressDouble > 1) {
            progressDouble = 1;
        }
        String progress = String.format("%.2f", progressDouble*100) + "%";
        String message = MessageUtils.color(progressMessage.replace("%done%", done)
                .replace("%total%", total)
                .replace("%progress%", progress));
        if (isBossBar) {
            bossBarManager.displayPersistentBossBar(player, message, progressDouble);
        } else {
            MessageUtils.sendActionbar(player, message);
        }
    }

    public boolean isFinished() {
        return loadChunksTask.isCancelled() // Loading chunks must be done
                && chunkQueue.isEmpty() // There must be no more pending chunks
                && (scanChunksTaskSyncHelper == null || (chunksDone == scanOptions.getChunkCount())); // Tile scanning must be done
    }

    private void scanChunk(Chunk chunk, ScanOptions scanOptions, ScanResult result) {
        int maxHeight = scanOptions.getWorld().getMaxHeight();
        List<String> materials = scanOptions.getMaterials();
        ChunkSnapshot chunkSnapshot = chunk.getChunkSnapshot();
        for (int x = 0; x < 16; x++) {
            for (int y = 0; y < maxHeight; y++) {
                for (int z = 0; z < 16; z++) {
                    // Inter version compatible method to get the material of that location
                    Material material = ChunkUtils.getMaterial(chunkSnapshot, x,y,z);
                    if (material != null) {
                        if (materials.contains(material.name()) || scanOptions.getScanType() == ScanType.ALL) {
                            result.increment(material.name());
                        }
                    }
                }
            }
        }
    }

    private boolean shouldIncementEntity(Entity entity) {
        if (scanOptions.getScanType() == ScanType.ALL || scanOptions.getScanType() == ScanType.ENTITY) return true;
        List<String> entities = scanOptions.getEntityTypes();
        return (entities.contains(entity.getType().name()));
    }
}
