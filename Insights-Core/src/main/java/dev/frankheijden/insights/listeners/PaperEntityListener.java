package dev.frankheijden.insights.listeners;

import dev.frankheijden.insights.api.InsightsPlugin;
import dev.frankheijden.insights.api.objects.wrappers.ScanObject;
import io.papermc.paper.event.entity.EntityBreakByEntityEvent;
import io.papermc.paper.event.entity.EntityBreakEvent;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.TNTPrimeEvent;

public class PaperEntityListener extends EntityListener {

    public PaperEntityListener(InsightsPlugin plugin) {
        super(plugin);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityBreak(EntityBreakEvent event) {
        Entity entity = event.getEntity();
        if (entity.getType() != EntityType.CUSHION) return;

        Entity remover = event instanceof EntityBreakByEntityEvent
                ? ((EntityBreakByEntityEvent) event).getRemover()
                : null;
        handleEntityBreak(entity, remover);
    }

    /**
     * Handles the EntityRemoveFromWorldEvent as "catch-all" for entity removals.
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onEntityRemove(com.destroystokyo.paper.event.entity.EntityRemoveFromWorldEvent event) {
        handleEntityRemoveFromWorld(event.getEntity());
    }

    /**
     * Handles the TNTPrimeEvent for ignited TNT blocks.
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onTNTPrime(TNTPrimeEvent event) {
        var primingEntity = event.getPrimingEntity();
        var block = event.getBlock();

        if (primingEntity instanceof Player) {
            var player = (Player) primingEntity;

            handleRemoval(player, block.getLocation(), ScanObject.of(block.getType()), 1, false);
        } else {
            handleModification(block, -1);
        }
    }
}
