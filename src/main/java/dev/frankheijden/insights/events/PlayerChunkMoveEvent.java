package dev.frankheijden.insights.events;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

public class PlayerChunkMoveEvent extends PlayerEvent implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private final Location from;
    private final Location to;
    private boolean cancel = false;

    public PlayerChunkMoveEvent(Player player, Location from, Location to) {
        super(player);
        this.player = player;
        this.from = from;
        this.to = to;
    }

    public Location getFrom() {
        return from;
    }

    public Chunk getFromChunk() {
        return from.getChunk();
    }

    public Location getTo() {
        return to;
    }

    public Chunk getToChunk() {
        return to.getChunk();
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
