package dev.frankheijden.insights.api.addons;

public class InsightsAddonContainer {

    private final InsightsAddonInfo addonInfo;
    private final InsightsAddon addon;

    protected InsightsAddonContainer(InsightsAddonInfo addonInfo, InsightsAddon addon) {
        this.addonInfo = addonInfo;
        this.addon = addon;
    }

    public InsightsAddonInfo addonInfo() {
        return addonInfo;
    }

    public InsightsAddon addon() {
        return addon;
    }
}
