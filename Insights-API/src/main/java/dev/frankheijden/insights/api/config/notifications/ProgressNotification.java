package dev.frankheijden.insights.api.config.notifications;

public interface ProgressNotification extends Notification {

    ProgressNotification progress(float progress);

}
