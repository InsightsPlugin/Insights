package dev.frankheijden.insights.api.config.notifications;

import dev.frankheijden.insights.api.InsightsPlugin;
import org.bukkit.Bukkit;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import java.util.LinkedList;
import java.util.Queue;

public class BossBarNotification implements Notification {

    protected final InsightsPlugin plugin;
    protected final BossBar bossBar;
    protected final String content;
    protected final Queue<Player> receivers = new LinkedList<>();
    protected final int ticks;
    protected BukkitTask task;

    protected BossBarNotification(InsightsPlugin plugin, BossBar bossBar, String content, int ticks) {
        this.plugin = plugin;
        this.bossBar = bossBar;
        this.content = content;
        this.ticks = ticks;
    }

    @Override
    public BossBarNotification add(Player player) {
        receivers.add(player);
        return this;
    }

    @Override
    public SendableNotification create() {
        return new SendableNotification(content) {
            @Override
            public void send() {
                if (task != null) {
                    task.cancel();
                }
                bossBar.setTitle(content);
                bossBar.setVisible(true);
                while (!receivers.isEmpty()) {
                    bossBar.addPlayer(receivers.poll());
                }
                task = Bukkit.getScheduler().runTaskLater(plugin, () -> bossBar.setVisible(false), ticks);
            }
        };
    }

    @Override
    public void clear() {
        bossBar.removeAll();
        bossBar.setVisible(false);
        if (task != null) {
            task.cancel();
        }
    }
}
