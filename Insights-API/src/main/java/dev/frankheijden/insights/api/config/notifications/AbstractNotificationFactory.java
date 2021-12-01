package dev.frankheijden.insights.api.config.notifications;

import dev.frankheijden.insights.api.InsightsPlugin;
import dev.frankheijden.insights.api.config.Messages;
import dev.frankheijden.insights.api.config.Settings;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;

public abstract class AbstractNotificationFactory<T extends Notification> {

    protected final InsightsPlugin plugin;
    protected final Settings settings;

    protected AbstractNotificationFactory(InsightsPlugin plugin) {
        this.plugin = plugin;
        this.settings = plugin.getSettings();
    }

    protected BossBar createBossBar(Messages.Message title) {
        return BossBar.bossBar(
                title.toComponent().orElse(Component.empty()),
                0,
                settings.NOTIFICATION_BOSSBAR_COLOR,
                settings.NOTIFICATION_BOSSBAR_OVERLAY,
                settings.NOTIFICATION_BOSSBAR_FLAGS
        );
    }

    public abstract T bossBar(Messages.Message title);

    public abstract T actionBar(Messages.Message content);

    public abstract T empty();
}
