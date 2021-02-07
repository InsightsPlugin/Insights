package dev.frankheijden.insights.listeners;

import dev.frankheijden.insights.api.InsightsPlugin;
import dev.frankheijden.insights.api.concurrent.storage.ChunkStorage;
import dev.frankheijden.insights.api.concurrent.storage.DistributionStorage;
import dev.frankheijden.insights.api.concurrent.storage.WorldStorage;
import dev.frankheijden.insights.api.config.limits.Limit;
import dev.frankheijden.insights.api.listeners.InsightsListener;
import dev.frankheijden.insights.api.utils.BlockUtils;
import dev.frankheijden.insights.api.utils.ChunkUtils;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockPistonEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class PistonListener extends InsightsListener {

    public PistonListener(InsightsPlugin plugin) {
        super(plugin);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBlockPistonExtend(BlockPistonExtendEvent event) {
        handlePistonEvent(event, event.getBlocks());
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBlockPistonRetract(BlockPistonRetractEvent event) {
        handlePistonEvent(event, event.getBlocks());
    }

    /**
     * Handles pistons, blocks the event if necessary.
     * Events are cancelled whenever:
     *   - The chunk is queued for scanning
     *   - The chunk hasn't been scanned yet
     *   - The limit was surpassed (lowest limit)
     * Events are allowed whenever:
     *   - Pushes/retractions happen within the same chunk
     *   - No limit exists for the pushed/retracted material
     */
    private void handlePistonEvent(BlockPistonEvent event, List<Block> blocks) {
        for (Block block : blocks) {
            Block relative = block.getRelative(event.getDirection());
            if (handlePistonBlock(block, relative)) {
                event.setCancelled(true);
                break;
            }
        }
    }

    private boolean handlePistonBlock(Block from, Block to) {
        // Always allow piston pushes within same chunk.
        if (BlockUtils.isSameChunk(from, to)) return false;

        Material material = from.getType();
        Optional<Limit> limitOptional = plugin.getLimits().getFirstLimit(material, limit -> true);

        // If no limit is present, allow the block to be moved.
        if (!limitOptional.isPresent()) return false;

        Chunk chunk = to.getChunk();
        UUID worldUid = chunk.getWorld().getUID();
        long chunkKey = ChunkUtils.getKey(chunk);

        // If the chunk is already queued, cancel the event and wait for the chunk to complete.
        if (plugin.getWorldChunkScanTracker().isQueued(worldUid, chunkKey)) return true;

        WorldStorage worldStorage = plugin.getWorldStorage();
        ChunkStorage chunkStorage = worldStorage.getWorld(worldUid);
        Optional<DistributionStorage> storageOptional = chunkStorage.get(chunkKey);

        // If the storage is not present, scan it & cancel the event.
        if (!storageOptional.isPresent()) {
            plugin.getChunkContainerExecutor().submit(chunk);
            return true;
        }

        // Else, the storage is present, and we can apply a limit.
        DistributionStorage storage = storageOptional.get();
        Limit limit = limitOptional.get();

        // Cache doesn't need to updated here just yet, needs to be done in MONITOR event phase.
        return storage.count(limit, material) + 1 > limit.getLimit();
    }
}
