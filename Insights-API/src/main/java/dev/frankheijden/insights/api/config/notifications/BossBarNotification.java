package dev.frankheijden.insights.api.config.notifications;

import dev.frankheijden.insights.api.InsightsPlugin;
import dev.frankheijden.insights.api.config.Messages;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import java.util.LinkedList;
import java.util.Queue;

public class BossBarNotification implements Notification {

    protected final InsightsPlugin plugin;
    protected final BossBar bossBar;
    protected final Messages.Message content;
    protected final Queue<Audience> receivers = new LinkedList<>();
    protected final Queue<Audience> viewers = new LinkedList<>();
    protected final int ticks;
    protected final Runnable bossBarClearer;
    protected BukkitTask task;

    protected BossBarNotification(InsightsPlugin plugin, BossBar bossBar, Messages.Message content, int ticks) {
        this.plugin = plugin;
        this.bossBar = bossBar;
        this.content = content;
        this.ticks = ticks;
        this.bossBarClearer = () -> {
            while (!viewers.isEmpty()) {
                viewers.poll().hideBossBar(bossBar);
            }
        };
    }

    @Override
    public BossBarNotification add(Player player) {
        receivers.add(plugin.messages().getAudiences().player(player));
        return this;
    }

    @Override
    public SendableNotification create() {
        return new SendableNotification(content.resetTemplates()) {
            @Override
            public void send() {
                if (task != null) {
                    task.cancel();
                }
                bossBar.name(content.toComponent().orElse(Component.empty()));

                while (!receivers.isEmpty()) {
                    var audience = receivers.poll();
                    audience.showBossBar(bossBar);
                    viewers.add(audience);
                }
                task = Bukkit.getScheduler().runTaskLater(plugin, bossBarClearer, ticks);
            }
        };
    }

    @Override
    public void clear() {
        bossBarClearer.run();
        if (task != null) {
            task.cancel();
        }
    }
}
