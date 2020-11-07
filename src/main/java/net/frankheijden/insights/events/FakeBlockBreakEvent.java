package net.frankheijden.insights.events;

import net.frankheijden.insights.Insights;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;

public class FakeBlockBreakEvent extends BlockBreakEvent {

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
}
