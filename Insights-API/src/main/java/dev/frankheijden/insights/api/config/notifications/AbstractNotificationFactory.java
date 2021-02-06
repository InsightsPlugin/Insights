package dev.frankheijden.insights.api.config.notifications;

import dev.frankheijden.insights.api.InsightsPlugin;
import dev.frankheijden.insights.api.config.Settings;
import org.bukkit.Bukkit;
import org.bukkit.boss.BossBar;

public abstract class AbstractNotificationFactory<T extends Notification> {

    protected final InsightsPlugin plugin;
    protected final Settings settings;

    protected AbstractNotificationFactory(InsightsPlugin plugin) {
        this.plugin = plugin;
        this.settings = plugin.getSettings();
    }

    protected BossBar createBossBar(String title) {
        return Bukkit.createBossBar(
                title,
                settings.NOTIFICATION_BOSSBAR_COLOR,
                settings.NOTIFICATION_BOSSBAR_STYLE,
                settings.NOTIFICATION_BOSSBAR_FLAGS
        );
    }

    public abstract T bossBar(String title);

    public abstract T actionBar(String content);

    public abstract T empty();
}
