package dev.frankheijden.insights.listeners;

import dev.frankheijden.insights.api.InsightsPlugin;
import dev.frankheijden.insights.api.config.Messages;
import dev.frankheijden.insights.api.config.Settings;
import dev.frankheijden.insights.api.listeners.InsightsListener;
import dev.frankheijden.insights.api.utils.ChunkUtils;
import org.bukkit.Chunk;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import java.util.UUID;

public class PlayerListener extends InsightsListener {

    public PlayerListener(InsightsPlugin plugin) {
        super(plugin);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerJoin(PlayerJoinEvent event) {
        plugin.getPlayerList().addPlayer(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerJoinEvent event) {
        plugin.getPlayerList().removePlayer(event.getPlayer());
    }

    /**
     * Handles the BlockPlaceEvent for players.
     * Chunk limitations are applied in here on the lowest (first) event priority.
     */
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        Chunk chunk = event.getBlock().getChunk();
        UUID worldUid = chunk.getWorld().getUID();
        long chunkKey = ChunkUtils.getKey(chunk);

        if (plugin.getWorldChunkScanTracker().isQueued(worldUid, chunkKey)) {
            plugin.getMessages().getMessage(Messages.Key.CHUNK_SCAN_QUEUED)
                    .color()
                    .sendTo(player);
            event.setCancelled(true);
            return;
        }

        if (!plugin.getWorldDistributionStorage().contains(worldUid, chunkKey)
                && plugin.getSettings().CHUNK_SCAN_MODE == Settings.ChunkScanMode.MODIFICATION) {
            plugin.getMessages().getMessage(Messages.Key.CHUNK_SCAN_STARTED)
                    .color()
                    .sendTo(player);
            plugin.getChunkContainerExecutor().submit(chunk, true).whenComplete((map, err) -> {
                plugin.getMessages().getMessage(Messages.Key.CHUNK_SCAN_COMPLETED)
                        .color()
                        .sendTo(player);
            });
            event.setCancelled(true);
            return;
        }
    }
}
