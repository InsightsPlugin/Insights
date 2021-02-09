package dev.frankheijden.insights.api.config.notifications;

import org.bukkit.entity.Player;

public class EmptyNotification implements Notification {

    private static EmptyNotification instance = null;

    /**
     * Static getter for an empty Notification.
     */
    public static EmptyNotification get() {
        if (instance == null) {
            instance = new EmptyNotification();
        }
        return instance;
    }

    protected EmptyNotification() {}

    @Override
    public EmptyNotification add(Player player) {
        return this;
    }

    @Override
    public SendableNotification create() {
        return new SendableNotification(null) {
            @Override
            public void send() {
                // Nothing to send.
            }
        };
    }

    @Override
    public void clear() {
        // Nothing to clear
    }
}
