package net.frankheijden.insights.tasks;

import com.google.gson.*;
import net.frankheijden.insights.Insights;
import net.frankheijden.insights.managers.VersionManager;
import net.frankheijden.insights.utils.MessageUtils;
import net.frankheijden.insights.utils.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.InvalidDescriptionException;
import org.bukkit.plugin.InvalidPluginException;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.lang.reflect.Method;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.StandardCharsets;

public class UpdateCheckerTask implements Runnable {

    private static final Insights plugin = Insights.getInstance();
    private static final VersionManager versionManager = VersionManager.getInstance();
    private final CommandSender sender;
    private final String currentVersion;
    private final boolean startup;

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
            jsonObject = readJsonFromURL(GITHUB_INSIGHTS_LINK).getAsJsonObject();
        } catch (IOException ex) {
            Bukkit.getLogger().severe("[Insights] Error fetching new version of Insights");
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
            Bukkit.getLogger().info("[Insights] Downloaded Insights version " + githubVersion
                    + ". Restarting plugin now...");
            Bukkit.getPluginManager().disablePlugin(plugin);
            try {
                Bukkit.getPluginManager().enablePlugin(Bukkit.getPluginManager().loadPlugin(getPluginFile()));
            } catch (InvalidPluginException | InvalidDescriptionException ex) {
                ex.printStackTrace();
                return;
            }
            Bukkit.getLogger().info("[Insights] Successfully upgraded Insights to " + githubVersion + "!");
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
