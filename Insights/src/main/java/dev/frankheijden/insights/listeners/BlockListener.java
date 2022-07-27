package dev.frankheijden.insights.listeners;

import dev.frankheijden.insights.Insights;
import dev.frankheijden.insights.api.addons.AddonRegion;
import dev.frankheijden.insights.api.region.Region;
import dev.frankheijden.insights.api.annotations.AllowDisabling;
import dev.frankheijden.insights.api.annotations.AllowPriorityOverride;
import dev.frankheijden.insights.api.listeners.InsightsListener;
import dev.frankheijden.insights.api.objects.wrappers.ScanObject;
import dev.frankheijden.insights.api.util.MaterialTags;
import dev.frankheijden.insights.api.utils.BlockUtils;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.Directional;
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
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.block.EntityBlockFormEvent;
import org.bukkit.event.block.FluidLevelChangeEvent;
import org.bukkit.event.block.LeavesDecayEvent;
import org.bukkit.event.block.SpongeAbsorbEvent;
import java.util.List;
import java.util.Optional;

public class BlockListener extends InsightsListener {

    public BlockListener(Insights plugin) {
        super(plugin);
    }

    /**
     * Handles the BlockPlaceEvent for players.
     * Chunk limitations are applied in here on the lowest (first) event priority.
     */
    @AllowPriorityOverride
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        var block = event.getBlock();
        var location = block.getLocation();
        var material = block.getType();
        var player = event.getPlayer();

        // In case of BlockMultiPlaceEvent, we need to do multiple samples.
        // A closing tripwire hook triggers a BlockMultiPlaceEvent for all attached strings,
        // but since they already exist in the world we do not need to count them.
        if (event instanceof BlockMultiPlaceEvent multiPlaceEvent && material != Material.TRIPWIRE_HOOK) {
            List<BlockState> replacedBlockStates = multiPlaceEvent.getReplacedBlockStates();
            for (BlockState state : replacedBlockStates) {
                if (handleModification(player, state.getLocation(), ScanObject.of(material), 1, false)) {
                    event.setCancelled(true);
                    break;
                }
            }
        } else {
            if (handleModification(player, location, ScanObject.of(material), 1, false)) {
                event.setCancelled(true);
            }
        }
    }

    /**
     * Handles the BlockPlaceEvent for players, monitoring changes
     * Chunk limitations are not applied here, they are only monitored and displayed to the player.
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockPlaceMonitor(BlockPlaceEvent event) {
        var block = event.getBlock();
        var location = block.getLocation();
        var material = block.getType();
        var player = event.getPlayer();

        // In case of BlockMultiPlaceEvent, we need to update the cache differently.
        // A closing tripwire hook triggers a BlockMultiPlaceEvent for all attached strings,
        // but since they already exist in the world we do not need to count them.
        if (event instanceof BlockMultiPlaceEvent && material != Material.TRIPWIRE_HOOK) {
            List<BlockState> replacedBlockStates = ((BlockMultiPlaceEvent) event).getReplacedBlockStates();
            for (BlockState state : replacedBlockStates) {
                var loc = state.getLocation();
                handleModificationUsingCache(loc, state.getType(), material, 1);
            }
        } else {
            handleModificationUsingCache(location, event.getBlockReplacedState().getType(), material, 1);
        }

        // No need to add any delta, cache is already updated
        evaluateModification(player, location, ScanObject.of(material), 0);
    }

    /**
     * Handles the BlockBreakEvent.
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        var block = event.getBlock();
        var location = block.getLocation();
        var material = block.getType();
        var player = event.getPlayer();

        int delta = -1;
        if (Tag.BEDS.isTagged(material)) {
            Optional<Block> blockOptional = BlockUtils.getOtherHalf(block);
            if (blockOptional.isPresent()) {
                var otherHalf = blockOptional.get();

                // Update the other half's location
                handleModificationUsingCache(otherHalf.getLocation(), material, delta);
            }
        }

        // Handle the removal
        boolean handled = handleModification(player, location, ScanObject.of(material), delta, false);

        // Need to check if block above broken block was a block which needs support (solid block below it),
        // and subtract that from the cache as well (only if it does not make a sound when broken, i.e. REDSTONE_WIRE)
        var aboveBlock = getTopNonGravityBlock(block);
        var aboveMaterial = aboveBlock.getType();
        if (MaterialTags.NEEDS_GROUND.isTagged(aboveMaterial)) {
            handled |= handleModification(player, aboveBlock.getLocation(), ScanObject.of(aboveMaterial), delta, false);
        }

        if (!handled) {
            evaluateModification(player, location, ScanObject.of(material), 0);
        }
    }

    private Block getTopNonGravityBlock(Block start) {
        do {
            start = start.getRelative(BlockFace.UP);
        } while (start.getType().hasGravity());
        return start;
    }

    @AllowDisabling
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBurn(BlockBurnEvent event) {
        handleModificationUsingCache(event.getBlock(), -1);
    }

    /**
     * Handles dispensers.
     */
    @AllowDisabling
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockDispense(BlockDispenseEvent event) {
        var block = event.getBlock();
        var blockData = block.getBlockData();

        // If block is not directional, we don't know the dispensed block's position.
        if (!(blockData instanceof Directional)) return;
        var relative = block.getRelative(((Directional) blockData).getFacing());

        Material item = event.getItem().getType();

        // If the item is an empty bucket, the dispenser scoops up water.
        if (item == Material.BUCKET) {
            handleModificationUsingCache(relative, -1);
            return;
        }

        // Figure out the material the dispenser will output.
        Material material = null;
        if (MaterialTags.BUCKETS.isTagged(item) && item != Material.MILK_BUCKET) {
            material = Material.WATER; // If a bucket is dispensed, this results in a new water source
        } else if (Tag.SHULKER_BOXES.isTagged(item)) {
            material = item; // If a shulker is dispensed, its dispensed as block.
        }

        if (material != null) {
            handleModificationUsingCache(relative.getLocation(), relative.getType(), material, 1);
        }
    }

    /**
     * Handles block explosions.
     */
    @AllowDisabling
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockExplode(BlockExplodeEvent event) {
        var block = event.getBlock();
        var location = block.getLocation();

        var playerListener = ((Insights) plugin).listenerManager().getPlayerListener();
        Optional<PlayerListener.ExplodedBed> bedOptional = playerListener.getIntentionalDesignBugAt(location);
        if (bedOptional.isPresent()) {
            var explodedBed = bedOptional.get();
            var material = explodedBed.getMaterial();
            handleModificationUsingCache(explodedBed.getHead(), material, -1);
            handleModificationUsingCache(explodedBed.getFoot(), material, -1);
        } else {
            handleModificationUsingCache(location, block.getType(), -1);
        }

        for (Block explodedBlock : event.blockList()) {
            handleModificationUsingCache(explodedBlock, -1);
        }
    }

    /**
     * Handles blocks melting/fading.
     */
    @AllowDisabling
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockFade(BlockFadeEvent event) {
        var block = event.getBlock();
        handleModificationUsingCache(block.getLocation(), block.getType(), event.getNewState().getType(), 1);
    }

    /**
     * Handles water flowing.
     */
    @AllowDisabling
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockFromTo(BlockFromToEvent event) {
        var block = event.getToBlock();

        // For some weird reason the "ToBlock" contains the from data...
        handleModificationUsingCache(block.getLocation(), block.getType(), event.getBlock().getType(), 1);
    }

    /**
     * Handles water drying up.
     */
    @AllowDisabling
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onFluidLevelChange(FluidLevelChangeEvent event) {
        if (event.getNewData().getMaterial() == Material.AIR) {
            handleModificationUsingCache(event.getBlock(), -1);
        }
    }

    @AllowDisabling
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockGrow(BlockGrowEvent event) {
        var block = event.getBlock();
        handleModificationUsingCache(block.getLocation(), block.getType(), event.getNewState().getType(), 1);
    }

    @AllowDisabling
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockIgnite(BlockIgniteEvent event) {
        // Block transformed from block -> entity
        handleModificationUsingCache(event.getBlock(), -1);
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
        if (blocks.isEmpty()) return;

        var materials = new Material[blocks.size()];
        for (var i = 0; i < blocks.size(); i++) {
            var block = blocks.get(i);
            var material = block.getType();
            handleModificationUsingCache(block.getLocation(), material, -1);
            materials[i] = material;
        }

        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            for (var i = 0; i < blocks.size(); i++) {
                var relative = blocks.get(i).getRelative(event.getDirection());
                var material = relative.getType();
                if (materials[i] == material) {
                    handleModificationUsingCache(relative.getLocation(), material, 1);
                }
            }
        }, 3L); // Ensures blocks have been updated after piston move
    }

    /**
     * Handles block spreads (grass growing).
     */
    @AllowDisabling
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockSpread(BlockSpreadEvent event) {
        var block = event.getBlock();
        handleModificationUsingCache(block.getLocation(), block.getType(), event.getNewState().getType(), 1);
    }

    /**
     * Handles blocks forming by entities (Snowmans, FrostWalker enchantment).
     */
    @AllowDisabling
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityBlockForm(EntityBlockFormEvent event) {
        var block = event.getBlock();
        handleModificationUsingCache(block.getLocation(), block.getType(), event.getNewState().getType(), 1);
    }

    /**
     * Handles leaves decaying.
     */
    @AllowDisabling
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onLeavesDecay(LeavesDecayEvent event) {
        handleModificationUsingCache(event.getBlock(), -1);
    }

    /**
     * Handles sponge absorbs.
     */
    @AllowDisabling
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onSpongeAbsorb(SpongeAbsorbEvent event) {
        var block = event.getBlock();
        handleModificationUsingCache(block.getLocation(), Material.SPONGE, Material.WET_SPONGE, 1);
        for (BlockState state : event.getBlocks()) {
            handleModificationUsingCache(state.getLocation(), Material.WATER, state.getType(), 1);
        }
    }

    /**
     * Handles redstone.
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void onBlockRedstone(BlockRedstoneEvent event) {
        var block = event.getBlock();
        var loc = block.getLocation();
        List<Region> regions = plugin.regionManager().regionsAt(loc);

        boolean cancel = false;
        for (Region region : regions) {
            cancel |= plugin.redstoneUpdateCount().increment(region) > plugin.settings().REDSTONE_UPDATE_LIMITER_LIMIT
                || (plugin.settings().REDSTONE_UPDATE_LIMITER_BLOCK_OUTSIDE_REGION && !(region instanceof AddonRegion));
        }

        if (cancel) {
            event.setNewCurrent(0);
        }
    }
}
