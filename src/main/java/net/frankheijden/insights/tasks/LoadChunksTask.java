package net.frankheijden.insights.tasks;

import io.papermc.lib.PaperLib;
import net.frankheijden.insights.Insights;
import net.frankheijden.insights.api.entities.ChunkLocation;
import net.frankheijden.insights.api.enums.ScanType;
import net.frankheijden.insights.api.interfaces.ScanCompleteEventListener;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.lang.reflect.Method;
import java.text.NumberFormat;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class LoadChunksTask implements Runnable {
    private Insights plugin;
    private ScanType scanType;
    private World world;
    private transient List<ChunkLocation> chunkLocations;
    private UUID uuid;
    private String path;
    private transient List<Material> materials;
    private transient List<EntityType> entityTypes;
    private boolean console;
    private ScanCompleteEventListener listener;

    private transient Map<CompletableFuture<Chunk>, ChunkLocation> pendingChunks;
    private transient boolean run = true;
    private int taskID;
    private boolean cancelled;
    private ScanChunksTask scanChunksTask;

    private long startTime;
    private int totalChunks;

    public LoadChunksTask(Insights plugin, ScanType scanType, World world, List<ChunkLocation> chunkLocations, UUID uuid, String path, List<Material> materials, List<EntityType> entityTypes, boolean console, ScanCompleteEventListener listener) {
        this.plugin = plugin;
        this.scanType = scanType;
        this.world = world;
        this.chunkLocations = chunkLocations;
        this.uuid = uuid;
        this.path = path;
        this.materials = (materials != null && materials.isEmpty()) ? null : materials;
        this.entityTypes = (entityTypes != null && entityTypes.isEmpty()) ? null : entityTypes;
        this.console = console;
        this.listener = listener;
    }

    public Insights getPlugin() {
        return plugin;
    }

    public ScanType getScanType() {
        return scanType;
    }

    public World getWorld() {
        return world;
    }

    public List<ChunkLocation> getChunkLocations() {
        return chunkLocations;
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getPath() {
        return path;
    }

    public List<Material> getMaterials() {
        return materials;
    }

    public List<EntityType> getEntityTypes() {
        return entityTypes;
    }

    public boolean isConsole() {
        return console;
    }

    public ScanCompleteEventListener getListener() {
        return listener;
    }

    public ScanChunksTask getScanChunksTask() {
        return scanChunksTask;
    }

    public int getTotalChunks() {
        return totalChunks;
    }

    public long getStartTime() {
        return startTime;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void start(long startTime) {
        this.startTime = startTime;
        this.totalChunks = chunkLocations.size();
        this.pendingChunks = new HashMap<>();
        this.taskID = Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, this, 0, 1);

        sendMessage( path + ".start", "%chunks%", NumberFormat.getIntegerInstance().format(totalChunks), "%world%", world.getName());

        scanChunksTask = new ScanChunksTask(this);
        scanChunksTask.start();

        if (uuid != null) {
            plugin.getPlayerScanTasks().put(uuid, this);

            Player player = Bukkit.getPlayer(uuid);
            if (player != null) {
                if (plugin.getConfiguration().GENERAL_SCAN_NOTIFICATION) {
                    scanChunksTask.setupNotification(player);
                }
            }
        }
    }

    public void sendMessage(String path, String... placeholders) {
        if (console) {
            plugin.getUtils().sendMessage(Bukkit.getConsoleSender(), path, placeholders);
        } else {
            plugin.getUtils().sendMessage(uuid, path, placeholders);
        }
    }

    private void setChunkForceLoaded(int x, int z, boolean b) {
        try {
            Class<?> worldClass = Class.forName("org.bukkit.World");
            Object worldObject = worldClass.cast(world);

            Method method = worldClass.getDeclaredMethod("setChunkForceLoaded", int.class, int.class, boolean.class);
            if (method != null) {
                method.invoke(worldObject, x, z, b);
            }
        } catch (NoSuchMethodException ignored) {

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public boolean removeFirst() {
        if (chunkLocations.size() > 0) {
            if (chunkLocations.size() != 1) {
                chunkLocations.remove(0);
            } else {
                chunkLocations = new ArrayList<>();
            }
        } else {
            stop();
            return false;
        }
        return true;
    }

    public void stop() {
        cancelled = true;
        run = false;
        Bukkit.getScheduler().cancelTask(taskID);
        world.save();
    }

    @Override
    public void run() {
        // Check if loop has finished, if not, return
        if (!run) {
            return;
        }
        run = false;

        long loopTime = System.currentTimeMillis();

        // Check if any CompletableFutures have been done processing.
        // If so, send the chunks to the ScanChunksTask for further processing.
        int chunksProcessedLastTick = 0;
        Map<CompletableFuture<Chunk>, ChunkLocation> newPendingChunks = new HashMap<>();
        Set<ChunkLocation> chunksToUnload = new HashSet<>();
        for (CompletableFuture<Chunk> completableFuture: pendingChunks.keySet()) {
            ChunkLocation chunkLocation = pendingChunks.get(completableFuture);
            if (completableFuture.isDone()) {
                ++chunksProcessedLastTick;

                scanChunksTask.addCompletableFuture(completableFuture);
                chunksToUnload.add(chunkLocation);
            } else {
                newPendingChunks.put(completableFuture, chunkLocation);
            }
        }
        pendingChunks = newPendingChunks;

        // Unload any chunks which have been used
        for (ChunkLocation unload : chunksToUnload) {
            setChunkForceLoaded(unload.getX(), unload.getZ(), false);
            world.unloadChunkRequest(unload.getX(), unload.getZ());
        }

        // Initalize how many chunks we may load this run
        int chunksToProcess = 1;
        if (chunksProcessedLastTick > 0 || pendingChunks.size() > 0) {
            int chunksExpectedToGetProcessed = (chunksProcessedLastTick - pendingChunks.size()) / 3 + 3;
            if (chunksExpectedToGetProcessed < chunksToProcess) {
                chunksToProcess = chunksExpectedToGetProcessed;
            }
        }

        // Load new chunks and addCompletableFuture them to the CompletableFuture list
        for (int i = 0; i < chunksToProcess; i++) {
            if (chunkLocations.size() <= 0) {
                stop();
                return;
            }
            ChunkLocation chunkLocation = chunkLocations.get(0);

            if (System.currentTimeMillis() > loopTime + 45) {
                run = true;
                return;
            }

            setChunkForceLoaded(chunkLocation.getX(), chunkLocation.getZ(), true);
            pendingChunks.put(PaperLib.getChunkAtAsync(world, chunkLocation.getX(), chunkLocation.getZ(), true), chunkLocation);

            if (!removeFirst()) {
                return;
            }
        }
        run = true;
    }
}
