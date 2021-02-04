package dev.frankheijden.insights.listeners;

import dev.frankheijden.insights.api.InsightsPlugin;
import dev.frankheijden.insights.api.annotations.AllowDisabling;
import dev.frankheijden.insights.api.listeners.InsightsListener;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.world.StructureGrowEvent;

public class WorldListener extends InsightsListener {

    public WorldListener(InsightsPlugin plugin) {
        super(plugin);
    }

    /**
     * Handles structure grows (trees/giant mushrooms).
     */
    @AllowDisabling
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onStructureGrow(StructureGrowEvent event) {
        Block block = event.getLocation().getBlock();
        handleModification(block, -1);

        for (BlockState state : event.getBlocks()) {
            handleModification(state, 1);
        }
    }
}
