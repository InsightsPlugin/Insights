package net.frankheijden.insights.tasks;

import net.frankheijden.insights.Insights;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class BossBarTask implements Runnable {
    private Insights plugin;

    public BossBarTask(Insights plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        List<Player> playersToRemove = new ArrayList<>();
        for (Player player : plugin.getBossBarUtils().bossBarDurationPlayers.keySet()) {
            if (System.currentTimeMillis() >= plugin.getBossBarUtils().bossBarDurationPlayers.get(player)) {
                playersToRemove.add(player);
                removePlayer(player);
            }
        }

        playersToRemove.forEach((player) -> plugin.getBossBarUtils().bossBarDurationPlayers.remove(player));
    }

    private void removePlayer(Player player) {
        BossBar bossBar = plugin.getBossBarUtils().bossBarPlayers.get(player);
        if (bossBar != null) {
            bossBar.setVisible(false);
        }
    }
}
