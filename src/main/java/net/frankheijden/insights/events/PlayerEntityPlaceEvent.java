package net.frankheijden.insights.events;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

public class PlayerEntityPlaceEvent extends PlayerEvent implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private final Entity entity;
    private boolean cancel = false;
    private final boolean includedInChunk;

    public PlayerEntityPlaceEvent(Player player, Entity entity) {
        this(player, entity, true);
    }

    public PlayerEntityPlaceEvent(Player player, Entity entity, boolean includedInChunk) {
        super(player);
        this.entity = entity;
        this.includedInChunk = includedInChunk;
    }

    public Entity getEntity() {
        return entity;
    }

    public boolean isIncludedInChunk() {
        return includedInChunk;
    }

    @Override
    public boolean isCancelled() {
        return cancel;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancel = cancel;
    }

    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
