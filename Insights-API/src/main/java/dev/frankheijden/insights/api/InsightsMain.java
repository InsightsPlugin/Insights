package dev.frankheijden.insights.api;

import dev.frankheijden.insights.api.config.Settings;

public interface InsightsMain {

    void reloadSettings();

    Settings getSettings();

}
