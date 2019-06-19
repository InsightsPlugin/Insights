package net.frankheijden.insights.api.events;

import org.bukkit.Chunk;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PlayerChunkMoveEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private Player player;
    private Chunk fromChunk;
    private Chunk toChunk;
    private boolean cancel = false;

    public PlayerChunkMoveEvent(Player player, Chunk fromChunk, Chunk toChunk) {
        this.player = player;
        this.fromChunk = fromChunk;
        this.toChunk = toChunk;
    }

    public Player getPlayer() {
        return player;
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
