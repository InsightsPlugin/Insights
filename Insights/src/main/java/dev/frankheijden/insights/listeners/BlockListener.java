package dev.frankheijden.insights.listeners;

import com.destroystokyo.paper.MaterialTags;
import dev.frankheijden.insights.api.InsightsPlugin;
import dev.frankheijden.insights.api.annotations.AllowDisabling;
import dev.frankheijden.insights.api.concurrent.storage.ChunkDistributionStorage;
import dev.frankheijden.insights.api.concurrent.storage.WorldDistributionStorage;
import dev.frankheijden.insights.api.config.LimitEnvironment;
import dev.frankheijden.insights.api.config.Messages;
import dev.frankheijden.insights.api.config.Settings;
import dev.frankheijden.insights.api.config.limits.Limit;
import dev.frankheijden.insights.api.listeners.InsightsListener;
import dev.frankheijden.insights.api.utils.ChunkUtils;
import dev.frankheijden.insights.api.utils.StringUtils;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockMultiPlaceEvent;
import org.bukkit.event.block.BlockPistonEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.block.EntityBlockFormEvent;
import org.bukkit.event.block.FluidLevelChangeEvent;
import org.bukkit.event.block.LeavesDecayEvent;
import org.bukkit.event.block.SpongeAbsorbEvent;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

public class BlockListener extends InsightsListener {

    public BlockListener(InsightsPlugin plugin) {
        super(plugin);
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
        UUID uuid = player.getUniqueId();
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

        // Create limit environment
        LimitEnvironment env = new LimitEnvironment(player, worldUid);

        // Get the first (smallest) limit for the specific user (bypass permissions taken into account)
        Optional<Limit> limitOptional = plugin.getLimits().getFirstLimit(material, env);

        WorldDistributionStorage worldStorage = plugin.getWorldDistributionStorage();
        ChunkDistributionStorage chunkStorage = worldStorage.getChunkDistribution(worldUid);

        // In case of BlockMultiPlaceEvent, we need to take a different delta.
        int delta = event instanceof BlockMultiPlaceEvent
                ? ((BlockMultiPlaceEvent) event).getReplacedBlockStates().size()
                : 1;

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
                    chunkStorage.modify(chunkKey, material, -delta);
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
            if (count + delta > limit.getLimit()) {
                plugin.getMessages().getMessage(Messages.Key.LIMIT_REACHED)
                        .replace(
                                "limit", StringUtils.pretty(limit.getLimit()),
                                "name", limit.getName(),
                                "area", "chunk"
                        )
                        .color()
                        .sendTo(player);
                event.setCancelled(true);
                return;
            }

            // Else notify the user (if they have permission)
            if (player.hasPermission("insights.notifications")) {
                double progress = (double) (count + delta) / limit.getLimit();
                plugin.getNotifications().getCachedProgress(uuid, Messages.Key.LIMIT_NOTIFICATION)
                        .progress(progress)
                        .add(player)
                        .create()
                        .replace(
                                "name", limit.getName(),
                                "count", StringUtils.pretty(count + delta),
                                "limit", StringUtils.pretty(limit.getLimit())
                        )
                        .color()
                        .send();
            }
        }

        // Update the cache
        handleModification(chunk, event.getBlockReplacedState().getType(), material, delta);
    }

    /**
     * Handles the BlockBreakEvent.
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        Material material = block.getType();
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        Chunk chunk = block.getChunk();
        UUID worldUid = chunk.getWorld().getUID();
        long chunkKey = ChunkUtils.getKey(chunk);

        // Beds account for two block updates.
        int delta = Tag.BEDS.isTagged(material) ? 2 : 1;

        // Notify the user (if they have permission)
        notify:
        if (player.hasPermission("insights.notifications")) {
            // If the chunk is queued, stop check here (notification will be displayed when it completes).
            if (plugin.getWorldChunkScanTracker().isQueued(worldUid, chunkKey)) {
                break notify;
            }

            // Create limit environment
            LimitEnvironment env = new LimitEnvironment(player, worldUid);

            // Get the first (smallest) limit for the specific user (bypass permissions taken into account)
            Optional<Limit> limitOptional = plugin.getLimits().getFirstLimit(material, env);
            if (!limitOptional.isPresent()) break notify;
            Limit limit = limitOptional.get();

            WorldDistributionStorage worldStorage = plugin.getWorldDistributionStorage();
            ChunkDistributionStorage chunkStorage = worldStorage.getChunkDistribution(worldUid);

            // Create a runnable for the notification.
            Consumer<Boolean> notification = scan -> {
                int count = chunkStorage.count(chunkKey, limit.getMaterials(material)).orElse(0);

                // If this is executed after a scan, the data is already up to date.
                if (!scan) count -= delta;

                double progress = (double) count / limit.getLimit();
                plugin.getNotifications().getCachedProgress(uuid, Messages.Key.LIMIT_NOTIFICATION)
                        .progress(progress)
                        .add(player)
                        .create()
                        .replace(
                                "name", limit.getName(),
                                "count", StringUtils.pretty(count),
                                "limit", StringUtils.pretty(limit.getLimit())
                        )
                        .color()
                        .send();
            };

            // If the data is already stored, send the notification immediately.
            if (chunkStorage.contains(chunkKey)) {
                notification.accept(false);
            } else { // Else, we need to scan the chunk first.
                plugin.getChunkContainerExecutor().submit(chunk, true).thenRun(() -> notification.accept(true));
            }
        }

        // Update the cache
        handleModification(block, -delta);
    }

    @AllowDisabling
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBurn(BlockBurnEvent event) {
        handleModification(event.getBlock(), -1);
    }

    /**
     * Handles dispensers.
     */
    @AllowDisabling
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockDispense(BlockDispenseEvent event) {
        Block block = event.getBlock();
        BlockData blockData = block.getBlockData();

        // If block is not directional, we don't know the dispensed block's position.
        if (!(blockData instanceof Directional)) return;
        Block relative = block.getRelative(((Directional) blockData).getFacing());

        Material item = event.getItem().getType();

        // If the item is an empty bucket, the dispenser scoops up water.
        if (item == Material.BUCKET) {
            handleModification(relative, -1);
            return;
        }

        // Figure out the material the dispenser will output.
        Material material = null;
        if (MaterialTags.BUCKETS.isTagged(item) && item != Material.MILK_BUCKET) {
            material = Material.WATER; // If a bucket is dispensed, this results in a new water source
        } else if (MaterialTags.SHULKER_BOXES.isTagged(item)) {
            material = item; // If a shulker is dispensed, its dispensed as block.
        }

        if (material != null) {
            handleModification(relative.getChunk(), relative.getType(), material, 1);
        }
    }

    /**
     * Handles block explosions.
     */
    @AllowDisabling
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockExplode(BlockExplodeEvent event) {
        Block block = event.getBlock();

        // Beds account for two block updates.
        int delta = Tag.BEDS.isTagged(block.getType()) ? 2 : 1;
        handleModification(block, -delta);

        for (Block explodedBlock : event.blockList()) {
            handleModification(explodedBlock, -1);
        }
    }

    /**
     * Handles blocks melting/fading.
     */
    @AllowDisabling
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockFade(BlockFadeEvent event) {
        Block block = event.getBlock();
        handleModification(block.getChunk(), block.getType(), event.getNewState().getType(), 1);
    }

    /**
     * Handles water flowing.
     */
    @AllowDisabling
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockFromTo(BlockFromToEvent event) {
        Block block = event.getToBlock();

        // For some weird reason the "ToBlock" contains the from data...
        handleModification(block.getChunk(), block.getType(), event.getBlock().getType(), 1);
    }

    /**
     * Handles water drying up.
     */
    @AllowDisabling
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onFluidLevelChange(FluidLevelChangeEvent event) {
        if (event.getNewData().getMaterial() == Material.AIR) {
            handleModification(event.getBlock(), -1);
        }
    }

    @AllowDisabling
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockGrow(BlockGrowEvent event) {
        Block block = event.getBlock();
        handleModification(block.getChunk(), block.getType(), event.getNewState().getType(), 1);
    }

    @AllowDisabling
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockIgnite(BlockIgniteEvent event) {
        // Block transformed from block -> entity
        handleModification(event.getBlock(), -1);
    }

    @AllowDisabling
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockPistonExtend(BlockPistonExtendEvent event) {
        handlePistonEvent(event, event.getBlocks());
    }

    @AllowDisabling
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockPistonRetract(BlockPistonRetractEvent event) {
        handlePistonEvent(event, event.getBlocks());
    }

    /**
     * Handles pistons.
     */
    private void handlePistonEvent(BlockPistonEvent event, List<Block> blocks) {
        for (Block block : blocks) {
            Block relative = block.getRelative(event.getDirection());

            handleModification(block, -1);
            handleModification(relative.getChunk(), relative.getType(), block.getType(), 1);
        }
    }

    /**
     * Handles block spreads (grass growing).
     */
    @AllowDisabling
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockSpread(BlockSpreadEvent event) {
        Block block = event.getBlock();
        handleModification(block.getChunk(), block.getType(), event.getNewState().getType(), 1);
    }

    /**
     * Handles blocks forming by entities (Snowmans, FrostWalker enchantment).
     */
    @AllowDisabling
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityBlockForm(EntityBlockFormEvent event) {
        Block block = event.getBlock();
        handleModification(block.getChunk(), block.getType(), event.getNewState().getType(), 1);
    }

    /**
     * Handles leaves decaying.
     */
    @AllowDisabling
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onLeavesDecay(LeavesDecayEvent event) {
        handleModification(event.getBlock(), -1);
    }

    /**
     * Handles sponge absorbs.
     */
    @AllowDisabling
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onSpongeAbsorb(SpongeAbsorbEvent event) {
        Block block = event.getBlock();
        handleModification(block.getChunk(), Material.SPONGE, Material.WET_SPONGE, 1);
        for (BlockState state : event.getBlocks()) {
            handleModification(state.getChunk(), Material.WATER, state.getType(), 1);
        }
    }
}
