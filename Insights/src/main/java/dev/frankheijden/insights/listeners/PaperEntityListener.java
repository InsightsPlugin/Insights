package dev.frankheijden.insights.listeners;

import dev.frankheijden.insights.api.InsightsPlugin;
import dev.frankheijden.insights.api.events.EntityRemoveFromWorldEvent;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

public class PaperEntityListener extends EntityListener {

    public PaperEntityListener(InsightsPlugin plugin) {
        super(plugin);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEntityRemove(com.destroystokyo.paper.event.entity.EntityRemoveFromWorldEvent event) {
        Bukkit.getPluginManager().callEvent(new EntityRemoveFromWorldEvent(event.getEntity()));
    }
}
