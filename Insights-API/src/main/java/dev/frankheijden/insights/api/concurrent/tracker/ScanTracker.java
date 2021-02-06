package dev.frankheijden.insights.api.concurrent.tracker;

public interface ScanTracker<T> {

    boolean set(T obj, boolean queued);

    boolean isQueued(T obj);

}
