package dev.frankheijden.insights.api.config.notifications;

import dev.frankheijden.insights.api.InsightsPlugin;
import dev.frankheijden.insights.api.config.Messages;

public class ProgressNotificationFactory extends AbstractNotificationFactory<ProgressNotification> {

    public ProgressNotificationFactory(InsightsPlugin plugin) {
        super(plugin);
    }

    @Override
    public ProgressNotification bossBar(Messages.Message title) {
        return new BossBarProgressNotification(
                plugin,
                createBossBar(title),
                title,
                settings.NOTIFICATION_BOSSBAR_DURATION_TICKS
        );
    }

    @Override
    public ProgressNotification actionBar(Messages.Message content) {
        return new ActionBarProgressNotification(
                plugin,
                content,
                settings.NOTIFICATION_ACTIONBAR_SEGMENTS,
                settings.NOTIFICATION_ACTIONBAR_DONE_COLOR,
                settings.NOTIFICATION_ACTIONBAR_TOTAL_COLOR,
                settings.NOTIFICATION_ACTIONBAR_SEQUENCE,
                settings.NOTIFICATION_ACTIONBAR_SEPARATOR
        );
    }

    @Override
    public ProgressNotification empty() {
        return EmptyProgressNotification.get();
    }
}
