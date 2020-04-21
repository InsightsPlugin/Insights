package net.frankheijden.insights.managers;

import net.frankheijden.insights.Insights;

public class VersionManager {

    private static VersionManager instance;
    private final Insights plugin = Insights.getInstance();
    private final String currentVersion;
    private String downloadedVersion;

    public VersionManager() {
        instance = this;
        this.currentVersion = plugin.getDescription().getVersion();
        this.downloadedVersion = currentVersion;
    }

    public static VersionManager getInstance() {
        return instance;
    }

    public String getCurrentVersion() {
        return currentVersion;
    }

    public String getDownloadedVersion() {
        return downloadedVersion;
    }

    public boolean hasDownloaded() {
        return !downloadedVersion.equals(currentVersion);
    }

    public boolean isDownloadedVersion(String version) {
        return downloadedVersion.equals(version);
    }

    public void setDownloadedVersion(String downloadedVersion) {
        this.downloadedVersion = downloadedVersion;
    }
}
