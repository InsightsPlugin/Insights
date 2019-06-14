package net.frankheijden.insights.utils;

import net.frankheijden.insights.Insights;
import net.frankheijden.insights.tasks.BossBarTask;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class BossBarUtils {
    private Insights plugin;

    public HashMap<Player, BossBar> bossBarPlayers;
    public HashMap<Player, Long> bossBarDurationPlayers;
    public BossBar defaultBossBar = null;
    public long bossBarDuration;
    public BossBarTask bossBarTask;
    public int bossBarTaskID;

    public BossBarUtils(Insights plugin) {
        this.plugin = plugin;
    }

    public void setupBossBarRunnable() {
        bossBarTask = new BossBarTask(plugin);
        bossBarTaskID = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, bossBarTask, 0, 1);
    }

    public void setupDefaultBossBar() {
        bossBarPlayers = new HashMap<>();
        bossBarDurationPlayers = new HashMap<>();

        if (plugin.config.GENERAL_NOTIFICATION_TYPE.toUpperCase().equals("BOSSBAR")) {
            BarColor barColor = BarColor.valueOf(plugin.config.GENERAL_NOTIFICATION_BOSSBAR_COLOR);
            BarStyle barStyle = BarStyle.valueOf(plugin.config.GENERAL_NOTIFICATION_BOSSBAR_STYLE);

            List<BarFlag> barFlags = new ArrayList<>();
            for (String barFlagString : plugin.config.GENERAL_NOTIFICATION_BOSSBAR_FLAGS) {
                barFlags.add(BarFlag.valueOf(barFlagString));
            }

            defaultBossBar = Bukkit.createBossBar("", barColor, barStyle, barFlags.toArray(new BarFlag[0]));
            bossBarDuration = plugin.config.GENERAL_NOTIFICATION_BOSSBAR_DURATION * 50; // ticks * 50 = milliseconds
        }
    }
}
