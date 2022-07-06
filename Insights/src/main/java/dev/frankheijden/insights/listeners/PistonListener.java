package dev.frankheijden.insights.listeners;

import dev.frankheijden.insights.api.InsightsPlugin;
import dev.frankheijden.insights.api.config.LimitEnvironment;
import dev.frankheijden.insights.api.region.Region;
import dev.frankheijden.insights.api.annotations.AllowPriorityOverride;
import dev.frankheijden.insights.api.concurrent.ScanOptions;
import dev.frankheijden.insights.api.concurrent.storage.Storage;
import dev.frankheijden.insights.api.config.limits.Limit;
import dev.frankheijden.insights.api.listeners.InsightsListener;
import dev.frankheijden.insights.api.objects.wrappers.ScanObject;
import dev.frankheijden.insights.api.region.RegionManager;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockPistonEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PistonListener extends InsightsListener {

    public PistonListener(InsightsPlugin plugin) {
        super(plugin);
    }

    @AllowPriorityOverride
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBlockPistonExtend(BlockPistonExtendEvent event) {
        handlePistonEvent(event, event.getBlocks());
    }

    @AllowPriorityOverride
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
            if (shouldCancelPistonMove(block, relative)) {
                event.setCancelled(true);
                break;
            }
        }
    }

    private boolean shouldCancelPistonMove(Block from, Block to) {
        boolean cancel = false;

        Material material = from.getType();
        RegionManager regionManager = plugin.regionManager();
        List<Region> fromRegions = regionManager.regionsAt(from.getLocation());
        List<Region> toRegions = regionManager.regionsAt(to.getLocation());
        List<Region> regions = new ArrayList<>();
        for (Region region : toRegions) {
            // Always allow piston pushes within the same region
            if (fromRegions.contains(region)) continue;
            regions.add(region);
        }

        for (Region region : regions) {
            LimitEnvironment env = new LimitEnvironment(null, Collections.singletonList(region));
            Limit limit = plugin.limits().firstLimit(from.getType(), env);
            if (limit != null) {
                // If the area is queued for a scan, cancel the event and wait for it to complete.
                if (regionManager.regionScanTracker().isQueued(region)) {
                    cancel = true;
                    continue;
                }

                Storage storage = regionManager.regionStorage().get(region);
                if (storage == null) {
                    regionManager.scan(region, ScanOptions.all());
                    cancel = true;
                    continue;
                }

                if (!cancel) {
                    cancel = storage.count(limit, ScanObject.of(material)) + 1 > limit.limitInfo(material).limit();
                }
            }
        }

        return cancel;
    }
}
