package net.frankheijden.insights.events;

import org.bukkit.Chunk;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

public class PlayerChunkMoveEvent extends PlayerEvent implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private final Chunk fromChunk;
    private final Chunk toChunk;
    private boolean cancel = false;

    public PlayerChunkMoveEvent(Player player, Chunk fromChunk, Chunk toChunk) {
        super(player);
        this.player = player;
        this.fromChunk = fromChunk;
        this.toChunk = toChunk;
    }

    public Chunk getFromChunk() {
        return fromChunk;
    }

    public Chunk getToChunk() {
        return toChunk;
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
