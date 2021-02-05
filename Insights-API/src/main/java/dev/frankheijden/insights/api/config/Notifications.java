package dev.frankheijden.insights.api.config;

import dev.frankheijden.insights.api.InsightsPlugin;
import dev.frankheijden.insights.api.config.notifications.AbstractNotificationFactory;
import dev.frankheijden.insights.api.config.notifications.Notification;
import dev.frankheijden.insights.api.config.notifications.NotificationFactory;
import dev.frankheijden.insights.api.config.notifications.ProgressNotification;
import dev.frankheijden.insights.api.config.notifications.ProgressNotificationFactory;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Notifications {

    private final NotificationFactory notificationFactory;
    private final ProgressNotificationFactory progressNotificationFactory;
    private final Messages messages;
    private final Settings.NotificationType type;
    private final Map<UUID, Notification> notificationMap = new HashMap<>();
    private final Map<UUID, ProgressNotification> progressNotificationMap = new HashMap<>();

    /**
     * Constructs a new Notifications Facade.
     */
    public Notifications(InsightsPlugin plugin) {
        this.notificationFactory = new NotificationFactory(plugin);
        this.progressNotificationFactory = new ProgressNotificationFactory(plugin);
        this.messages = plugin.getMessages();
        this.type = plugin.getSettings().NOTIFICATION_TYPE;
    }

    private <T extends Notification> T createNotification(AbstractNotificationFactory<T> factory, String content) {
        switch (type) {
            case ACTIONBAR: return factory.actionBar(content);
            case BOSSBAR: return factory.bossBar(content);
            default: throw new IllegalArgumentException("Notification Type '" + type + "' is not implemented!");
        }
    }

    /**
     * Creates a new notification from given factory and message locale at given path.
     */
    private <T extends Notification> T get(AbstractNotificationFactory<T> factory, Messages.Key key) {
        return messages.getMessage(key).getMessage()
                .map(content -> createNotification(factory, content))
                .orElse(factory.empty());
    }

    public Notification get(Messages.Key messageKey) {
        return get(notificationFactory, messageKey);
    }

    public Notification getCached(UUID uuid, Messages.Key messageKey) {
        return notificationMap.computeIfAbsent(uuid, k -> get(messageKey));
    }

    public ProgressNotification getCachedProgress(UUID uuid, Messages.Key messageKey) {
        return progressNotificationMap.computeIfAbsent(uuid, k -> getProgress(messageKey));
    }

    public ProgressNotification getProgress(Messages.Key messageKey) {
        return get(progressNotificationFactory, messageKey);
    }
}
