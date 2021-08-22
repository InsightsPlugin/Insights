package dev.frankheijden.insights.api.concurrent;

import dev.frankheijden.insights.api.InsightsPlugin;
import io.papermc.lib.PaperLib;
import java.util.concurrent.CompletableFuture;
import org.bukkit.HeightMap;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class ChunkTeleport {

    private final InsightsPlugin plugin;

    public ChunkTeleport(InsightsPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Teleports a player to a chunk.
     */
    public CompletableFuture<Result> teleportPlayerToChunk(Player player, World world, int x, int z, boolean gen) {
        CompletableFuture<Result> resultFuture = new CompletableFuture<>();
        PaperLib.getChunkAtAsync(world, x, z, gen).whenComplete((chunk, chunkErr) -> {
            if (chunkErr != null) {
                resultFuture.completeExceptionally(chunkErr);
                return;
            } else if (chunk == null) {
                resultFuture.complete(Result.NOT_GENERATED);
                return;
            }

            plugin.getServer().getScheduler().runTask(plugin, () -> {
                int blockX = (x << 4) + 8;
                int blockZ = (z << 4) + 8;
                int blockY = world.getHighestBlockYAt(blockX, blockZ, HeightMap.MOTION_BLOCKING) + 1;
                var loc = new Location(world, blockX + .5, blockY, blockZ + .5);
                PaperLib.teleportAsync(player, loc).whenComplete((success, tpErr) -> {
                    if (tpErr != null) {
                        resultFuture.completeExceptionally(tpErr);
                    } else if (Boolean.FALSE.equals(success)) {
                        resultFuture.complete(Result.FAILED);
                    } else {
                        resultFuture.complete(Result.SUCCESS);
                    }
                });
            });
        });

        return resultFuture;
    }

    public enum Result {
        NOT_GENERATED,
        FAILED,
        SUCCESS
    }
}
