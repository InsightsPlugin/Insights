package dev.frankheijden.insights.api.config.notifications;

import dev.frankheijden.insights.api.utils.StringUtils;
import org.bukkit.ChatColor;

public abstract class SendableNotification {

    protected String content;

    protected SendableNotification(String content) {
        this.content = content;
    }

    public SendableNotification replace(String... replacements) {
        if (content != null) content = StringUtils.replace(content, replacements);
        return this;
    }

    public SendableNotification color() {
        if (content != null) content = ChatColor.translateAlternateColorCodes('&', content);
        return this;
    }

    public abstract void send();
}
