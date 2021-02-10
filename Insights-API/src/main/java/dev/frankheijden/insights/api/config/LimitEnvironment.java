package dev.frankheijden.insights.api.config;

import dev.frankheijden.insights.api.config.limits.Limit;
import org.bukkit.entity.Player;
import java.util.UUID;
import java.util.function.Predicate;

public class LimitEnvironment implements Predicate<Limit> {

    private final Player player;
    private final UUID worldUid;
    private final String addon;

    public LimitEnvironment(Player player, UUID worldUid) {
        this(player, worldUid, null);
    }

    /**
     * Constructs a new LimitEnvironment for a given player, in a given world, and given addon.
     */
    public LimitEnvironment(Player player, UUID worldUid, String addon) {
        this.player = player;
        this.worldUid = worldUid;
        this.addon = addon;
    }

    @Override
    public boolean test(Limit limit) {
        return limit.getSettings().appliesToWorld(worldUid)
                && (addon == null || limit.getSettings().appliesToAddon(addon))
                && !player.hasPermission(limit.getBypassPermission());
    }
}
