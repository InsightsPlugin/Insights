package net.frankheijden.insights.events;

import net.frankheijden.insights.Insights;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.block.BlockBreakEvent;

public class FakeBlockBreakEvent extends BlockBreakEvent {
    private static final HandlerList handlers = new HandlerList();

    public FakeBlockBreakEvent(Block theBlock, Player player) {
        super(theBlock, player);
    }

    @Override
    public void setCancelled(boolean cancel) {
        super.setCancelled(cancel);
        if (cancel) {
            Insights.logger.warning("A plugin tried to cancel Insights' fake BlockBreakEvent - cancelling it won't have any effect!");
        }
    }

    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
