package net.frankheijden.insights.managers;

import net.frankheijden.insights.Insights;
import net.frankheijden.insights.entities.BossBarTime;
import net.frankheijden.insights.tasks.BossBarTask;
import org.bukkit.Bukkit;
import org.bukkit.boss.*;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class BossBarManager {

    private static final Insights plugin = Insights.getInstance();
    private static BossBarManager instance = null;

    private final BossBarTask task;
    private final Map<UUID, BossBarTime> dataMap;
    private final Map<UUID, BossBar> persistentMap;
    private final int BOSSBAR_DURATION_MILLIS;

    public BossBarManager() {
        instance = this;
        this.task = new BossBarTask();
        this.dataMap = new ConcurrentHashMap<>();
        this.persistentMap = new ConcurrentHashMap<>();
        this.BOSSBAR_DURATION_MILLIS = plugin.getConfiguration().GENERAL_NOTIFICATION_BOSSBAR_DURATION * 50;
    }

    public static BossBarManager getInstance() {
        return instance;
    }

    public void start() {
        task.start();
    }

    public void stop() {
        task.stop();
    }

    public void displayPersistentBossBar(Player player, String text, double progress) {
        BossBar bossBar = persistentMap.get(player.getUniqueId());
        if (bossBar == null) {
            bossBar = createBossBar();
            bossBar.addPlayer(player);
            bossBar.setVisible(true);
        }

        bossBar.setTitle(text);
        bossBar.setProgress(progress);
        persistentMap.put(player.getUniqueId(), bossBar);
    }

    public void displayBossBar(Player player, String text, double progress) {
        long endTime = System.currentTimeMillis() + BOSSBAR_DURATION_MILLIS;

        BossBarTime barTime = dataMap.get(player.getUniqueId());
        if (barTime == null) {
            BossBar bossBar = createBossBar();
            bossBar.addPlayer(player);
            bossBar.setVisible(true);
            barTime = new BossBarTime(bossBar, endTime);
        }

        BossBar bossBar = barTime.getBossBar();
        bossBar.setTitle(text);
        bossBar.setProgress(progress);

        barTime.setEndTime(endTime);
        dataMap.put(player.getUniqueId(), barTime);
    }

    public void removeExpiredBossBars() {
        List<UUID> remove = new ArrayList<>();
        dataMap.forEach(((uuid, bossBarTime) -> {
            if (System.currentTimeMillis() >= bossBarTime.getEndTime()) {
                bossBarTime.getBossBar().setVisible(false);
                remove.add(uuid);
            }
        }));

        remove.forEach(dataMap::remove);
    }

    public void refreshPersistentBossBar(Player player) {
        BossBar bossBar = persistentMap.get(player.getUniqueId());
        if (bossBar != null) {
            bossBar.removeAll();
            bossBar.addPlayer(player);
        }
    }

    public void removePersistentBossBar(UUID uuid) {
        BossBar bossBar = persistentMap.get(uuid);
        if (bossBar == null) return;
        bossBar.setVisible(false);
        bossBar.removeAll();
        persistentMap.remove(uuid);
    }

    public static BossBar createBossBar() {
        BarColor barColor = BarColor.valueOf(plugin.getConfiguration().GENERAL_NOTIFICATION_BOSSBAR_COLOR);
        BarStyle barStyle = BarStyle.valueOf(plugin.getConfiguration().GENERAL_NOTIFICATION_BOSSBAR_STYLE);

        List<BarFlag> barFlags = new ArrayList<>();
        for (String barFlagString : plugin.getConfiguration().GENERAL_NOTIFICATION_BOSSBAR_FLAGS) {
            barFlags.add(BarFlag.valueOf(barFlagString));
        }

        return Bukkit.createBossBar("", barColor, barStyle, barFlags.toArray(new BarFlag[0]));
    }
}
