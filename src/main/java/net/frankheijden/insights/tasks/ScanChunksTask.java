package net.frankheijden.insights.tasks;

import io.papermc.lib.PaperLib;
import net.frankheijden.insights.Insights;
import net.frankheijden.insights.api.entities.ScanOptions;
import net.frankheijden.insights.api.entities.ScanResult;
import net.frankheijden.insights.api.enums.ScanType;
import net.frankheijden.insights.api.events.ScanCompleteEvent;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.ChunkSnapshot;
import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.text.NumberFormat;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;

public class ScanChunksTask implements Runnable {
    private Insights plugin;
    private ScanOptions scanOptions;
    private ScanResult scanResult;

    private final Queue<CompletableFuture<Chunk>> chunkQueue;

    private long startTime;
    private int taskID;
    private int chunksDone;
    private boolean run = true;

    private LoadChunksTask loadChunksTask;

    private ScanChunksTaskSyncHelper scanChunksTaskSyncHelper = null;
    private final Queue<BlockState[]> blockStatesList;

    private long lastProgressMessage;
    private boolean isBossBar;
    private String progressMessage;
    private boolean canSendProgressMessage = false;

    public ScanChunksTask(Insights plugin, ScanOptions scanOptions, LoadChunksTask loadChunksTask) {
        this.plugin = plugin;
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

        if (scanOptions.getScanType() == ScanType.TILE || scanOptions.getScanType() == ScanType.BOTH) {
            scanChunksTaskSyncHelper = new ScanChunksTaskSyncHelper(plugin,scanOptions, this);
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
                        String name = plugin.getUtils().capitalizeName(entry.getKey().toLowerCase());
                        sendMessage( scanOptions.getPath() + ".end.format",
                                "%entry%", name,
                                "%count%", NumberFormat.getIntegerInstance().format(entry.getValue()));
                    }

                    sendMessage(scanOptions.getPath() + ".end.total",
                            "%chunks%", NumberFormat.getIntegerInstance().format(chunksDone),
                            "%blocks%", NumberFormat.getIntegerInstance().format(chunksDone * 16 * 16 * 256),
                            "%time%", plugin.getUtils().getDHMS(startTime),
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

        if (plugin.getBossBarUtils() != null && plugin.getBossBarUtils().scanBossBarPlayers.get(scanOptions.getUUID()) != null && plugin.getConfiguration().GENERAL_NOTIFICATION_TYPE.toUpperCase().equals("BOSSBAR") && PaperLib.getMinecraftVersion() >= 9) {
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
            plugin.getUtils().sendMessage(Bukkit.getConsoleSender(), path, placeholders);
        } else if (scanOptions.hasUUID()) {
            plugin.getUtils().sendMessage(scanOptions.getUUID(), path, placeholders);
        }
    }

    public void setupNotification(Player player) {
        canSendProgressMessage = true;
        if (plugin.getConfiguration().GENERAL_NOTIFICATION_TYPE.toUpperCase().equals("BOSSBAR") && PaperLib.getMinecraftVersion() >= 9) {
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
        String done = NumberFormat.getIntegerInstance().format(chunksDone);
        String total = NumberFormat.getIntegerInstance().format(scanOptions.getChunkCount());
        double progressDouble = ((double) chunksDone)/((double) scanOptions.getChunkCount());
        if (progressDouble < 0) {
            progressDouble = 0;
        } else if (progressDouble > 1) {
            progressDouble = 1;
        }
        String progress = String.format("%.2f", progressDouble*100) + "%";
        String message = plugin.getUtils().color(progressMessage.replace("%done%", done).replace("%total%", total).replace("%progress%", progress));
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
            plugin.getUtils().sendActionbar(player, message);
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
                        plugin.sendDebug(loadChunksTask.getInternalTaskID(), "Loaded " + chunksDoneLoadingString + "/" + totalChunksString + " and scanned " + chunksDoneScanningString + "/" + totalChunksString + " " + (scanOptions.getChunkCount() == 1 ? "chunk" : "chunks") + "...");
                    } else {
                        plugin.sendDebug(loadChunksTask.getInternalTaskID(), "Scanned " + chunksDoneScanningString + "/" + totalChunksString + " " + (scanOptions.getChunkCount() == 1 ? "chunk" : "chunks") + "...");
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

            if ((scanOptions.getScanType() == ScanType.CUSTOM && scanOptions.getEntityTypes() != null) || scanOptions.getScanType() == ScanType.ALL || scanOptions.getScanType() == ScanType.ENTITY || scanOptions.getScanType() == ScanType.BOTH) {
                for (Entity entity : chunk.getEntities()) {
                    if ((scanOptions.getEntityTypes() != null && scanOptions.getEntityTypes().contains(entity.getType())) || scanOptions.getScanType() == ScanType.ALL || scanOptions.getScanType() == ScanType.ENTITY || scanOptions.getScanType() == ScanType.BOTH) {
                        scanResult.increment(entity);
                    }
                }
            }

            if (scanOptions.getScanType() == ScanType.TILE || scanOptions.getScanType() == ScanType.BOTH) {
                scanChunksTaskSyncHelper.addChunk(chunk);
            } else if ((scanOptions.getScanType() == ScanType.CUSTOM && scanOptions.getMaterials() != null) || scanOptions.getScanType() == ScanType.ALL) {
                ChunkSnapshot chunkSnapshot = chunk.getChunkSnapshot();
                for (int x = 0; x < 16; x++) {
                    for (int y = 0; y < scanOptions.getWorld().getMaxHeight(); y++) {
                        for (int z = 0; z < 16; z++) {
                            Material material = plugin.getUtils().getMaterial(chunkSnapshot, x,y,z);
                            if (material != null) {
                                if ((scanOptions.getMaterials() != null && scanOptions.getMaterials().contains(material)) || scanOptions.getScanType() == ScanType.ALL) {
                                    scanResult.increment(material);
                                }
                            }
                        }
                    }
                }
            }

            chunkQueue.poll();

            if (scanOptions.getScanType() != ScanType.TILE && scanOptions.getScanType() != ScanType.BOTH) {
                chunksDone++;
            }
        }

        if (scanOptions.getScanType() == ScanType.TILE || scanOptions.getScanType() == ScanType.BOTH) {
            while (!blockStatesList.isEmpty()) {
                for (BlockState blockState : blockStatesList.poll()) {
                    scanResult.increment(blockState.getType());
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
