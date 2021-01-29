package dev.frankheijden.insights.managers;

import dev.frankheijden.insights.Insights;
import dev.frankheijden.insights.entities.Notification;
import dev.frankheijden.insights.tasks.NotifyTask;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class NotificationManager {

    private static final Insights plugin = Insights.getInstance();
    private static NotificationManager instance = null;

    private final NotifyTask task;
    private static final Map<UUID, Notification> notifications = new ConcurrentHashMap<>();
    private static final Map<UUID, BossBar> persistentBossBars = new ConcurrentHashMap<>();
    private final int BOSSBAR_DURATION_MILLIS;

    public NotificationManager() {
        instance = this;
        this.task = new NotifyTask();
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
        notifications.values().forEach(n -> removeBossBar(n.getBossBar()));
        notifications.clear();
        persistentBossBars.values().forEach(NotificationManager::removeBossBar);
        persistentBossBars.clear();
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
                removeBossBar(notification.getBossBar());
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
        removeBossBar(bossBar);
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

    public static void removeBossBar(BossBar bossBar) {
        bossBar.setVisible(false);
        bossBar.removeAll();
    }
}
