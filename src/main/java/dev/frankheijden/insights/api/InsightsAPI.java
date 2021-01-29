package dev.frankheijden.insights.api;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import dev.frankheijden.insights.config.Config;
import dev.frankheijden.insights.config.Limit;
import dev.frankheijden.insights.config.Limits;
import dev.frankheijden.insights.managers.HookManager;
import dev.frankheijden.insights.managers.MetricsManager;
import dev.frankheijden.insights.managers.ScanManager;
import dev.frankheijden.insights.managers.WorldGuardManager;
import dev.frankheijden.insights.utils.TimeUtils;
import dev.frankheijden.insights.Insights;
import dev.frankheijden.insights.tasks.LoadChunksTask;
import dev.frankheijden.insights.utils.StringUtils;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * InsightsAPI Class, methods are called in a static fashion.
 */
public class InsightsAPI {

    /**
     * Gets the instance of Insights.
     * @return Insights Main class
     */
    public static Insights getInstance() {
        return Insights.getInstance();
    }

    /**
     * Toggles realtime checking for the UUID specified.
     * NOTE: To use realtime checking, the user still needs the permission 'insights.check.realtime'.
     * @param uuid UUID of player
     */
    public static void toggleCheck(UUID uuid) {
        getInstance().getSqLite().toggleRealtimeCheck(uuid);
    }

    /**
     * Enables or disabled realtime checking for the UUID specified.
     * NOTE: To use realtime checking, the user still needs the permission 'insights.check.realtime'.
     * @param uuid UUID of player
     * @param enabled boolean enabled
     */
    public static void setToggleCheck(UUID uuid, boolean enabled) {
        getInstance().getSqLite().setRealtimeCheck(uuid, enabled);
    }

    /**
     * Checks if the player specified is scanning for chunks.
     * @param uuid UUID of player
     * @return boolean scanning
     */
    public static boolean isScanningChunks(UUID uuid) {
        return ScanManager.getInstance().isScanning(uuid);
    }

    /**
     * Gets a percentage between 0 and 1 of the progress of scanning chunks,
     * returns null if the player is not scanning chunks.
     * @param uuid UUID of player
     * @return double progress, or null if no ScanTask.
     */
    public static Double getScanProgress(UUID uuid) {
        LoadChunksTask loadChunksTask = ScanManager.getInstance().getTask(uuid);
        if (loadChunksTask != null) {
            double total = loadChunksTask.getTotalChunks();
            double done = loadChunksTask.getScanChunksTask().getChunksDone();
            double progress = done/total;
            if (progress < 0) {
                progress = 0;
            } else if (progress > 1) {
                progress = 1;
            }
            return progress;
        }
        return null;
    }

    /**
     * Gets the time elapsed for the current scan of a player
     * @param uuid UUID of player
     * @return String time elapsed, or null if no ScanTask.
     */
    public static String getTimeElapsedOfScan(UUID uuid) {
        LoadChunksTask loadChunksTask = ScanManager.getInstance().getTask(uuid);
        if (loadChunksTask != null) {
            return TimeUtils.getDHMS(loadChunksTask.getStartTime());
        }
        return null;
    }

    /**
     * Retrieves the HookManager instance
     * @return HookManager instance
     */
    public static HookManager getHookManager() {
        return HookManager.getInstance();
    }

    /**
     * Checks if limiting is enabled in a world by the config.
     * @param world The world to check
     * @return If enabled or not.
     */
    public static boolean isLimitingEnabled(World world) {
        String name = world.getName();
        Config config = Insights.getInstance().getConfiguration();
        if (config.GENERAL_WORLDS_WHITELIST) {
            if (!StringUtils.matches(config.GENERAL_WORLDS_LIST, name)) {
                return false;
            }
        } else if (StringUtils.matches(config.GENERAL_WORLDS_LIST, name)) {
            return false;
        }
        return true;
    }

    /**
     * Check if limiting is enabled in a region by the config.
     * @param region The region to check
     * @return If enabled or not.
     */
    public static boolean isLimitingEnabled(String region) {
        Config config = Insights.getInstance().getConfiguration();
        if (config.GENERAL_REGIONS_WHITELIST) {
            if (!StringUtils.matches(config.GENERAL_REGIONS_LIST, region)) {
                return false;
            }
        } else if (StringUtils.matches(config.GENERAL_REGIONS_LIST, region)) {
            return false;
        }
        return true;
    }

    /**
     * Checks if the location is in a limited region.
     * @param location The location to check
     * @return Whether or not the location is in a limited region
     */
    public static boolean isInLimitedRegion(Location location) {
        WorldGuardManager worldGuardManager = WorldGuardManager.getInstance();
        if (worldGuardManager != null) {
            ProtectedRegion region = worldGuardManager.getInsightsRegion(location);
            if (region != null) {
                return !isLimitingEnabled(region.getId());
            }
        }
        return false;
    }

    /**
     * Retrieves the limit for a specified player.
     * @param player The player to check for
     * @param str The string what the limit is (entity/material)
     * @return The limit, or null if none
     */
    public static Limit getLimit(Player player, String str) {
        return getLimit(player.getWorld(), player.getLocation(), str, player);
    }

    /**
     * Retrieves the limit for a specified world and location.
     * @param world The world to check if limiting is enabled in there
     * @param location The location to check for if limiting is enabled in that region
     * @param str The string what the limit is (entity/material)
     * @param sender The command sender to apply the limit to
     * @return The limit, or null if none
     */
    public static Limit getLimit(World world, Location location, String str, CommandSender sender) {
        if (!isLimitingEnabled(world)) return null;
        if (location != null && isInLimitedRegion(location)) return null;

        Limits limits = Insights.getInstance().getConfiguration().getLimits();
        Limit limit = limits.getLimit(str, sender);
        if (limit != null) MetricsManager.incrementLimitCount();
        return limit;
    }
}
