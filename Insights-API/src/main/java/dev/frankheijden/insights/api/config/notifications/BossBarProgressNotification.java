package dev.frankheijden.insights.api.config.notifications;

import dev.frankheijden.insights.api.InsightsPlugin;
import org.bukkit.boss.BossBar;

public class BossBarProgressNotification extends BossBarNotification implements ProgressNotification {

    protected BossBarProgressNotification(InsightsPlugin plugin, BossBar bossBar, String content, int ticks) {
        super(plugin, bossBar, content, ticks);
    }

    @Override
    public BossBarProgressNotification progress(double progress) {
        bossBar.setProgress(Math.max(0, Math.min(1, progress)));
        return this;
    }
}
