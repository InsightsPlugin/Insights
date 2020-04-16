package net.frankheijden.insights.tasks;

import net.frankheijden.insights.Insights;
import net.frankheijden.insights.entities.ScanOptions;
import net.frankheijden.insights.entities.ScanResult;
import net.frankheijden.insights.enums.ScanType;
import net.frankheijden.insights.events.ScanCompleteEvent;
import net.frankheijden.insights.utils.*;
import org.bukkit.*;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.text.NumberFormat;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.*;

public class ScanChunksTask implements Runnable {

    private static final Insights plugin = Insights.getInstance();
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

    private long lastProgressMessage;
    private boolean isBossBar;
    private String progressMessage;
    private boolean canSendProgressMessage = false;

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
    }

    public void start(long startTime) {
        this.startTime = startTime;
        this.taskID = Bukkit.getServer().getScheduler().scheduleAsyncRepeatingTask(plugin, this, 0, 1); // TODO: find non-deprecated method

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

        if (scanOptions.getUUID() != null) {
            plugin.getPlayerScanTasks().remove(scanOptions.getUUID());
        }

        if (plugin.getBossBarUtils() != null && plugin.getBossBarUtils().scanBossBarPlayers.get(scanOptions.getUUID()) != null && plugin.getConfiguration().GENERAL_NOTIFICATION_TYPE.toUpperCase().equals("BOSSBAR") && plugin.isPost1_9()) {
            plugin.getBossBarUtils().scanBossBarPlayers.get(scanOptions.getUUID()).setVisible(false);
            plugin.getBossBarUtils().scanBossBarPlayers.get(scanOptions.getUUID()).removeAll();
            plugin.getBossBarUtils().scanBossBarPlayers.remove(scanOptions.getUUID());
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
        if (plugin.getConfiguration().GENERAL_NOTIFICATION_TYPE.toUpperCase().equals("BOSSBAR") && plugin.isPost1_9()) {
            isBossBar = true;

            plugin.getBossBarUtils().scanBossBarPlayers.put(scanOptions.getUUID(), plugin.getBossBarUtils().createNewBossBar());
            if (player != null) {
                plugin.getBossBarUtils().scanBossBarPlayers.get(scanOptions.getUUID()).addPlayer(player);
            }
            plugin.getBossBarUtils().scanBossBarPlayers.get(scanOptions.getUUID()).setVisible(true);
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

    private void sendNotification() {
        if (scanOptions.getChunkCount() == 0) return;
        String done = NumberFormat.getIntegerInstance().format(chunksDone);
        String total = NumberFormat.getIntegerInstance().format(scanOptions.getChunkCount());
        double progressDouble = ((double) chunksDone)/((double) scanOptions.getChunkCount());
        if (progressDouble < 0) {
            progressDouble = 0;
        } else if (progressDouble > 1) {
            progressDouble = 1;
        }
        String progress = String.format("%.2f", progressDouble*100) + "%";
        String message = MessageUtils.color(progressMessage.replace("%done%", done).replace("%total%", total).replace("%progress%", progress));
        if (isBossBar) {
            updateBossBar(message, progressDouble);
        } else {
            updateActionBar(message);
        }
    }

    private void updateBossBar(String message, double progress) {
        plugin.getBossBarUtils().scanBossBarPlayers.get(scanOptions.getUUID()).setProgress(progress);
        plugin.getBossBarUtils().scanBossBarPlayers.get(scanOptions.getUUID()).setTitle(message);
    }

    private void updateActionBar(String message) {
        Player player = Bukkit.getPlayer(scanOptions.getUUID());
        if (player != null) {
            MessageUtils.sendActionbar(player, message);
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
        if (canSendProgressMessage) {
            sendNotification();
        }

        // Notify player about progress every 10 seconds in chat
        long now = System.currentTimeMillis();
        if (now > lastProgressMessage + 10000) {
            lastProgressMessage = System.currentTimeMillis();
            if (chunksDone > 0) {
                String chunksDoneScanningString = NumberFormat.getIntegerInstance().format(chunksDone);
                int chunksDoneLoading = scanOptions.getChunkCount() - scanOptions.getChunkLocations().size();
                String totalChunksString = NumberFormat.getIntegerInstance().format(scanOptions.getChunkCount());

                if (scanOptions.isDebug()) {
                    if (chunksDoneLoading != scanOptions.getChunkCount()) {
                        String chunksDoneLoadingString = NumberFormat.getIntegerInstance().format(chunksDoneLoading);
                        plugin.log(Insights.LogType.DEBUG, "Loaded " + chunksDoneLoadingString + "/" + totalChunksString + " and scanned " + chunksDoneScanningString + "/" + totalChunksString + " " + (scanOptions.getChunkCount() == 1 ? "chunk" : "chunks") + "...", loadChunksTask.getInternalTaskID());
                    } else {
                        plugin.log(Insights.LogType.DEBUG, "Scanned " + chunksDoneScanningString + "/" + totalChunksString + " " + (scanOptions.getChunkCount() == 1 ? "chunk" : "chunks") + "...", loadChunksTask.getInternalTaskID());
                    }
                }

                sendMessage(scanOptions.getPath() + ".progress",
                        "%count%", chunksDoneScanningString,
                        "%total%", totalChunksString,
                        "%world%", scanOptions.getWorld().getName());
            }
        }

        if (!this.run) {
            return;
        }
        this.run = false;

        while (!chunkQueue.isEmpty()) {
            Chunk chunk;
            try {
                chunk = chunkQueue.peek().get();
            } catch (InterruptedException | ExecutionException ex) {
                ex.printStackTrace();
                continue;
            }

            if ((scanOptions.getScanType() == ScanType.CUSTOM && scanOptions.getEntityTypes() != null) || scanOptions.getScanType() == ScanType.ALL || scanOptions.getScanType() == ScanType.ENTITY) {
                for (Entity entity : chunk.getEntities()) {
                    if ((scanOptions.getEntityTypes() != null && scanOptions.getEntityTypes().contains(entity.getType().name())) || scanOptions.getScanType() == ScanType.ALL || scanOptions.getScanType() == ScanType.ENTITY) {
                        scanResult.increment(entity.getType().name());
                    }
                }
            }

            if (scanOptions.getScanType() == ScanType.TILE) {
                scanChunksTaskSyncHelper.addChunk(chunk);
            } else if ((scanOptions.getScanType() == ScanType.CUSTOM && scanOptions.getMaterials() != null) || scanOptions.getScanType() == ScanType.ALL) {
                ChunkSnapshot chunkSnapshot = chunk.getChunkSnapshot();
                for (int x = 0; x < 16; x++) {
                    for (int y = 0; y < scanOptions.getWorld().getMaxHeight(); y++) {
                        for (int z = 0; z < 16; z++) {
                            Material material = ChunkUtils.getMaterial(chunkSnapshot, x,y,z);
                            if (material != null) {
                                if ((scanOptions.getMaterials() != null && scanOptions.getMaterials().contains(material.name())) || scanOptions.getScanType() == ScanType.ALL) {
                                    scanResult.increment(material.name());
                                }
                            }
                        }
                    }
                }
            }

            chunkQueue.poll();

            if (scanOptions.getScanType() != ScanType.TILE) {
                chunksDone++;
            }
        }

        if (scanOptions.getScanType() == ScanType.TILE) {
            while (!blockStatesList.isEmpty()) {
                for (BlockState blockState : blockStatesList.poll()) {
                    scanResult.increment(blockState.getType().name());
                }
                chunksDone++;
            }
        }

        if (loadChunksTask.isCancelled() && chunkQueue.isEmpty() && (scanChunksTaskSyncHelper == null || (chunksDone == scanOptions.getChunkCount()))) {
            this.stop();
        }

        this.run = true;
    }
}
