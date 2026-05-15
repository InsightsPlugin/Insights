package dev.frankheijden.insights.tasks;

import dev.frankheijden.insights.api.InsightsPlugin;
import dev.frankheijden.insights.api.concurrent.ScanOptions;
import dev.frankheijden.insights.api.objects.chunk.ChunkLocation;
import dev.frankheijden.insights.api.tasks.InsightsAsyncTask;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

public class PlayerTrackerTask extends InsightsAsyncTask {

    private final Map<ChunkLocation, Long> scanLocations = new ConcurrentHashMap<>();
    private static final Set<Integer> knownErrorStackTraceHashes = ConcurrentHashMap.newKeySet();

    public PlayerTrackerTask(InsightsPlugin plugin) {
        super(plugin);
    }

    @Override
    public void run() {
        var worldStorage = plugin.getWorldStorage();
        Set<ChunkLocation> locations = new HashSet<>();
        for (Map.Entry<UUID, Player> entry : plugin.getPlayerList()) {
            var location = entry.getValue().getLocation();
            var world = location.getWorld();
            Set<Long> loadedChunks = worldStorage.getWorld(world.getUID()).getChunks();

            int chunkX = location.getBlockX() >> 4;
            int chunkZ = location.getBlockZ() >> 4;
            for (int x = -1; x <= 1; x++) {
                for (int z = -1; z <= 1; z++) {
                    var loc = new ChunkLocation(world, chunkX + x, chunkZ + z);
                    if (!loadedChunks.contains(loc.getKey()) && !this.scanLocations.containsKey(loc)) {
                        locations.add(loc);
                    }
                }
            }
        }

        if (locations.isEmpty()) {
            return;
        }

        long now = System.nanoTime();
        for (ChunkLocation loc : locations) {
            var world = loc.getWorld();
            int chunkX = loc.getX();
            int chunkZ = loc.getZ();
            if (world.isChunkLoaded(chunkX, chunkZ)) {
                this.scanLocations.put(loc, now);

                Bukkit.getRegionScheduler().run(plugin, world, chunkX, chunkZ, scheduledTask -> {
                    var chunk = world.getChunkAt(chunkX, chunkZ);
                    plugin.getChunkContainerExecutor().submit(chunk, ScanOptions.all()).whenComplete((s, e) -> {
                        if (s == null) {
                            int hash = e.getStackTrace()[0].hashCode();
                            if (!knownErrorStackTraceHashes.contains(hash)) {
                                knownErrorStackTraceHashes.add(hash);
                                plugin.getLogger().log(
                                        Level.SEVERE,
                                        "Error occurred while scanning "
                                                + loc
                                                + " (future errors with the same stacktrace are suppressed)",
                                        e
                                );
                            }
                        }
                        this.scanLocations.remove(loc);
                    });
                });
            }
        }
    }
}
