package dev.frankheijden.insights.listeners;

import com.destroystokyo.paper.event.block.TNTPrimeEvent;
import dev.frankheijden.insights.api.InsightsPlugin;
import dev.frankheijden.insights.api.objects.wrappers.ScanObject;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

public class PaperEntityListener extends EntityListener {

    public PaperEntityListener(InsightsPlugin plugin) {
        super(plugin);
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
        var primerEntity = event.getPrimerEntity();
        var block = event.getBlock();

        if (primerEntity instanceof Player) {
            var player = (Player) primerEntity;

            handleRemoval(player, block.getLocation(), ScanObject.of(block.getType()), 1, false);
        } else {
            handleModification(block, -1);
        }
    }
}
