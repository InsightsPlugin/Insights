package dev.frankheijden.insights.api.addons;

import dev.frankheijden.insights.api.InsightsPlugin;
import org.bukkit.Location;
import org.checkerframework.checker.nullness.qual.NonNull;
import java.nio.file.Path;
import java.util.List;

public abstract class InsightsAddon {

    private InsightsPlugin plugin;
    private InsightsAddonContainer addonContainer;
    private Path dataDirectoryPath;

    final void init(
            InsightsPlugin plugin,
            InsightsAddonContainer addonContainer,
            Path addonsPath
    ) {
        this.plugin = plugin;
        this.addonContainer = addonContainer;
        this.dataDirectoryPath = addonsPath.resolve(addonContainer.addonInfo().addonId());
    }

    public void enable() {

    }

    public void disable() {

    }

    public InsightsPlugin insightsPlugin() {
        return plugin;
    }

    public InsightsAddonContainer addonContainer() {
        return addonContainer;
    }

    public Path dataDirectoryPath() {
        return dataDirectoryPath;
    }

    public abstract @NonNull List<AddonRegion> regionsAt(Location location);

}
