package net.frankheijden.insights.tasks;

import net.frankheijden.insights.Insights;
import org.bukkit.entity.Player;
import org.inventivetalent.update.spiget.SpigetUpdate;
import org.inventivetalent.update.spiget.UpdateCallback;

public class UpdateCheckerTask implements Runnable {
    private Insights plugin;
    private Player player;

    public UpdateCheckerTask(Insights plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
    }

    @Override
    public void run() {
        SpigetUpdate spigetUpdate = new SpigetUpdate(plugin, 56489);
        spigetUpdate.checkForUpdate(new UpdateCallback() {
            @Override
            public void updateAvailable(String versionAvailable, String downloadUrl, boolean hasDirectDownload) {
                String versionInstalled = plugin.getDescription().getVersion();
                if (plugin.getConfiguration().GENERAL_UPDATES_DOWNLOAD) {
                    if (plugin.getVersionQueued() != null) {
                        if (plugin.getVersionQueued().equalsIgnoreCase(versionAvailable)) {
                            plugin.getUtils().sendMessage(player, "messages.update.downloaded", "%old%", versionInstalled, "%new%", versionAvailable);
                            return;
                        }
                    }

                    if (plugin.isDownloading()) {
                        plugin.addNotifyPlayer(player);
                        return;
                    }

                    String path = null;
                    if (hasDirectDownload) {
                        plugin.setDownloading(true);
                        if (spigetUpdate.downloadUpdate()) {
                            path = "messages.update.downloaded";
                            plugin.setVersionQueued(versionAvailable);
                        }
                    }

                    if (path == null) path = "messages.update.download_failed";

                    final String path_ = path;
                    plugin.getUtils().sendMessage(player, path, "%old%", versionInstalled, "%new%", versionAvailable);
                    plugin.getNotifyPlayers().forEach((p) -> plugin.getUtils().sendMessage(player, path_, "%old%", versionInstalled, "%new%", versionAvailable));
                    plugin.clearNotifyPlayers();
                    plugin.setDownloading(false);
                } else {
                    plugin.getUtils().sendMessage(player, "messages.update.available", "%old%", versionInstalled, "%new%", versionAvailable);
                }
            }

            @Override
            public void upToDate() {
                //
            }
        });
    }
}
