package net.frankheijden.insights.api.entities;

import org.bukkit.block.Block;
import org.bukkit.plugin.Plugin;

/**
 * Interface for hooking into Insights.
 */
public abstract class Hook {
    private final Plugin plugin;

    /**
     * Initializes hook
     * @param plugin Initiator Plugin
     */
    public Hook(Plugin plugin) {
        this.plugin = plugin;
    }

    /**
     * The plugin which initiated the hook
     * @return Plugin
     */
    public Plugin getPlugin() {
        return plugin;
    }

    /**
     * Whether or not Insights should cancel it's block place listener
     * for the specified block. When returning true in this method,
     * Insights will not scan, nor display the count for the specified
     * chunk of the block's location.
     *
     * @param block The block Insights calls it's listener on
     * @return True if Insights should cancel for the specified block.
     */
    public abstract boolean shouldCancel(Block block);
}
