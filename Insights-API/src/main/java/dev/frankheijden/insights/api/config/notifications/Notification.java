package dev.frankheijden.insights.api.config.notifications;

import org.bukkit.entity.Player;

public interface Notification {

    Notification add(Player player);

    SendableNotification create();

    void clear();

}
