package net.frankheijden.insights.managers;

import net.frankheijden.insights.Insights;
import net.frankheijden.insights.entities.*;
import net.frankheijden.insights.tasks.NotifyTask;
import org.bukkit.Bukkit;
import org.bukkit.boss.*;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class NotificationManager {

    private static final Insights plugin = Insights.getInstance();
    private static NotificationManager instance = null;

    private final NotifyTask task;
    private final Map<UUID, Notification> notifications;
    private final Map<UUID, BossBar> persistentBossBars;
    private final int BOSSBAR_DURATION_MILLIS;

    public NotificationManager() {
        instance = this;
        this.task = new NotifyTask();
        this.notifications = new ConcurrentHashMap<>();
        this.persistentBossBars = new ConcurrentHashMap<>();
        this.BOSSBAR_DURATION_MILLIS = plugin.getConfiguration().GENERAL_NOTIFICATION_BOSSBAR_DURATION * 50;
    }

    public static NotificationManager getInstance() {
        return instance;
    }

    public void start() {
        task.start();
    }

    public void stop() {
        task.stop();
    }

    public void displayPersistentBossBar(Player player, String text, double progress) {
        BossBar bossBar = persistentBossBars.get(player.getUniqueId());
        if (bossBar == null) {
            bossBar = createBossBar();
            bossBar.addPlayer(player);
            bossBar.setVisible(true);
        }

        bossBar.setTitle(text);
        bossBar.setProgress(progress);
        persistentBossBars.put(player.getUniqueId(), bossBar);
    }

    public void displayBossBar(Player player, String text, double progress) {
        long endTime = System.currentTimeMillis() + BOSSBAR_DURATION_MILLIS;

        Notification notification = notifications.get(player.getUniqueId());
        if (notification == null) {
            BossBar bossBar = createBossBar();
            bossBar.addPlayer(player);
            bossBar.setVisible(true);
            notification = new Notification(bossBar, endTime);
        }

        BossBar bossBar = notification.getBossBar();
        bossBar.setTitle(text);
        bossBar.setProgress(progress);

        notification.setEndTime(endTime);
        notifications.put(player.getUniqueId(), notification);
    }

    public void removeExpired() {
        List<UUID> remove = new ArrayList<>();
        notifications.forEach(((uuid, notification) -> {
            if (System.currentTimeMillis() >= notification.getEndTime()) {
                notification.getBossBar().setVisible(false);
                remove.add(uuid);
            }
        }));

        remove.forEach(notifications::remove);
    }

    public void refreshPersistent(Player player) {
        BossBar bossBar = persistentBossBars.get(player.getUniqueId());
        if (bossBar != null) {
            bossBar.removeAll();
            bossBar.addPlayer(player);
        }
    }

    public void removePersistent(UUID uuid) {
        BossBar bossBar = persistentBossBars.get(uuid);
        if (bossBar == null) return;
        bossBar.setVisible(false);
        bossBar.removeAll();
        persistentBossBars.remove(uuid);
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
