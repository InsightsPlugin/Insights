package dev.frankheijden.insights.api.config;

import dev.frankheijden.insights.api.config.limits.Limit;
import org.bukkit.entity.Player;
import java.util.function.Predicate;

public class LimitEnvironment implements Predicate<Limit> {

    private final Player player;
    private final String worldName;
    private final String addon;

    public LimitEnvironment(Player player, String worldName) {
        this(player, worldName, null);
    }

    /**
     * Constructs a new LimitEnvironment for a given player, in a given world, and given addon.
     */
    public LimitEnvironment(Player player, String worldName, String addon) {
        this.player = player;
        this.worldName = worldName;
        this.addon = addon;
    }

    @Override
    public boolean test(Limit limit) {
        return limit.getSettings().appliesToWorld(worldName)
                && (addon == null || limit.getSettings().appliesToAddon(addon))
                && !player.hasPermission(limit.getBypassPermission());
    }
}
