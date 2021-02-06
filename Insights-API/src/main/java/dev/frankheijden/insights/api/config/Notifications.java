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
    private final Map<UUID, Cache<Notification>> notificationMap = new HashMap<>();
    private final Map<UUID, Cache<ProgressNotification>> progressNotificationMap = new HashMap<>();

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

    /**
     * Retrieves the cached Notification, if present, or create a new one if it isn't present.
     */
    public Notification getCached(UUID uuid, Messages.Key messageKey) {
        return notificationMap.compute(uuid, (uuid1, cache) -> {
            if (cache == null || cache.getKey() != messageKey) {
                return new Cache<>(get(messageKey), messageKey);
            }
            return cache;
        }).getNotification();
    }

    /**
     * Retrieves the cached ProgressNotification, if present, or create a new one if it isn't present.
     */
    public ProgressNotification getCachedProgress(UUID uuid, Messages.Key messageKey) {
        return progressNotificationMap.compute(uuid, (uuid1, cache) -> {
            if (cache == null || cache.getKey() != messageKey) {
                return new Cache<>(getProgress(messageKey), messageKey);
            }
            return cache;
        }).getNotification();
    }

    public ProgressNotification getProgress(Messages.Key messageKey) {
        return get(progressNotificationFactory, messageKey);
    }

    private static final class Cache<T extends Notification> {

        private final T notification;
        private final Messages.Key key;

        private Cache(T notification, Messages.Key key) {
            this.notification = notification;
            this.key = key;
        }

        public T getNotification() {
            return notification;
        }

        public Messages.Key getKey() {
            return key;
        }
    }
}
