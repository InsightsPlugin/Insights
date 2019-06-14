package net.frankheijden.insights.tasks;

import net.frankheijden.insights.Insights;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;

public class BossBarTask implements Runnable {
    private Insights plugin;

    public BossBarTask(Insights plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        for (Player player : plugin.bossBarUtils.bossBarDurationPlayers.keySet()) {
            if (System.currentTimeMillis() >= plugin.bossBarUtils.bossBarDurationPlayers.get(player)) {
                removePlayer(player);
            }
        }
    }

    private void removePlayer(Player player) {
        plugin.bossBarUtils.bossBarDurationPlayers.remove(player);

        BossBar bossBar = plugin.bossBarUtils.bossBarPlayers.get(player);
        if (bossBar != null) {
            bossBar.setVisible(false);
        }
    }
}
