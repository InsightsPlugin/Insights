package net.frankheijden.insights.tasks;

import com.google.gson.*;
import net.frankheijden.insights.Insights;
import net.frankheijden.insights.utils.MessageUtils;
import net.frankheijden.insights.utils.StringUtils;
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

    private static final Insights plugin = Insights.getInstance();
    private final Player player;
    private final String currentVersion;
    private boolean downloading;
    private boolean downloaded;

    private static final String GITHUB_INSIGHTS_LINK = "https://api.github.com/repos/FrankHeijden/Insights/releases/latest";

    public UpdateCheckerTask(Player player) {
        this.player = player;
        this.currentVersion = plugin.getDescription().getVersion();
        this.downloading = false;
        this.downloaded = false;
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
            downloadLink = assets.get(0)
                    .getAsJsonObject()
                    .getAsJsonPrimitive("browser_download_url")
                    .getAsString();
        }
        if (StringUtils.isNewVersion(currentVersion, githubVersion)) {
            if (plugin.getConfiguration().GENERAL_UPDATES_DOWNLOAD) {
                MessageUtils.sendMessage(player, "messages.update.downloading",
                        "%old%", currentVersion,
                        "%new%", githubVersion,
                        "%info%", body);
                downloadPlugin(githubVersion, downloadLink);
            } else {
                MessageUtils.sendMessage(player, "messages.update.available",
                        "%old%", currentVersion,
                        "%new%", githubVersion,
                        "%info%", body);
            }
        }
    }

    private void downloadPlugin(String githubVersion, String downloadLink) {
        if (downloading || (downloaded && currentVersion.equals(githubVersion))) {
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
    }

    private void status(String githubVersion, boolean isError) {
        final String path = "messages.update.download_" + (isError ? "failed" : "success");
        Bukkit.getOnlinePlayers().forEach((p) -> {
            if (p.hasPermission("insights.notification.update")) {
                MessageUtils.sendMessage(player, path, "%new%", githubVersion);
            }
        });
    }

    private File getPluginFile() {
        try {
            Method method = JavaPlugin.class.getDeclaredMethod("getFile");
            method.setAccessible(true);
            return (File) method.invoke(plugin);
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
