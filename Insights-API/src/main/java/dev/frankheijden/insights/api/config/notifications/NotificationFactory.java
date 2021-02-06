package dev.frankheijden.insights.api.config.notifications;

import dev.frankheijden.insights.api.InsightsPlugin;

public class NotificationFactory extends AbstractNotificationFactory<Notification> {

    public NotificationFactory(InsightsPlugin plugin) {
        super(plugin);
    }

    @Override
    public Notification bossBar(String title) {
        return new BossBarNotification(
                plugin,
                createBossBar(title),
                title,
                settings.NOTIFICATION_BOSSBAR_DURATION_TICKS
        );
    }

    @Override
    public Notification actionBar(String content) {
        return new ActionBarNotification(content);
    }

    @Override
    public Notification empty() {
        return EmptyNotification.get();
    }
}
