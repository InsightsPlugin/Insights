package net.frankheijden.insights.tasks;

import net.frankheijden.insights.Insights;
import org.bukkit.entity.Player;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

public class UpdateCheckerTask implements Runnable {
    private Insights plugin;
    private Player player;

    public UpdateCheckerTask(Insights plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
    }

    @Override
    public void run() {
        try {
            URL url = new URL("https://api.spigotmc.org/legacy/update.php?resource=56489");
            URLConnection urlConnection = url.openConnection();
            String versionAvailable = new BufferedReader(new InputStreamReader(urlConnection.getInputStream())).readLine();
            String versionInstalled = plugin.getDescription().getVersion();
            if (!versionInstalled.equals(versionAvailable)) {
                plugin.getUtils().sendMessage(player, "messages.update_available", "%old%", versionInstalled, "%new%", versionAvailable);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
