package dev.frankheijden.insights.listeners;

import dev.frankheijden.insights.api.InsightsPlugin;
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
}
