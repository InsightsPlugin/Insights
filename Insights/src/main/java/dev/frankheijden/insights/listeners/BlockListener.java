package dev.frankheijden.insights.listeners;

import dev.frankheijden.insights.api.InsightsPlugin;
import dev.frankheijden.insights.api.annotations.AllowDisabling;
import dev.frankheijden.insights.api.listeners.InsightsListener;
import dev.frankheijden.insights.api.util.MaterialTags;
import org.bukkit.Location;
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
        Location location = block.getLocation();
        Material material = block.getType();
        Player player = event.getPlayer();

        // In case of BlockMultiPlaceEvent, we need to take a different delta.
        int delta = event instanceof BlockMultiPlaceEvent
                ? ((BlockMultiPlaceEvent) event).getReplacedBlockStates().size()
                : 1;

        if (handleAddition(player, location, material, delta)) {
            event.setCancelled(true);
        }
    }

    /**
     * Handles the BlockPlaceEvent for players, monitoring changes
     * Chunk limitations are not applied here, they are only monitored and displayed to the player.
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockPlaceMonitor(BlockPlaceEvent event) {
        Block block = event.getBlock();
        Location location = block.getLocation();
        Material material = block.getType();
        Player player = event.getPlayer();

        // In case of BlockMultiPlaceEvent, we need to take a different delta.
        int delta = event instanceof BlockMultiPlaceEvent
                ? ((BlockMultiPlaceEvent) event).getReplacedBlockStates().size()
                : 1;

        evaluateAddition(player, location, material, delta);

        // Update the cache
        handleModification(location, event.getBlockReplacedState().getType(), material, delta);
    }

    /**
     * Handles the BlockBreakEvent.
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        Location location = block.getLocation();
        Material material = block.getType();
        Player player = event.getPlayer();

        // Beds account for two block updates.
        int delta = Tag.BEDS.isTagged(material) ? 2 : 1;

        // Handle the removal
        handleRemoval(player, location, material, delta);
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
        } else if (Tag.SHULKER_BOXES.isTagged(item)) {
            material = item; // If a shulker is dispensed, its dispensed as block.
        }

        if (material != null) {
            handleModification(relative.getLocation(), relative.getType(), material, 1);
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
        handleModification(block.getLocation(), block.getType(), event.getNewState().getType(), 1);
    }

    /**
     * Handles water flowing.
     */
    @AllowDisabling
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockFromTo(BlockFromToEvent event) {
        Block block = event.getToBlock();

        // For some weird reason the "ToBlock" contains the from data...
        handleModification(block.getLocation(), block.getType(), event.getBlock().getType(), 1);
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
        handleModification(block.getLocation(), block.getType(), event.getNewState().getType(), 1);
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

            handleModification(block.getLocation(), block.getType(), -1);
            handleModification(relative.getLocation(), block.getType(), 1);
        }
    }

    /**
     * Handles block spreads (grass growing).
     */
    @AllowDisabling
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockSpread(BlockSpreadEvent event) {
        Block block = event.getBlock();
        handleModification(block.getLocation(), block.getType(), event.getNewState().getType(), 1);
    }

    /**
     * Handles blocks forming by entities (Snowmans, FrostWalker enchantment).
     */
    @AllowDisabling
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityBlockForm(EntityBlockFormEvent event) {
        Block block = event.getBlock();
        handleModification(block.getLocation(), block.getType(), event.getNewState().getType(), 1);
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
        handleModification(block.getLocation(), Material.SPONGE, Material.WET_SPONGE, 1);
        for (BlockState state : event.getBlocks()) {
            handleModification(state.getLocation(), Material.WATER, state.getType(), 1);
        }
    }
}
