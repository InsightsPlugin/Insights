package dev.frankheijden.insights.api;

import dev.frankheijden.insights.api.concurrent.ChunkContainerExecutor;
import dev.frankheijden.insights.api.concurrent.ContainerExecutor;
import dev.frankheijden.insights.api.concurrent.PlayerList;
import dev.frankheijden.insights.api.concurrent.storage.WorldDistributionStorage;
import dev.frankheijden.insights.api.concurrent.tracker.WorldChunkScanTracker;
import dev.frankheijden.insights.api.config.Limits;
import dev.frankheijden.insights.api.config.Messages;
import dev.frankheijden.insights.api.config.Notifications;
import dev.frankheijden.insights.api.config.Settings;
import java.util.Map;

public interface InsightsMain {

    void reloadSettings();

    void reloadMessages();

    void reloadNotifications();

    void reloadLimits();

    Settings getSettings();

    Messages getMessages();

    Notifications getNotifications();

    Limits getLimits();

    ContainerExecutor getExecutor();

    ChunkContainerExecutor getChunkContainerExecutor();

    PlayerList getPlayerList();

    WorldDistributionStorage getWorldDistributionStorage();

    WorldChunkScanTracker getWorldChunkScanTracker();

    Map<String, Class<?>> getAllowedDisableEvents();
}