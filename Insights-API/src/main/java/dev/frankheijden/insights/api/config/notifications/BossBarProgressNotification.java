package dev.frankheijden.insights.api.config.notifications;

import dev.frankheijden.insights.api.InsightsPlugin;
import dev.frankheijden.insights.api.config.Messages;
import net.kyori.adventure.bossbar.BossBar;

public class BossBarProgressNotification extends BossBarNotification implements ProgressNotification {

    protected BossBarProgressNotification(InsightsPlugin plugin, BossBar bossBar, Messages.Message content, int ticks) {
        super(plugin, bossBar, content, ticks);
    }

    @Override
    public BossBarProgressNotification progress(float progress) {
        bossBar.progress(Math.max(0, Math.min(1, progress)));
        return this;
    }
}
