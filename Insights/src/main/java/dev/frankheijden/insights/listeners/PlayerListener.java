package dev.frankheijden.insights.listeners;

import dev.frankheijden.insights.api.InsightsPlugin;
import dev.frankheijden.insights.api.concurrent.storage.ChunkDistributionStorage;
import dev.frankheijden.insights.api.concurrent.storage.WorldDistributionStorage;
import dev.frankheijden.insights.api.config.Messages;
import dev.frankheijden.insights.api.config.Settings;
import dev.frankheijden.insights.api.config.limits.Limit;
import dev.frankheijden.insights.api.listeners.InsightsListener;
import dev.frankheijden.insights.api.utils.ChunkUtils;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import java.util.Optional;
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
        Block block = event.getBlock();
        Material material = block.getType();
        Player player = event.getPlayer();
        Chunk chunk = block.getChunk();
        UUID worldUid = chunk.getWorld().getUID();
        long chunkKey = ChunkUtils.getKey(chunk);

        // If the chunk is queued for scanning, notify the player & cancel.
        if (plugin.getWorldChunkScanTracker().isQueued(worldUid, chunkKey)) {
            plugin.getMessages().getMessage(Messages.Key.CHUNK_SCAN_QUEUED)
                    .color()
                    .sendTo(player);
            event.setCancelled(true);
            return;
        }

        // Get the first (smallest) limit for the specific user (bypass permissions taken into account)
        Optional<Limit> limitOptional = plugin.getLimits().getFirstLimit(block, player);

        WorldDistributionStorage worldStorage = plugin.getWorldDistributionStorage();
        ChunkDistributionStorage chunkStorage = worldStorage.getChunkDistribution(worldUid);

        // If a limit is present, the chunk is not known, and ChunkScanMode is set to MODIFICATION, scan the chunk
        if (limitOptional.isPresent() && !chunkStorage.contains(chunkKey)
                && plugin.getSettings().CHUNK_SCAN_MODE == Settings.ChunkScanMode.MODIFICATION) {
            // Notify the user scan started
            plugin.getMessages().getMessage(Messages.Key.CHUNK_SCAN_STARTED)
                    .color()
                    .sendTo(player);

            // Submit the chunk for scanning
            plugin.getChunkContainerExecutor().submit(chunk, true).whenComplete((map, err) -> {
                // Subtract block from BlockPlaceEvent as it was cancelled
                // Can't subtract one from the given map, as a copied version is stored.
                if (map.containsKey(material)) {
                    chunkStorage.modify(chunkKey, material, -1);
                }

                // Notify the user scan completed
                plugin.getMessages().getMessage(Messages.Key.CHUNK_SCAN_COMPLETED)
                        .color()
                        .sendTo(player);
            });
            event.setCancelled(true);
            return;
        }

        if (limitOptional.isPresent()) {
            Limit limit = limitOptional.get();
            int count = chunkStorage.count(chunkKey, limit.getMaterials(material)).orElse(0);

            // If count is beyond limit, act
            if (count + 1 > limit.getLimit()) {
                player.sendMessage("You reached the limit (" + count + "/" + limit.getLimit() + ")!");
                event.setCancelled(true);
                return;
            }
        }

        // Update the cache
        chunkStorage.modify(chunkKey, material, 1);
    }
}
