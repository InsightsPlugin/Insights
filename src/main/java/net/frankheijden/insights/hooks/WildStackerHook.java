package net.frankheijden.insights.hooks;

import com.bgsoftware.wildstacker.api.WildStackerAPI;
import com.bgsoftware.wildstacker.api.handlers.SystemManager;
import net.frankheijden.insights.api.entities.Hook;
import org.bukkit.block.Block;
import org.bukkit.plugin.Plugin;

public class WildStackerHook extends Hook {
    public WildStackerHook(Plugin plugin) {
        super(plugin);
    }

    @Override
    public boolean shouldCancel(Block block) {
        SystemManager manager = WildStackerAPI.getWildStacker().getSystemManager();
        return manager.isStackedBarrel(block) || manager.isStackedSpawner(block);
    }
}
