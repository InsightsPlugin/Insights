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
import java.util.UUID;

public class BossBarUtils {
    private Insights plugin;

    public HashMap<UUID, BossBar> scanBossBarPlayers;

    public HashMap<Player, BossBar> bossBarPlayers;
    public HashMap<Player, Long> bossBarDurationPlayers;
    public long bossBarDuration;
    public BossBarTask bossBarTask;
    public int bossBarTaskID;

    public BossBarUtils(Insights plugin) {
        this.plugin = plugin;

        scanBossBarPlayers = new HashMap<>();
        bossBarPlayers = new HashMap<>();
        bossBarDurationPlayers = new HashMap<>();
    }

    public void setupBossBarRunnable() {
        bossBarTask = new BossBarTask(plugin);
        bossBarTaskID = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, bossBarTask, 0, 1);
    }

    public BossBar createNewBossBar() {
        BarColor barColor = BarColor.valueOf(plugin.getConfiguration().GENERAL_NOTIFICATION_BOSSBAR_COLOR);
        BarStyle barStyle = BarStyle.valueOf(plugin.getConfiguration().GENERAL_NOTIFICATION_BOSSBAR_STYLE);

        List<BarFlag> barFlags = new ArrayList<>();
        for (String barFlagString : plugin.getConfiguration().GENERAL_NOTIFICATION_BOSSBAR_FLAGS) {
            barFlags.add(BarFlag.valueOf(barFlagString));
        }

        return Bukkit.createBossBar("", barColor, barStyle, barFlags.toArray(new BarFlag[0]));
    }

    public void setupBossBarUtils() {
        if (plugin.getConfiguration().GENERAL_NOTIFICATION_TYPE.toUpperCase().equals("BOSSBAR")) {
            bossBarDuration = plugin.getConfiguration().GENERAL_NOTIFICATION_BOSSBAR_DURATION * 50; // ticks * 50 = milliseconds
        }
    }
}
