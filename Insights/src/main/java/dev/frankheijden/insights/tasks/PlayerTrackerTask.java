package dev.frankheijden.insights.tasks;

import dev.frankheijden.insights.api.InsightsPlugin;
import dev.frankheijden.insights.api.concurrent.PlayerList;
import dev.frankheijden.insights.api.concurrent.storage.WorldDistributionStorage;
import dev.frankheijden.insights.api.tasks.InsightsAsyncTask;
import dev.frankheijden.insights.api.utils.ChunkUtils;
import dev.frankheijden.insights.api.utils.SetUtils;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.entity.Player;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class PlayerTrackerTask extends InsightsAsyncTask {

    public PlayerTrackerTask(InsightsPlugin plugin) {
        super(plugin);
    }

    @Override
    protected void runInternal() {
        PlayerList playerList = plugin.getPlayerList();

        Map<UUID, Set<Long>> worldChunkMap = new HashMap<>();
        for (World world : Bukkit.getWorlds()) {
            worldChunkMap.put(world.getUID(), new HashSet<>(playerList.size() * 9));
        }

        for (Map.Entry<UUID, Player> entry : playerList) {
            Chunk chunk = entry.getValue().getChunk();
            Set<Long> worldChunks = worldChunkMap.get(chunk.getWorld().getUID());
            for (int x = -1; x <= 1; x++) {
                for (int z = -1; z <= 1; z++) {
                    worldChunks.add(ChunkUtils.getKey(chunk.getX() + x, chunk.getZ() + z));
                }
            }
        }

        WorldDistributionStorage worldDistributionStorage = plugin.getWorldDistributionStorage();
        for (Map.Entry<UUID, Set<Long>> entry : worldChunkMap.entrySet()) {
            Set<Long> loadedChunks = worldDistributionStorage.getChunkDistribution(entry.getKey()).getKeys();
            Set<Long> playerChunks = entry.getValue();
            Set<Long> intersected = SetUtils.intersect(loadedChunks, playerChunks);
            playerChunks.removeIf(intersected::contains);
        }

        int chunkCount = 0;
        for (Set<Long> chunks : worldChunkMap.values()) {
            chunkCount += chunks.size();
        }

        if (chunkCount == 0) return;
        int size = chunkCount;
        Bukkit.getScheduler().runTask(plugin, () -> {
            CompletableFuture<?>[] futures = new CompletableFuture[size];
            int i = 0;

            for (Map.Entry<UUID, Set<Long>> entry : worldChunkMap.entrySet()) {
                World world = Bukkit.getWorld(entry.getKey());
                if (world == null) continue;

                for (Long key : entry.getValue()) {
                    Chunk chunk = world.getChunkAt(ChunkUtils.getX(key), ChunkUtils.getZ(key));
                    futures[i++] = plugin.getChunkContainerExecutor().submit(chunk, true);
                }
            }

            CompletableFuture.allOf(futures).whenComplete((v, err) -> run.set(true));
        });
    }
}
