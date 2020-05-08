package net.frankheijden.insights.listeners;

import net.frankheijden.insights.managers.CacheManager;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.*;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityExplodeEvent;

import java.util.Objects;

public class CacheListener implements Listener {

    private static final CacheManager cacheManager = CacheManager.getInstance();

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBurn(BlockBurnEvent event) {
        remove(event.getBlock());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockExplode(BlockExplodeEvent event) {
        event.blockList().forEach(this::remove);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockFade(BlockFadeEvent event) {
        change(event.getBlock(), event.getNewState().getType());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockForm(BlockFormEvent event) {
        Block block = event.getBlock();
        change(block.getLocation(), block.getType(), event.getNewState().getType());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockSpread(BlockSpreadEvent event) {
        change(event.getBlock(), event.getSource().getType());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityBlockForm(EntityBlockFormEvent event) {
        Block block = event.getBlock();
        change(block.getLocation(), block.getType(), event.getNewState().getType());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onLeavesDecay(LeavesDecayEvent event) {
        remove(event.getBlock());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityExplodeEvent(EntityExplodeEvent event) {
        event.blockList().forEach(this::remove);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityChangeBlock(EntityChangeBlockEvent event) {
        change(event.getBlock(), event.getTo());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlock(BlockFromToEvent event) {
        change(event.getToBlock(), event.getBlock().getType());
    }

    private void remove(Block block) {
        change(block, Material.AIR);
    }

    private void change(Block from, Material to) {
        change(from.getLocation(), from.getType(), to);
    }

    private void change(Location loc, Material from, Material to) {
        if (from.equals(to)) return;
        cacheManager.getSelections(loc)
                .map(cacheManager::getCache)
                .filter(Objects::nonNull)
                .forEach(c -> {
                    c.updateCache(from.name(), -1);
                    c.updateCache(to.name(), 1);
                });
    }
}
