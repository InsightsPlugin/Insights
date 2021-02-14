package dev.frankheijden.insights.listeners;

import dev.frankheijden.insights.api.InsightsPlugin;
import dev.frankheijden.insights.api.listeners.InsightsListener;
import dev.frankheijden.insights.api.tasks.UpdateCheckerTask;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerListener extends InsightsListener {

    public PlayerListener(InsightsPlugin plugin) {
        super(plugin);
    }

    /**
     * Handles the PlayerJoinEvent, updating the concurrent PlayerList and checking for updates.
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        plugin.getPlayerList().addPlayer(player);

        if (player.hasPermission("insights.update")) {
            UpdateCheckerTask.check(player);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerJoinEvent event) {
        plugin.getPlayerList().removePlayer(event.getPlayer());
    }
}
