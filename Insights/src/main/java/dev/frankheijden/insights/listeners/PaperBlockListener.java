package dev.frankheijden.insights.listeners;

import com.destroystokyo.paper.event.block.BlockDestroyEvent;
import dev.frankheijden.insights.api.InsightsPlugin;
import dev.frankheijden.insights.api.annotations.AllowDisabling;
import dev.frankheijden.insights.api.listeners.InsightsListener;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

public class PaperBlockListener extends InsightsListener {

    public PaperBlockListener(InsightsPlugin plugin) {
        super(plugin);
    }

    @AllowDisabling
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockDestroy(BlockDestroyEvent event) {
        Block block = event.getBlock();
        handleModificationUsingCache(block.getLocation(), block.getType(), event.getNewState().getMaterial(), 1);
    }
}
