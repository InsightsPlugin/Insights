package dev.frankheijden.insights.api.addons;

import org.bukkit.Location;
import org.checkerframework.checker.nullness.qual.NonNull;
import java.util.List;

public interface InsightsAddon {

    void enable();

    void disable();

    @NonNull List<AddonRegion> regionsAt(Location location);

}
