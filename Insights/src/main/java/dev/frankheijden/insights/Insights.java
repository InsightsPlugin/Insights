package dev.frankheijden.insights;

import dev.frankheijden.insights.api.InsightsPlugin;
import dev.frankheijden.insights.api.config.Settings;
import java.io.File;
import java.io.IOException;

public class Insights extends InsightsPlugin {

    private static final String SETTINGS_FILE_NAME = "config.yml";

    private static Insights instance;
    private Settings settings;

    @Override
    public void onLoad() {
        super.onLoad();
        instance = this;
    }

    public static Insights getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        super.onEnable();
        reloadConfigs();
    }

    @Override
    public void reloadSettings() {
        File file = new File(getDataFolder(), SETTINGS_FILE_NAME);
        try {
            settings = Settings.load(file, getResource(SETTINGS_FILE_NAME)).exceptionally(getLogger());
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public Settings getSettings() {
        return settings;
    }
}
