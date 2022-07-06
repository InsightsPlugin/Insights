package dev.frankheijden.insights.api;

import dev.frankheijden.insights.api.addons.AddonManager;
import dev.frankheijden.insights.api.concurrent.ChunkContainerExecutor;
import dev.frankheijden.insights.api.concurrent.ChunkTeleport;
import dev.frankheijden.insights.api.concurrent.ContainerExecutor;
import dev.frankheijden.insights.api.concurrent.PlayerList;
import dev.frankheijden.insights.api.concurrent.count.RedstoneUpdateCount;
import dev.frankheijden.insights.api.concurrent.storage.ScanHistory;
import dev.frankheijden.insights.api.config.Limits;
import dev.frankheijden.insights.api.config.Messages;
import dev.frankheijden.insights.api.config.Notifications;
import dev.frankheijden.insights.api.config.Settings;
import dev.frankheijden.insights.api.listeners.manager.InsightsListenerManager;
import dev.frankheijden.insights.api.metrics.MetricsManager;
import dev.frankheijden.insights.api.region.RegionManager;
import org.checkerframework.checker.nullness.qual.NonNull;

public interface InsightsMain {

    void reloadSettings();

    void reloadMessages();

    void reloadNotifications();

    void reloadLimits();

    @NonNull Settings settings();

    @NonNull Messages messages();

    @NonNull Notifications notifications();

    @NonNull Limits limits();

    @NonNull AddonManager addonManager();

    @NonNull ContainerExecutor executor();

    @NonNull ChunkContainerExecutor chunkContainerExecutor();

    @NonNull PlayerList playerList();

    @NonNull RegionManager regionManager();

    @NonNull MetricsManager metricsManager();

    @NonNull ScanHistory scanHistory();

    @NonNull InsightsListenerManager listenerManager();

    @NonNull RedstoneUpdateCount redstoneUpdateCount();

    @NonNull ChunkTeleport chunkTeleport();

}
