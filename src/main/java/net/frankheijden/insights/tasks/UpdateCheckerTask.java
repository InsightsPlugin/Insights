package net.frankheijden.insights.tasks;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.frankheijden.insights.Insights;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.lang.reflect.Method;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.StandardCharsets;

public class UpdateCheckerTask implements Runnable {
    private Insights plugin;
    private Player player;
    private String currentVersion;
    private boolean downloading;
    private boolean downloaded;
    private boolean error;

    private static String GITHUB_INSIGHTS_LINK = "https://api.github.com/repos/FrankHeijden/Insights/releases/latest";

    public UpdateCheckerTask(Insights plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        this.currentVersion = plugin.getDescription().getVersion();
        this.downloading = false;
        this.downloaded = false;
        this.error = false;
    }

    @Override
    public void run() {
        JsonObject jsonObject;
        try {
            jsonObject = readJsonFromURL(GITHUB_INSIGHTS_LINK).getAsJsonObject();
        } catch (IOException ex) {
            throw new RuntimeException("Error downloading a new version of Insights", ex);
        }
        String githubVersion = jsonObject.getAsJsonPrimitive("tag_name").getAsString();
        githubVersion = githubVersion.replace("v", "");
        String body = jsonObject.getAsJsonPrimitive("body").getAsString();

        JsonArray assets = jsonObject.getAsJsonArray("assets");
        String downloadLink = null;
        if (assets != null && assets.size() > 0) {
            downloadLink = assets.get(0).getAsJsonObject().getAsJsonPrimitive("browser_download_url").getAsString();
        }
        if (isNewVersion(githubVersion)) {
            if (plugin.getConfiguration().GENERAL_UPDATES_DOWNLOAD) {
                plugin.getUtils().sendMessage(player, "messages.update.downloading", "%old%", currentVersion, "%new%", githubVersion, "%info%", body);
                if (downloading || (downloaded && currentVersion.equals(githubVersion) && !error)) {
                    return;
                }
                downloaded = false;

                if (downloadLink == null) {
                    status(githubVersion, true);
                    return;
                }
                downloading = true;

                try {
                    download(downloadLink, getPluginFile());
                } catch (IOException ex) {
                    status(githubVersion, true);
                    throw new RuntimeException("Error downloading a new version of Insights", ex);
                }

                status(githubVersion, false);
                downloading = false;
                downloaded = true;
            } else {
                plugin.getUtils().sendMessage(player, "messages.update.available", "%old%", currentVersion, "%new%", githubVersion, "%info%", body);
            }
        }
    }

    private void status(String githubVersion, boolean isError) {
        final String path = "messages.update.download_" + (isError ? "failed" : "success");
        Bukkit.getOnlinePlayers().forEach((p) -> {
            if (p.hasPermission("insights.notification.update")) {
                plugin.getUtils().sendMessage(player, path, "%new%", githubVersion);
            }
        });
    }

    private File getPluginFile() {
        try {
            Method method = JavaPlugin.class.getDeclaredMethod("getFile");
            method.setAccessible(true);
            return (File) method.invoke(this.plugin);
        } catch (ReflectiveOperationException ex) {
            throw new RuntimeException("Error retrieving current plugin file", ex);
        }
    }

    private void download(String urlString, File target) throws IOException {
        URL url = new URL(urlString);
        ReadableByteChannel rbc = Channels.newChannel(url.openStream());
        FileOutputStream fos = new FileOutputStream(target);
        fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
    }

    private boolean isNewVersion(String version) {
        String[] currentVersion = this.currentVersion.split("\\.");
        String[] newVersion = version.split("\\.");

        int i = 0;
        while (i < currentVersion.length && i < newVersion.length) {
            if (Integer.parseInt(newVersion[i]) > Integer.parseInt(currentVersion[i])) return true;
            i++;
        }
        return false;
    }

    private String readAll(BufferedReader reader) throws IOException {
        StringBuilder sb = new StringBuilder();
        int cp;
        while ((cp = reader.read()) != -1) {
            sb.append((char) cp);
        }
        return sb.toString();
    }

    private JsonElement readJsonFromURL(String url) throws IOException {
        try (InputStream is = new URL(url).openStream()) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
            String jsonText = readAll(reader);
            return new JsonParser().parse(jsonText);
        }
    }
}
