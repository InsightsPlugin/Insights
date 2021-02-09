package dev.frankheijden.insights.api.addons;

import org.bukkit.Location;
import java.util.Optional;

public interface InsightsAddon {

    String getPluginName();

    String getAreaName();

    String getVersion();

    Optional<AddonCuboid> getCuboid(Location location);

}
