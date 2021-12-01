package dev.frankheijden.insights.api.config.notifications;

import dev.frankheijden.insights.api.InsightsPlugin;
import dev.frankheijden.insights.api.config.Messages;

public class NotificationFactory extends AbstractNotificationFactory<Notification> {

    public NotificationFactory(InsightsPlugin plugin) {
        super(plugin);
    }

    @Override
    public Notification bossBar(Messages.Message title) {
        return new BossBarNotification(
                plugin,
                createBossBar(title),
                title,
                settings.NOTIFICATION_BOSSBAR_DURATION_TICKS
        );
    }

    @Override
    public Notification actionBar(Messages.Message content) {
        return new ActionBarNotification(plugin, content);
    }

    @Override
    public Notification empty() {
        return EmptyNotification.get();
    }
}
