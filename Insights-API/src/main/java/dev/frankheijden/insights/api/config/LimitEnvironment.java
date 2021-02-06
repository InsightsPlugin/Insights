package dev.frankheijden.insights.api.config;

import dev.frankheijden.insights.api.config.limits.Limit;
import org.bukkit.entity.Player;
import java.util.UUID;
import java.util.function.Predicate;

public class LimitEnvironment implements Predicate<Limit> {

    private final Player player;
    private final UUID worldUid;

    public LimitEnvironment(Player player, UUID worldUid) {
        this.player = player;
        this.worldUid = worldUid;
    }

    @Override
    public boolean test(Limit limit) {
        return limit.getSettings().appliesToWorld(worldUid)
                && !player.hasPermission(limit.getBypassPermission());
    }
}
