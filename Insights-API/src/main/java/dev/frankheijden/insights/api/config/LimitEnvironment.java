package dev.frankheijden.insights.api.config;

import dev.frankheijden.insights.api.addons.AddonRegion;
import dev.frankheijden.insights.api.config.limits.Limit;
import dev.frankheijden.insights.api.region.Region;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import java.util.List;
import java.util.function.Predicate;

public class LimitEnvironment implements Predicate<Limit> {

    private final Player player;
    private final List<Region> regions;

    /**
     * Constructs a new LimitEnvironment for a given player, in a given world, and given addon.
     */
    public LimitEnvironment(
            @Nullable Player player,
            @NonNull List<Region> regions
    ) {
        this.player = player;
        this.regions = regions;
    }

    @Override
    public boolean test(Limit limit) {
        if (player != null && player.hasPermission(limit.bypassPermission())) {
            return false;
        }

        for (Region region : regions) {
            World world = Bukkit.getWorld(region.worldUuid());
            if (world == null) return !limit.settings().worldWhitelist();

            if (!limit.settings().appliesToWorld(world.getName())) {
                return false;
            }

            if (region instanceof AddonRegion addonRegion) {
                if (!limit.settings().appliesToAddon(addonRegion.addonId())) {
                    return false;
                }
            }
        }

        return true;
    }
}
