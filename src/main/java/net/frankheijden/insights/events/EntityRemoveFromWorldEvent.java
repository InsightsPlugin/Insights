package net.frankheijden.insights.events;

import org.bukkit.entity.Entity;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityEvent;

public class EntityRemoveFromWorldEvent extends EntityEvent {
    private static final HandlerList handlers = new HandlerList();

    public EntityRemoveFromWorldEvent(Entity what) {
        super(what);
    }

    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
