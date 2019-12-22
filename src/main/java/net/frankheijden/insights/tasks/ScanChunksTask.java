package net.frankheijden.insights.tasks;

import io.papermc.lib.PaperLib;
import net.frankheijden.insights.api.enums.ScanType;
import net.frankheijden.insights.api.events.ScanCompleteEvent;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.ChunkSnapshot;
import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.text.NumberFormat;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class ScanChunksTask implements Runnable {
    private LoadChunksTask loadChunksTask;
    private TreeMap<String, Integer> counts;
    private final Vector<CompletableFuture<Chunk>> completableFutures;

    private int taskID;
    private int chunksDone;
    private boolean run;

    private ScanChunksTaskSyncHelper scanChunksTaskSyncHelper = null;
    private final Vector<BlockState[]> blockStatesList;

    private long lastProgressMessage;
    private boolean isBossBar;
    private String progressMessage;
    private boolean canSendProgressMessage = false;

    public ScanChunksTask(LoadChunksTask loadChunksTask) {
        this.loadChunksTask = loadChunksTask;
        this.counts = new TreeMap<>();
        this.completableFutures = new Vector<>();
        this.blockStatesList = new Vector<>();
        this.chunksDone = 0;
    }

    public void start() {
        this.run = true;
        this.taskID = Bukkit.getServer().getScheduler().scheduleAsyncRepeatingTask(loadChunksTask.getPlugin(), this, 0, 1);

        if (loadChunksTask.getScanType() == ScanType.TILE || loadChunksTask.getScanType() == ScanType.BOTH) {
            scanChunksTaskSyncHelper = new ScanChunksTaskSyncHelper(this);
            scanChunksTaskSyncHelper.start();
        }
    }

    private void stop() {
        forceStop();

        String chunksDoneString = NumberFormat.getIntegerInstance().format(chunksDone);
        String totalChunksString = NumberFormat.getIntegerInstance().format(loadChunksTask.getTotalChunks());

        if (loadChunksTask.getUuid() != null) {
            Player player = Bukkit.getPlayer(loadChunksTask.getUuid());
            if (player != null || loadChunksTask.isConsole()) {
                long totalCount = 0;
                if (counts.size() > 0) {
                    loadChunksTask.sendMessage(loadChunksTask.getPath() + ".end.header");
                    for (Map.Entry<String, Integer> entry : counts.entrySet()) {
                        totalCount = totalCount + entry.getValue();
                        String name = loadChunksTask.getPlugin().getUtils().capitalizeName(entry.getKey().toLowerCase());
                        loadChunksTask.sendMessage( loadChunksTask.getPath() + ".end.format", "%entry%", name, "%count%", NumberFormat.getIntegerInstance().format(entry.getValue()));
                    }
                    loadChunksTask.sendMessage(loadChunksTask.getPath() + ".end.total", "%chunks%", chunksDoneString, "%blocks%", NumberFormat.getIntegerInstance().format(loadChunksTask.getTotalChunks() * 16 * 16 * 256), "%time%", loadChunksTask.getPlugin().getUtils().getDHMS(loadChunksTask.getStartTime()), "%world%", loadChunksTask.getWorld().getName());
                    loadChunksTask.sendMessage(loadChunksTask.getPath() + ".end.footer");
                } else {
                    loadChunksTask.sendMessage(loadChunksTask.getPath() + ".end.no_entries");
                }
            }
        }

        if (loadChunksTask.getScanType() == ScanType.CUSTOM) {
            if (loadChunksTask.getMaterials() != null) {
                for (Material material : loadChunksTask.getMaterials()) {
                    if (!counts.containsKey(material.name())) {
                        counts.put(material.name(), 0);
                    }
                }
            }
            if (loadChunksTask.getEntityTypes() != null) {
                for (EntityType entityType : loadChunksTask.getEntityTypes()) {
                    if (!counts.containsKey(entityType.name())) {
                        counts.put(entityType.name(), 0);
                    }
                }
            }
        }

        if (loadChunksTask.getListener() != null) {
            ScanCompleteEvent scanCompleteEvent = new ScanCompleteEvent(loadChunksTask);
            loadChunksTask.getListener().onScanComplete(scanCompleteEvent);
        }

        if (loadChunksTask.shouldPrintDebug()) {
            loadChunksTask.getPlugin().sendDebug(loadChunksTask.getInternalTaskID(), "Finished scanning " + chunksDoneString + "/" + totalChunksString + " " + (loadChunksTask.getTotalChunks() == 1 ? "chunk" : "chunks") + ".");
        }

        System.gc();
    }

    public void forceStop() {
        Bukkit.getScheduler().cancelTask(this.taskID);

        if (loadChunksTask.getUuid() != null) {
            loadChunksTask.getPlugin().getPlayerScanTasks().remove(loadChunksTask.getUuid());
        }

        if (loadChunksTask.getPlugin().getBossBarUtils() != null && loadChunksTask.getPlugin().getBossBarUtils().scanBossBarPlayers.get(loadChunksTask.getUuid()) != null && loadChunksTask.getPlugin().getConfiguration().GENERAL_NOTIFICATION_TYPE.toUpperCase().equals("BOSSBAR") && PaperLib.getMinecraftVersion() >= 9) {
            loadChunksTask.getPlugin().getBossBarUtils().scanBossBarPlayers.get(loadChunksTask.getUuid()).setVisible(false);
            loadChunksTask.getPlugin().getBossBarUtils().scanBossBarPlayers.get(loadChunksTask.getUuid()).removeAll();
            loadChunksTask.getPlugin().getBossBarUtils().scanBossBarPlayers.remove(loadChunksTask.getUuid());
        }

        if (scanChunksTaskSyncHelper != null) {
            scanChunksTaskSyncHelper.stop();
        }
    }

    public void setupNotification(Player player) {
        canSendProgressMessage = true;
        if (loadChunksTask.getPlugin().getConfiguration().GENERAL_NOTIFICATION_TYPE.toUpperCase().equals("BOSSBAR") && PaperLib.getMinecraftVersion() >= 9) {
            isBossBar = true;

            loadChunksTask.getPlugin().getBossBarUtils().scanBossBarPlayers.put(loadChunksTask.getUuid(), loadChunksTask.getPlugin().getBossBarUtils().createNewBossBar());
            if (player != null) {
                loadChunksTask.getPlugin().getBossBarUtils().scanBossBarPlayers.get(loadChunksTask.getUuid()).addPlayer(player);
            }
            loadChunksTask.getPlugin().getBossBarUtils().scanBossBarPlayers.get(loadChunksTask.getUuid()).setVisible(true);
        }

        progressMessage = loadChunksTask.getPlugin().getMessages().getString("messages.scan_notification");
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
        String total = NumberFormat.getIntegerInstance().format(loadChunksTask.getTotalChunks());
        double progressDouble = ((double) chunksDone)/((double) loadChunksTask.getTotalChunks());
        if (progressDouble < 0) {
            progressDouble = 0;
        } else if (progressDouble > 1) {
            progressDouble = 1;
        }
        String progress = String.format("%.2f", progressDouble*100) + "%";
        String message = loadChunksTask.getPlugin().getUtils().color(progressMessage.replace("%done%", done).replace("%total%", total).replace("%progress%", progress));
        if (isBossBar) {
            updateBossBar(message, progressDouble);
        } else {
            updateActionBar(message);
        }
    }

    private void updateBossBar(String message, double progress) {
        loadChunksTask.getPlugin().getBossBarUtils().scanBossBarPlayers.get(loadChunksTask.getUuid()).setProgress(progress);
        loadChunksTask.getPlugin().getBossBarUtils().scanBossBarPlayers.get(loadChunksTask.getUuid()).setTitle(message);
    }

    private void updateActionBar(String message) {
        Player player = Bukkit.getPlayer(loadChunksTask.getUuid());
        if (player != null) {
            loadChunksTask.getPlugin().getUtils().sendActionbar(player, message);
        }
    }

    public LoadChunksTask getLoadChunksTask() {
        return loadChunksTask;
    }

    public TreeMap<String, Integer> getCounts() {
        return counts;
    }

    public int getChunksDone() {
        return chunksDone;
    }

    public void addCompletableFuture(CompletableFuture<Chunk> completableFuture) {
        completableFutures.add(completableFuture);
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
                int chunksDoneLoading = loadChunksTask.getTotalChunks() - loadChunksTask.getChunkLocations().size();
                String totalChunksString = NumberFormat.getIntegerInstance().format(loadChunksTask.getTotalChunks());

                if (loadChunksTask.shouldPrintDebug()) {
                    if (chunksDoneLoading != loadChunksTask.getTotalChunks()) {
                        String chunksDoneLoadingString = NumberFormat.getIntegerInstance().format(chunksDoneLoading);
                        loadChunksTask.getPlugin().sendDebug(loadChunksTask.getInternalTaskID(), "Loaded " + chunksDoneLoadingString + "/" + totalChunksString + " and scanned " + chunksDoneScanningString + "/" + totalChunksString + " " + (loadChunksTask.getTotalChunks() == 1 ? "chunk" : "chunks") + "...");
                    } else {
                        loadChunksTask.getPlugin().sendDebug(loadChunksTask.getInternalTaskID(), "Scanned " + chunksDoneScanningString + "/" + totalChunksString + " " + (loadChunksTask.getTotalChunks() == 1 ? "chunk" : "chunks") + "...");
                    }
                }

                loadChunksTask.sendMessage(loadChunksTask.getPath() + ".progress", "%count%", chunksDoneScanningString, "%total%", totalChunksString, "%world%", loadChunksTask.getWorld().getName());
            }
        }

        if (!this.run) {
            return;
        }
        this.run = false;

        synchronized (completableFutures) {
            List<CompletableFuture<Chunk>> removeableCompletableFutures = new ArrayList<>();
            for (CompletableFuture<Chunk> completableFuture : completableFutures) {
                Chunk chunk;
                try {
                    chunk = completableFuture.get();
                } catch (InterruptedException | ExecutionException ex) {
                    ex.printStackTrace();
                    continue;
                }

                if ((loadChunksTask.getScanType() == ScanType.CUSTOM && loadChunksTask.getEntityTypes() != null) || loadChunksTask.getScanType() == ScanType.ALL || loadChunksTask.getScanType() == ScanType.ENTITY || loadChunksTask.getScanType() == ScanType.BOTH) {
                    for (Entity entity : chunk.getEntities()) {
                        if ((loadChunksTask.getEntityTypes() != null && loadChunksTask.getEntityTypes().contains(entity.getType())) || loadChunksTask.getScanType() == ScanType.ALL || loadChunksTask.getScanType() == ScanType.ENTITY || loadChunksTask.getScanType() == ScanType.BOTH) {
                            counts.merge(entity.getType().name(), 1, Integer::sum);
                        }
                    }
                }

                if (loadChunksTask.getScanType() == ScanType.TILE || loadChunksTask.getScanType() == ScanType.BOTH) {
                    scanChunksTaskSyncHelper.addChunk(chunk);
                } else if ((loadChunksTask.getScanType() == ScanType.CUSTOM && loadChunksTask.getMaterials() != null) || loadChunksTask.getScanType() == ScanType.ALL) {
                    ChunkSnapshot chunkSnapshot = chunk.getChunkSnapshot();
                    for (int x = 0; x < 16; x++) {
                        for (int y = 0; y < loadChunksTask.getWorld().getMaxHeight(); y++) {
                            for (int z = 0; z < 16; z++) {
                                Material material = loadChunksTask.getPlugin().getUtils().getMaterial(chunkSnapshot, x,y,z);
                                if (material != null) {
                                    if ((loadChunksTask.getMaterials() != null && loadChunksTask.getMaterials().contains(material)) || loadChunksTask.getScanType() == ScanType.ALL) {
                                        counts.merge(material.name(), 1, Integer::sum);
                                    }
                                }
                            }
                        }
                    }
                }

                removeableCompletableFutures.add(completableFuture);
                if (loadChunksTask.getScanType() != ScanType.TILE && loadChunksTask.getScanType() != ScanType.BOTH) {
                    chunksDone++;
                }
            }
            completableFutures.removeAll(removeableCompletableFutures);
        }

        if (loadChunksTask.getScanType() == ScanType.TILE || loadChunksTask.getScanType() == ScanType.BOTH) {
            synchronized (blockStatesList) {
                List<BlockState[]> blockStatesListToRemove = new ArrayList<>();
                for (BlockState[] blockStates : blockStatesList) {
                    for (BlockState blockState : blockStates) {
                        counts.merge(blockState.getType().name(), 1, Integer::sum);
                    }
                    blockStatesListToRemove.add(blockStates);
                    chunksDone++;
                }
                blockStatesList.removeAll(blockStatesListToRemove);
            }
        }

        if (loadChunksTask.isCancelled() && completableFutures.isEmpty() && (scanChunksTaskSyncHelper == null || (chunksDone == loadChunksTask.getTotalChunks()))) {
            this.stop();
        }

        this.run = true;
    }
}
