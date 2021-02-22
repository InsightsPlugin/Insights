package dev.frankheijden.insights.tasks;

import dev.frankheijden.insights.api.InsightsPlugin;
import dev.frankheijden.insights.api.concurrent.PlayerList;
import dev.frankheijden.insights.api.concurrent.ScanOptions;
import dev.frankheijden.insights.api.concurrent.storage.WorldStorage;
import dev.frankheijden.insights.api.tasks.InsightsAsyncTask;
import dev.frankheijden.insights.api.utils.ChunkUtils;
import io.papermc.lib.PaperLib;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.IntConsumer;

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
            Location loc = entry.getValue().getLocation();
            int chunkX = loc.getBlockX() >> 4;
            int chunkZ = loc.getBlockZ() >> 4;
            Set<Long> worldChunks = worldChunkMap.get(loc.getWorld().getUID());
            for (int x = -1; x <= 1; x++) {
                for (int z = -1; z <= 1; z++) {
                    worldChunks.add(ChunkUtils.getKey(chunkX + x, chunkZ + z));
                }
            }
        }

        WorldStorage worldStorage = plugin.getWorldStorage();
        for (Map.Entry<UUID, Set<Long>> entry : worldChunkMap.entrySet()) {
            Set<Long> loadedChunks = worldStorage.getWorld(entry.getKey()).getChunks();
            Set<Long> playerChunks = entry.getValue();
            playerChunks.removeIf(loadedChunks::contains);
        }

        int chunkCount = 0;
        for (Set<Long> chunks : worldChunkMap.values()) {
            chunkCount += chunks.size();
        }

        if (chunkCount == 0) {
            run.set(true);
            return;
        }

        final int size = chunkCount;
        AtomicInteger counter = new AtomicInteger();
        IntConsumer checker = i -> {
            if (counter.addAndGet(i) >= size) {
                run.set(true);
            }
        };

        Bukkit.getScheduler().runTask(plugin, () -> {
            for (Map.Entry<UUID, Set<Long>> entry : worldChunkMap.entrySet()) {
                World world = Bukkit.getWorld(entry.getKey());
                if (world == null) {
                    checker.accept(entry.getValue().size());
                    continue;
                }

                for (Long key : entry.getValue()) {
                    PaperLib.getChunkAtAsync(world, ChunkUtils.getX(key), ChunkUtils.getZ(key)).thenAccept(chunk -> {
                        if (chunk == null) {
                            checker.accept(1);
                            return;
                        }

                        plugin.getChunkContainerExecutor().submit(chunk, ScanOptions.all()).whenComplete((s, e) -> {
                            checker.accept(1);
                        });
                    }).exceptionally(err -> {
                        checker.accept(1);
                        return null;
                    });
                }
            }
        });
    }
}
