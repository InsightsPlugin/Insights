package dev.frankheijden.insights.api;

import dev.frankheijden.insights.api.addons.AddonManager;
import dev.frankheijden.insights.api.concurrent.ChunkContainerExecutor;
import dev.frankheijden.insights.api.concurrent.ContainerExecutor;
import dev.frankheijden.insights.api.concurrent.PlayerList;
import dev.frankheijden.insights.api.concurrent.storage.AddonStorage;
import dev.frankheijden.insights.api.concurrent.storage.WorldStorage;
import dev.frankheijden.insights.api.concurrent.tracker.AddonScanTracker;
import dev.frankheijden.insights.api.concurrent.tracker.WorldChunkScanTracker;
import dev.frankheijden.insights.api.config.Limits;
import dev.frankheijden.insights.api.config.Messages;
import dev.frankheijden.insights.api.config.Notifications;
import dev.frankheijden.insights.api.config.Settings;
import dev.frankheijden.insights.api.metrics.MetricsManager;
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

    AddonManager getAddonManager();

    ContainerExecutor getExecutor();

    ChunkContainerExecutor getChunkContainerExecutor();

    PlayerList getPlayerList();

    WorldStorage getWorldStorage();

    AddonStorage getAddonStorage();

    WorldChunkScanTracker getWorldChunkScanTracker();

    AddonScanTracker getAddonScanTracker();

    MetricsManager getMetricsManager();

    Map<String, Class<?>> getAllowedDisableEvents();
}
