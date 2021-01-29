package dev.frankheijden.insights.tasks;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.frankheijden.insights.managers.VersionManager;
import dev.frankheijden.insights.Insights;
import dev.frankheijden.insights.utils.MessageUtils;
import dev.frankheijden.insights.utils.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.InvalidDescriptionException;
import org.bukkit.plugin.InvalidPluginException;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.net.ConnectException;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public class UpdateCheckerTask implements Runnable {

    private static final Insights plugin = Insights.getInstance();
    private static final VersionManager versionManager = VersionManager.getInstance();
    private final CommandSender sender;
    private final String currentVersion;
    private final boolean startup;
    private final Map<String, UpdateCache> cacheMap = new HashMap<>();

    private static final String GITHUB_INSIGHTS_LINK = "https://api.github.com/repos/InsightsPlugin/Insights/releases/latest";

    private UpdateCheckerTask(CommandSender sender, boolean startup) {
        this.sender = sender;
        this.currentVersion = plugin.getDescription().getVersion();
        this.startup = startup;
    }

    public static void start(CommandSender sender) {
        start(sender, false);
    }

    public static void start(CommandSender sender, boolean startup) {
        UpdateCheckerTask task = new UpdateCheckerTask(sender, startup);
        Bukkit.getScheduler().runTaskAsynchronously(plugin, task);
    }

    public boolean isStartupCheck() {
        return this.startup;
    }

    @Override
    public void run() {
        if (isStartupCheck()) {
            sender.sendMessage("[Insights] Checking for updates...");
        }

        JsonObject jsonObject;
        try {
            jsonObject = getCachedJson(GITHUB_INSIGHTS_LINK).getAsJsonObject();
        } catch (ConnectException | UnknownHostException ex) {
            Insights.logger.severe(String.format("Error fetching new version of Insights: (%s) %s (maybe check your connection?)",
                    ex.getClass().getSimpleName(), ex.getMessage()));
            return;
        } catch (IOException ex) {
            Insights.logger.log(Level.SEVERE, ex, () -> "Error fetching new version of Insights");
            return;
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
            if (isStartupCheck()) {
                sender.sendMessage("[Insights] Insights " + githubVersion + " is available!");
                sender.sendMessage("[Insights] Release info: " + body);
            }
            if (canDownloadPlugin()) {
                if (isStartupCheck()) {
                    sender.sendMessage("[Insights] Started downloading from \"" + downloadLink + "\"...");
                } else {
                    MessageUtils.sendMessage(sender, "messages.update.downloading",
                            "%old%", currentVersion,
                            "%new%", githubVersion,
                            "%info%", body);
                }
                downloadPlugin(githubVersion, downloadLink);
            } else if (!isStartupCheck()) {
                MessageUtils.sendMessage(sender, "messages.update.available",
                        "%old%", currentVersion,
                        "%new%", githubVersion,
                        "%info%", body);
            }
        } else if (versionManager.hasDownloaded()) {
            MessageUtils.sendMessage(sender, "messages.update.download_success",
                    "%new%", versionManager.getDownloadedVersion());
        } else if (isStartupCheck()) {
            sender.sendMessage("[Insights] We are up-to-date!");
        }
    }

    private boolean canDownloadPlugin() {
        if (isStartupCheck()) return plugin.getConfiguration().GENERAL_UPDATES_DOWNLOAD_STARTUP;
        return plugin.getConfiguration().GENERAL_UPDATES_DOWNLOAD;
    }

    private void downloadPlugin(String githubVersion, String downloadLink) {
        if (versionManager.isDownloadedVersion(githubVersion)) {
            broadcastDownloadStatus(githubVersion, false);
            return;
        }

        if (downloadLink == null) {
            broadcastDownloadStatus(githubVersion, true);
            return;
        }

        try {
            download(downloadLink, getPluginFile());
        } catch (IOException ex) {
            broadcastDownloadStatus(githubVersion, true);
            throw new RuntimeException("Error downloading a new version of Insights", ex);
        }

        if (isStartupCheck()) {
            Insights.logger.info(String.format("Downloaded Insights version v%s. Restarting plugin now...", githubVersion));
            Bukkit.getPluginManager().disablePlugin(plugin);
            try {
                Bukkit.getPluginManager().enablePlugin(Bukkit.getPluginManager().loadPlugin(getPluginFile()));
            } catch (InvalidPluginException | InvalidDescriptionException ex) {
                ex.printStackTrace();
                return;
            }
            Insights.logger.info(String.format("Successfully upgraded Insights to v%s!", githubVersion));
        } else {
            versionManager.setDownloadedVersion(githubVersion);
            broadcastDownloadStatus(githubVersion, false);
        }
    }

    private void broadcastDownloadStatus(String githubVersion, boolean isError) {
        final String path = "messages.update.download_" + (isError ? "failed" : "success");
        Bukkit.getOnlinePlayers().forEach((p) -> {
            if (p.hasPermission("insights.notification.update")) {
                MessageUtils.sendMessage(sender, path, "%new%", githubVersion);
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
        try (InputStream is = stream(urlString);
             ReadableByteChannel rbc = Channels.newChannel(is);
             FileOutputStream fos = new FileOutputStream(target)) {
            fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
        }
    }

    private String readAll(BufferedReader reader) throws IOException {
        StringBuilder sb = new StringBuilder();
        int cp;
        while ((cp = reader.read()) != -1) {
            sb.append((char) cp);
        }
        return sb.toString();
    }

    private JsonElement getCachedJson(String url) throws IOException {
        UpdateCache cache = cacheMap.get(url);
        if (cache == null || !cache.isAlive()) {
            cache = new UpdateCache(readJsonFromURL(url));
            cacheMap.put(url, cache);
        }
        return cache.getElement();
    }

    private JsonElement readJsonFromURL(String url) throws IOException {
        try (InputStream is = stream(url)) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
            String jsonText = readAll(reader);
            return new JsonParser().parse(jsonText);
        }
    }

    private InputStream stream(String url) throws IOException {
        URLConnection conn = new URL(url).openConnection();
        conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10.15; rv:77.0) Gecko/20100101 Firefox/77.0");
        conn.setConnectTimeout(10000);
        return conn.getInputStream();
    }

    public static final class UpdateCache {

        public static final long CACHE_TTL_MILLIS = 30 * 60 * 1000L;

        private final JsonElement element;
        private final long timestamp;

        public UpdateCache(JsonElement element) {
            this.element = element;
            this.timestamp = System.currentTimeMillis();
        }

        public JsonElement getElement() {
            return element;
        }

        public boolean isAlive() {
            return System.currentTimeMillis() - timestamp <= CACHE_TTL_MILLIS;
        }
    }
}
