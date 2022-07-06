package dev.frankheijden.insights.api.tasks;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.frankheijden.insights.api.InsightsPlugin;
import dev.frankheijden.insights.api.config.Messages;
import dev.frankheijden.insights.api.utils.VersionUtils;
import org.bukkit.command.CommandSender;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.logging.Level;

public class UpdateCheckerTask extends InsightsAsyncTask {

    private static final URL GITHUB_LINK;

    static {
        URL githubLink = null;
        try {
            githubLink = new URL("https://api.github.com/repos/InsightsPlugin/Insights/releases/latest");
        } catch (MalformedURLException ex) {
            ex.printStackTrace();
        }
        GITHUB_LINK = githubLink;
    }

    private static final String USER_AGENT = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10.15; rv:77.0)"
            + " Gecko/20100101"
            + " Firefox/77.0";

    private static final String UPDATE_CHECK_START = "Checking for updates...";
    private static final String RATE_LIMIT = "Received ratelimit from GitHub.";
    private static final String GENERAL_ERROR = "Error fetching new version of Insights: {0}";
    private static final String TRY_LATER = "Error fetching new version of Insights, please try again later!";
    private static final String CONNECTION_ERROR = GENERAL_ERROR + ": ({0}) {1} (maybe check your connection?)";
    private static final String UNAVAILABLE = GENERAL_ERROR + ": ({0}) {1} (no update available)";
    private static final String UPDATE_AVAILABLE = "Insights {0} is available!";
    private static final String RELEASE_INFO = "Release info: {0}";
    private static final String UP_TO_DATE = "We are up-to-date!";
    private static Update cachedUpdate = null;

    public UpdateCheckerTask(InsightsPlugin plugin) {
        super(plugin);
    }

    /**
     * Checks the cached version and displays the result, if an update is available to the CommandSender.
     */
    public static void check(CommandSender sender) {
        var messages = InsightsPlugin.getInstance().messages();
        getCachedUpdate().ifPresent(update -> messages.getMessage(Messages.Key.UPDATE_AVAILABLE).addTemplates(
                Messages.tagOf("version", update.version),
                Messages.tagOf("body", update.body)
        ).sendTo(sender));
    }

    public static Optional<Update> getCachedUpdate() {
        return Optional.ofNullable(cachedUpdate);
    }

    @Override
    public void run() {
        plugin.getLogger().info(UPDATE_CHECK_START);

        try {
            HttpURLConnection conn;
            int code;
            try {
                conn = (HttpURLConnection) GITHUB_LINK.openConnection();
                conn.setRequestProperty("User-Agent", USER_AGENT);
                conn.setConnectTimeout(10000);
                code = conn.getResponseCode();
            } catch (ConnectException | UnknownHostException | SocketTimeoutException ex) {
                plugin.getLogger().log(Level.SEVERE, CONNECTION_ERROR, new Object[]{
                        ex.getClass().getSimpleName(),
                        ex.getMessage()
                });
                return;
            } catch (FileNotFoundException ex) {
                plugin.getLogger().log(Level.SEVERE, UNAVAILABLE, new Object[]{
                        ex.getClass().getSimpleName(),
                        ex.getMessage()
                });
                return;
            }

            // If we're ratelimited
            if (conn.getHeaderFieldInt("x-ratelimit-remaining", 60) == 0) {
                plugin.getLogger().warning(RATE_LIMIT);
                return;
            }

            // Get the inputstream
            boolean error = code < 200 || code >= 300;
            InputStream in = error ? conn.getErrorStream() : conn.getInputStream();
            if (in == null) {
                plugin.getLogger().warning(TRY_LATER);
                return;
            }

            // Parse as JsonObject
            JsonObject json;
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
                json = JsonParser.parseReader(reader).getAsJsonObject();
            }

            // Stop here if the response is an error
            if (error) {
                if (json.has("message")) {
                    plugin.getLogger().warning(json.get("message").getAsString());
                }
                return;
            }

            String version = json.getAsJsonPrimitive("tag_name").getAsString()
                    .replace("v", "") // Strip the prefix "v" from the tag
                    .split("-")[0]; // Remove any suffixes
            String body = json.getAsJsonPrimitive("body").getAsString();

            if (VersionUtils.isNewVersion(plugin.getDescription().getVersion(), version)) {
                plugin.getLogger().log(Level.INFO, UPDATE_AVAILABLE, version);
                plugin.getLogger().log(Level.INFO, RELEASE_INFO, body);
                cachedUpdate = new Update(version, body);
            } else {
                plugin.getLogger().info(UP_TO_DATE);
            }
        } catch (IOException ex) {
            plugin.getLogger().log(Level.SEVERE, GENERAL_ERROR, ex.getMessage());
        }
    }

    public static class Update {

        private final String version;
        private final String body;

        public Update(String version, String body) {
            this.version = version;
            this.body = body;
        }

        public String getVersion() {
            return version;
        }

        public String getBody() {
            return body;
        }
    }
}
