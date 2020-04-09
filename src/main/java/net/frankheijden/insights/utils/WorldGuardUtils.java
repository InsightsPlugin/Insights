package net.frankheijden.insights.utils;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import net.frankheijden.insights.Insights;
import org.bukkit.Location;
import org.bukkit.World;

import java.lang.reflect.Method;

public class WorldGuardUtils {
    private Insights plugin;
    private boolean isNewWG = false;

    public WorldGuardUtils(Insights plugin) {
        this.plugin = plugin;

        try {
            Class.forName("com.sk89q.worldguard.bukkit.WGBukkit");
        } catch (ClassNotFoundException ex) {
            isNewWG = true;
        }
    }

    public ProtectedRegion isInRegion(Location location) {
        ApplicableRegionSet regionSet = getApplicableRegionSet(location);
        if (regionSet != null) {
            for (ProtectedRegion region : regionSet.getRegions()) {
                if (plugin.getConfiguration().GENERAL_REGIONS_LIST.contains(region.getId())) {
                    return region;
                }
            }
        }
        return null;
    }

    public ProtectedRegion isInRegionBlocks(Location location) {
        ApplicableRegionSet regionSet = getApplicableRegionSet(location);
        if (regionSet != null) {
            for (ProtectedRegion region : regionSet.getRegions()) {
                if (plugin.getConfiguration().GENERAL_REGION_BLOCKS_WHITELIST.containsKey(region.getId())) {
                    return region;
                }
            }
        }
        return null;
    }

    public ApplicableRegionSet getApplicableRegionSet(Location location) {
        if (isNewWG) {
            RegionManager regionManager = WorldGuard.getInstance().getPlatform().getRegionContainer().get(BukkitAdapter.adapt(location.getWorld()));
            if (regionManager != null) {
                return regionManager.getApplicableRegions(BlockVector3.at(location.getX(), location.getY(), location.getZ()));
            }
        } else {
            try {
                Class<?> wgBukkitClass = Class.forName("com.sk89q.worldguard.bukkit.WGBukkit");
                Method m = wgBukkitClass.getDeclaredMethod("getRegionManager", World.class);
                Object regionManagerObject = m.invoke(null, location.getWorld());
                if (regionManagerObject != null) {
                    RegionManager regionManager = (RegionManager) regionManagerObject;
                    Class<?> regionManagerClass = Class.forName("com.sk89q.worldguard.protection.managers.RegionManager");
                    Object regionManagerObject2 = regionManagerClass.cast(regionManager);
                    Method getApplicableRegionsMethod = regionManagerClass.getDeclaredMethod("getApplicableRegions", Location.class);
                    return (ApplicableRegionSet) getApplicableRegionsMethod.invoke(regionManagerObject2, location);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return null;
    }

    public boolean contains(String region, Location location) {
        ApplicableRegionSet regionSet = getApplicableRegionSet(location);
        if (regionSet != null) {
            for (ProtectedRegion protectedRegion : regionSet.getRegions()) {
                if (protectedRegion.getId().equalsIgnoreCase(region)) {
                    return true;
                }
            }
        }
        return false;
    }
}
