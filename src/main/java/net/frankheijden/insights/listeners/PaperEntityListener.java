package net.frankheijden.insights.listeners;

import net.frankheijden.insights.events.EntityRemoveFromWorldEvent;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class PaperEntityListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityRemoveFromWorld(com.destroystokyo.paper.event.entity.EntityRemoveFromWorldEvent event) {
        Bukkit.getPluginManager().callEvent(new EntityRemoveFromWorldEvent(event.getEntity()));
    }
}
