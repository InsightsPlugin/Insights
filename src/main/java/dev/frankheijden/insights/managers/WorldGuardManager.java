package dev.frankheijden.insights.managers;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import dev.frankheijden.insights.Insights;
import dev.frankheijden.insights.config.RegionBlocks;
import dev.frankheijden.insights.utils.StringUtils;
import org.bukkit.Location;
import org.bukkit.World;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class WorldGuardManager {

    private static final Insights plugin = Insights.getInstance();
    private static WorldGuardManager instance;

    private boolean wg7;

    public WorldGuardManager() {
        instance = this;
        try {
            Class.forName("com.sk89q.worldguard.bukkit.WGBukkit");
            wg7 = false;
        } catch (ClassNotFoundException ex) {
            wg7 = true;
        }
    }

    public static WorldGuardManager getInstance() {
        return instance;
    }

    public ProtectedRegion getInsightsRegion(Location location) {
        for (ProtectedRegion region : getRegions(location)) {
            List<String> strs = plugin.getConfiguration().GENERAL_REGIONS_LIST;
            if (StringUtils.matches(strs, region.getId())) {
                return region;
            }
        }
        return null;
    }

    public ProtectedRegion getRegionWithLimitedBlocks(Location location) {
        for (ProtectedRegion region : getRegions(location)) {
            List<String> strs = plugin.getConfiguration().GENERAL_REGION_BLOCKS.stream()
                    .map(RegionBlocks::getRegex)
                    .collect(Collectors.toList());
            if (StringUtils.matches(strs, region.getId())) {
                return region;
            }
        }
        return null;
    }

    public Set<ProtectedRegion> getRegions(Location location) {
        ApplicableRegionSet regionSet = null;
        try {
            regionSet = getApplicableRegionSet(location);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        if (regionSet == null) return new HashSet<>();
        return regionSet.getRegions();
    }

    public ApplicableRegionSet getApplicableRegionSet(Location location) throws Exception {
        if (wg7) {
            RegionManager regionManager = WorldGuard.getInstance().getPlatform().getRegionContainer().get(BukkitAdapter.adapt(location.getWorld()));
            if (regionManager != null) {
                return regionManager.getApplicableRegions(BlockVector3.at(location.getX(), location.getY(), location.getZ()));
            }
        } else {
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
        }
        return null;
    }
}
