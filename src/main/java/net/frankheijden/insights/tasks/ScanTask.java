package net.frankheijden.insights.tasks;

import io.papermc.lib.PaperLib;
import net.frankheijden.insights.Insights;
import net.frankheijden.insights.api.entities.ChunkLocation;
import net.frankheijden.insights.api.events.ScanCompleteEvent;
import net.frankheijden.insights.api.interfaces.ScanCompleteEventListener;
import org.bukkit.*;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.lang.reflect.Method;
import java.text.NumberFormat;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class ScanTask implements Runnable {
    private Insights plugin;
    private World world;
    private CommandSender sender;
    private String path;
    private transient List<ChunkLocation> chunkLocations;
    private transient List<Material> materials;
    private transient List<EntityType> entityTypes;
    private ScanCompleteEventListener listener;

    private transient Map<CompletableFuture<Chunk>, ChunkLocation> pendingChunks;
    private transient boolean run = true;
    private int taskID;
    private boolean isCancelled = false;
    private ScanRunnable scanRunnable;
    private ScanNotificationTask scanNotificationTask;
    private long lastProgressMessage;

    private long startTime;
    private int totalChunks;

    public ScanTask(Insights plugin, World world, List<ChunkLocation> chunkLocations, List<Material> materials, List<EntityType> entityTypes, ScanCompleteEventListener listener) {
        this(plugin, world, null, null, chunkLocations, materials, entityTypes, listener);
    }

    public ScanTask(Insights plugin, World world, CommandSender sender, String path, List<ChunkLocation> chunkLocations, List<Material> materials, List<EntityType> entityTypes, ScanCompleteEventListener listener) {
        this.plugin = plugin;
        this.world = world;
        this.sender = sender;
        this.path = path;
        this.chunkLocations = chunkLocations;
        this.materials = (materials != null && materials.isEmpty()) ? null : materials;
        this.entityTypes = (entityTypes != null && entityTypes.isEmpty()) ? null : entityTypes;
        this.listener = listener;
    }

    public void start(long startTime) {
        this.startTime = startTime;

        this.totalChunks = chunkLocations.size();
        plugin.utils.sendMessage(sender, path + ".start", "%chunks%", NumberFormat.getIntegerInstance().format(totalChunks), "%world%", world.getName());
        pendingChunks = new HashMap<>();
        taskID = Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, this, 0, 1);

        scanRunnable = new ScanRunnable();
        scanRunnable.start();

        if (sender != null && sender instanceof Player) {
            Player player = (Player) sender;
            plugin.playerScanTasks.put(player.getUniqueId(), this);

            if (plugin.config.GENERAL_SCAN_NOTIFICATION) {
                scanNotificationTask = new ScanNotificationTask(player);
                scanNotificationTask.start();
            }
        }
    }

    public boolean isScanningForAll() {
        return materials == null && entityTypes == null;
    }

    public int getTotalChunks() {
        return totalChunks;
    }

    public ScanRunnable getScanRunnable() {
        return scanRunnable;
    }

    public long getStartTime() {
        return startTime;
    }

    @Override
    public void run() {
        if (!run) {
            return;
        }
        run = false;

        long loopTime = System.currentTimeMillis();

        int chunksProcessedLastTick = 0;
        Map<CompletableFuture<Chunk>, ChunkLocation> newPendingChunks = new HashMap<>();
        Set<ChunkLocation> chunksToUnload = new HashSet<>();
        for (CompletableFuture<Chunk> completableFuture: pendingChunks.keySet()) {
            ChunkLocation chunkLocation = pendingChunks.get(completableFuture);
            if (completableFuture.isDone()) {
                ++chunksProcessedLastTick;

                scanRunnable.add(completableFuture);
                chunksToUnload.add(chunkLocation);
            } else {
                newPendingChunks.put(completableFuture, chunkLocation);
            }
        }
        pendingChunks = newPendingChunks;

        for (ChunkLocation unload : chunksToUnload) {
            setChunkForceLoaded(unload.getX(), unload.getZ(), false);
            world.unloadChunkRequest(unload.getX(), unload.getZ());
        }

        int chunksToProcess = 1;
        if (chunksProcessedLastTick > 0 || pendingChunks.size() > 0) {
            int chunksExpectedToGetProcessed = (chunksProcessedLastTick - pendingChunks.size()) / 3 + 3;
            if (chunksExpectedToGetProcessed < chunksToProcess) {
                chunksToProcess = chunksExpectedToGetProcessed;
            }
        }

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
            chunkLocations.remove(0);
        } else {
            stop();
            return false;
        }
        return true;
    }

    public void stop() {
        isCancelled = true;
        run = false;
        Bukkit.getScheduler().cancelTask(taskID);
        world.save();
    }

    public class ScanRunnable implements Runnable {
        private transient TreeMap<String, Integer> counts;
        private transient List<CompletableFuture<Chunk>> completableFutures;
        private transient List<CompletableFuture<Chunk>> completableFuturesToAdd;

        private int taskID;
        private int chunksDone;
        private boolean run;

        public ScanRunnable() {
            counts = new TreeMap<>();
            completableFutures = new ArrayList<>();
            completableFuturesToAdd = new ArrayList<>();
            chunksDone = 0;
        }

        public int getChunksDone() {
            return chunksDone;
        }

        public void add(CompletableFuture<Chunk> completableFuture) {
            completableFuturesToAdd.add(completableFuture);
        }

        public void start() {
            this.run = true;
            this.taskID = Bukkit.getServer().getScheduler().scheduleAsyncRepeatingTask(plugin, this, 0, 1);
        }

        private void stop() {
            Bukkit.getScheduler().cancelTask(taskID);

            long totalCount = 0;
            if (counts.size() > 0) {
                plugin.utils.sendMessage(sender, path + ".end.header");
                for (Map.Entry<String, Integer> entry : counts.entrySet()) {
                    totalCount = totalCount + entry.getValue();
                    String name = plugin.utils.capitalizeName(entry.getKey().toLowerCase());
                    plugin.utils.sendMessage(sender, path + ".end.format", "%entry%", name, "%count%", NumberFormat.getIntegerInstance().format(entry.getValue()));
                }
                plugin.utils.sendMessage(sender, path + ".end.total", "%chunks%", NumberFormat.getIntegerInstance().format(totalChunks), "%blocks%", NumberFormat.getIntegerInstance().format(totalChunks * 16 * 16 * 256), "%time%", plugin.utils.getDHMS(startTime), "%world%", world.getName());
                plugin.utils.sendMessage(sender, path + ".end.footer");
            } else {
                plugin.utils.sendMessage(sender, path + ".end.no_entries");
            }

            if (sender != null && sender instanceof Player) {
                Player player = (Player) sender;
                plugin.playerScanTasks.remove(player.getUniqueId());

                if (scanNotificationTask != null) {
                    scanNotificationTask.stop();
                }
            }

            if (listener != null) {
                ScanCompleteEvent scanCompleteEvent = new ScanCompleteEvent(counts, world, chunkLocations, materials, entityTypes);
                listener.onScanComplete(scanCompleteEvent);
            }

            System.gc();
        }

        @Override
        public void run() {
            long now = System.currentTimeMillis();
            if (now > lastProgressMessage + 10000) {
                lastProgressMessage = System.currentTimeMillis();
                if (chunksDone > 0) {
                    plugin.utils.sendMessage(sender, path + ".progress", "%count%", NumberFormat.getIntegerInstance().format(chunksDone), "%total%", NumberFormat.getIntegerInstance().format(totalChunks), "%world%", world.getName());
                }
            }

            if (!this.run) {
                return;
            }
            this.run = false;

            List<CompletableFuture<Chunk>> removeableCompletableFutures = new ArrayList<>();
            for (CompletableFuture<Chunk> completableFuture : completableFutures) {
                Chunk chunk;
                try {
                    chunk = completableFuture.get();
                } catch (InterruptedException | ExecutionException ex) {
                    ex.printStackTrace();
                    return;
                }

                if (entityTypes != null || isScanningForAll()) {
                    for (Entity entity : chunk.getEntities()) {
                        if ((entityTypes != null && entityTypes.contains(entity.getType())) || isScanningForAll()) {
                            counts.merge(entity.getType().name(), 1, Integer::sum);
                        }
                    }
                }

                if (materials != null || isScanningForAll()) {
                    ChunkSnapshot chunkSnapshot = chunk.getChunkSnapshot();
                    for (int x = 0; x < 16; x++) {
                        for (int y = 0; y < world.getMaxHeight(); y++) {
                            for (int z = 0; z < 16; z++) {
                                Material material = plugin.utils.getMaterial(chunkSnapshot, x,y,z);
                                if (material != null) {
                                    if ((materials != null && materials.contains(material)) || isScanningForAll()) {
                                        counts.merge(material.name(), 1, Integer::sum);
                                    }
                                }
                            }
                        }
                    }
                }

                removeableCompletableFutures.add(completableFuture);
                chunksDone++;
            }

            completableFutures.removeAll(removeableCompletableFutures);
            completableFutures.addAll(completableFuturesToAdd);
            completableFuturesToAdd.clear();

            if (isCancelled && completableFutures.isEmpty()) {
                this.stop();
            }

            this.run = true;
        }
    }

    public class ScanNotificationTask implements Runnable {
        private Player player;
        private String message;

        private boolean isBossBar = false;
        private int taskID;

        public ScanNotificationTask(Player player) {
            this.player = player;
        }

        public void start() {
            if (plugin.config.GENERAL_NOTIFICATION_TYPE.toUpperCase().equals("BOSSBAR") && PaperLib.getMinecraftVersion() >= 9) {
                isBossBar = true;

                plugin.bossBarUtils.scanBossBarPlayers.put(player, plugin.bossBarUtils.defaultBossBar);
                plugin.bossBarUtils.scanBossBarPlayers.get(player).addPlayer(player);
                plugin.bossBarUtils.scanBossBarPlayers.get(player).setVisible(true);
            }

            message = plugin.messages.getString("messages.scan_notification");
            if (message != null && !message.isEmpty()) {
                this.taskID = Bukkit.getServer().getScheduler().scheduleAsyncRepeatingTask(plugin, this, 0, 10);
            } else {
                System.err.println("[Insights] Missing locale in messages.yml at path 'messages.scan_notification'!");
                player.sendMessage("[Insights] Missing locale in messages.yml at path 'messages.scan_notification'!");
            }
        }

        public void stop() {
            Bukkit.getServer().getScheduler().cancelTask(this.taskID);
            if (isBossBar) {
                plugin.bossBarUtils.scanBossBarPlayers.get(player).setVisible(false);
                plugin.bossBarUtils.scanBossBarPlayers.get(player).removePlayer(player);
                plugin.bossBarUtils.scanBossBarPlayers.remove(player);
            }
        }

        @Override
        public void run() {
            String done = NumberFormat.getIntegerInstance().format(scanRunnable.chunksDone);
            String total = NumberFormat.getIntegerInstance().format(totalChunks);
            double progressDouble = ((double) scanRunnable.chunksDone)/((double) totalChunks);
            if (progressDouble < 0) {
                progressDouble = 0;
            } else if (progressDouble > 1) {
                progressDouble = 1;
            }
            String progress = (int) (progressDouble*100) + "%";
            String message = plugin.utils.color(this.message.replace("%done%", done).replace("%total%", total).replace("%progress%", progress));
            if (isBossBar) {
                updateBossBar(message, progressDouble);
            } else {
                updateActionBar(message);
            }
        }

        private void updateBossBar(String message, double progress) {
            plugin.bossBarUtils.scanBossBarPlayers.get(player).setProgress(progress);
            plugin.bossBarUtils.scanBossBarPlayers.get(player).setTitle(message);
        }

        private void updateActionBar(String message) {
            plugin.utils.sendActionbar(player, message);
        }
    }
}
