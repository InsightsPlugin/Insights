package dev.frankheijden.insights.api;

import org.bukkit.plugin.java.JavaPlugin;

public abstract class InsightsPlugin extends JavaPlugin implements InsightsMain {

    /**
     * Reloads all configurations.
     */
    public void reloadConfigs() {
        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }
        reloadSettings();
    }
}
