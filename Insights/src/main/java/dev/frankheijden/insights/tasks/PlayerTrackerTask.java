package dev.frankheijden.insights.tasks;

import dev.frankheijden.insights.api.InsightsPlugin;
import dev.frankheijden.insights.api.concurrent.ScanOptions;
import dev.frankheijden.insights.api.concurrent.storage.WorldStorage;
import dev.frankheijden.insights.api.objects.chunk.ChunkLocation;
import dev.frankheijden.insights.api.tasks.InsightsAsyncTask;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import java.time.Duration;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerTrackerTask extends InsightsAsyncTask {

    private final Map<ChunkLocation, Long> scanLocations = new ConcurrentHashMap<>();
    private final long scanTimeout = Duration.ofSeconds(5).toNanos();

    public PlayerTrackerTask(InsightsPlugin plugin) {
        super(plugin);
    }

    @Override
    public void run() {
        long nanos = System.nanoTime();
        this.scanLocations.entrySet().removeIf(entry -> {
            if (nanos < entry.getValue() + scanTimeout) return false;
            plugin.getLogger().warning("Chunk in " + entry.getKey() + " timed out!");
            return true;
        });

        WorldStorage worldStorage = plugin.getWorldStorage();
        Set<ChunkLocation> locations = new HashSet<>();
        for (Map.Entry<UUID, Player> entry : plugin.getPlayerList()) {
            Location location = entry.getValue().getLocation();
            World world = location.getWorld();
            Set<Long> loadedChunks = worldStorage.getWorld(world.getUID()).getChunks();

            int chunkX = location.getBlockX() >> 4;
            int chunkZ = location.getBlockZ() >> 4;
            for (int x = -1; x <= 1; x++) {
                for (int z = -1; z <= 1; z++) {
                    ChunkLocation loc = new ChunkLocation(world, chunkX + x, chunkZ + z);
                    if (!loadedChunks.contains(loc.getKey()) && !this.scanLocations.containsKey(loc)) {
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
                World world = loc.getWorld();
                if (world.isChunkLoaded(loc.getX(), loc.getZ())) {
                    this.scanLocations.put(loc, now);

                    Chunk chunk = world.getChunkAt(loc.getX(), loc.getZ());
                    plugin.getChunkContainerExecutor().submit(chunk, ScanOptions.all()).whenComplete((s, e) -> {
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
