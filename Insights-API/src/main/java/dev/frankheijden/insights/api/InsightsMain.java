package dev.frankheijden.insights.api;

import dev.frankheijden.insights.api.concurrent.ChunkContainerExecutor;
import dev.frankheijden.insights.api.concurrent.ContainerExecutor;
import dev.frankheijden.insights.api.concurrent.PlayerList;
import dev.frankheijden.insights.api.concurrent.storage.WorldDistributionStorage;
import dev.frankheijden.insights.api.concurrent.tracker.WorldChunkScanTracker;
import dev.frankheijden.insights.api.config.Settings;

public interface InsightsMain {

    void reloadSettings();

    Settings getSettings();

    ContainerExecutor getExecutor();

    ChunkContainerExecutor getChunkContainerExecutor();

    PlayerList getPlayerList();

    WorldDistributionStorage getWorldDistributionStorage();

    WorldChunkScanTracker getWorldChunkScanTracker();
}
