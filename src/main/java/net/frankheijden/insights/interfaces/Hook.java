package net.frankheijden.insights.interfaces;

import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;

/**
 * Interface for hooking into Insights.
 */
public interface Hook {

    /**
     * The plugin which initiated the hook
     * @return Plugin
     */
    Plugin getPlugin();

    /**
     * Whether or not Insights should cancel it's block place listener
     * for the specified block. When returning true in this method,
     * Insights will not scan, nor display the count for the specified
     * area of the block's location.
     *
     * @param block The block Insights calls it's listener on
     * @return True if Insights should cancel for the specified block.
     */
    default boolean shouldCancel(Block block) {
        return false;
    }

    /**
     * Whether or not Insights should cancel it's entity place listener
     * for the specified entity. When returning true in this method,
     * Insights will not scan, nor display the count for the specified
     * area of the entity's location.
     *
     * @param entity The entity Insights calls it's listener on
     * @return True if Insights should cancel for the specified entity.
     */
    default boolean shouldCancel(Entity entity) {
        return false;
    }
}
