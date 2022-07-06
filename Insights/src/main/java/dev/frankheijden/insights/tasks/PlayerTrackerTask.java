package dev.frankheijden.insights.tasks;

import dev.frankheijden.insights.api.InsightsPlugin;
import dev.frankheijden.insights.api.concurrent.ScanOptions;
import dev.frankheijden.insights.api.objects.chunk.ChunkLocation;
import dev.frankheijden.insights.api.tasks.InsightsAsyncTask;
import org.bukkit.entity.Player;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerTrackerTask extends InsightsAsyncTask {

    private final Map<ChunkLocation, Long> scanLocations = new ConcurrentHashMap<>();

    public PlayerTrackerTask(InsightsPlugin plugin) {
        super(plugin);
    }

    @Override
    public void run() {
        var regionStorage = plugin.regionManager().regionStorage();
        Set<ChunkLocation> locations = new HashSet<>();
        for (Map.Entry<UUID, Player> entry : plugin.playerList()) {
            var location = entry.getValue().getLocation();
            var world = location.getWorld();
            Set<Long> loadedChunks = regionStorage.worldChunkSet(world.getUID());

            int chunkX = location.getBlockX() >> 4;
            int chunkZ = location.getBlockZ() >> 4;
            for (int x = -1; x <= 1; x++) {
                for (int z = -1; z <= 1; z++) {
                    var loc = new ChunkLocation(world, chunkX + x, chunkZ + z);
                    if (!loadedChunks.contains(loc.key()) && !this.scanLocations.containsKey(loc)) {
                        locations.add(loc);
                    }
                }
            }
        }

        if (locations.isEmpty()) {
            return;
        }

        plugin.getServer().getScheduler().runTask(plugin, () -> {
            long now = System.nanoTime();
            for (ChunkLocation loc : locations) {
                var world = loc.world();
                if (world.isChunkLoaded(loc.x(), loc.z())) {
                    this.scanLocations.put(loc, now);

                    var chunk = world.getChunkAt(loc.x(), loc.z());
                    plugin.chunkContainerExecutor().submit(chunk, ScanOptions.all()).whenComplete((s, e) -> {
                        if (s == null) {
                            plugin.getLogger().warning("Error occurred while scanning " + loc);
                        }
                        this.scanLocations.remove(loc);
                    });
                }
            }
        });
    }
}
