package dev.frankheijden.insights.api.config.notifications;

public class EmptyProgressNotification extends EmptyNotification implements ProgressNotification {

    private static EmptyProgressNotification instance = null;

    /**
     * Static getter for an empty ProgressNotification.
     */
    public static EmptyProgressNotification get() {
        if (instance == null) {
            instance = new EmptyProgressNotification();
        }
        return instance;
    }

    protected EmptyProgressNotification() {}

    @Override
    public EmptyProgressNotification progress(double progress) {
        return this;
    }
}
