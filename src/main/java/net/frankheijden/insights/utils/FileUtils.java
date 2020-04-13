package net.frankheijden.insights.utils;

import net.frankheijden.insights.Insights;
import org.bukkit.Bukkit;

import java.io.File;
import java.io.IOException;

public class FileUtils {

    public static void createInsightsDirectoryIfNotExists() {
        Insights plugin = Insights.getInstance();
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }
    }

    public static File copyResourceIfNotExists(String resource) {
        createInsightsDirectoryIfNotExists();

        Insights plugin = Insights.getInstance();
        File file = new File(plugin.getDataFolder(), resource);
        if (!file.exists()) {
            Bukkit.getLogger().info("[Insights] '" + resource + "' not found, creating!");
            plugin.saveResource(resource, false);
        }
        return file;
    }

    public static File createFileIfNotExists(String fileName) {
        createInsightsDirectoryIfNotExists();

        Insights plugin = Insights.getInstance();
        File file = new File(plugin.getDataFolder(), fileName);
        if (!file.exists()) {
            Bukkit.getLogger().info("[Insights] '" + fileName + "' not found, creating!");
            try {
                file.createNewFile();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return file;
    }
}
