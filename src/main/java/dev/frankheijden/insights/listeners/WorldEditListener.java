package dev.frankheijden.insights.listeners;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.extent.Extent;
import dev.frankheijden.insights.api.InsightsAPI;
import dev.frankheijden.insights.managers.CacheManager;
import dev.frankheijden.insights.managers.NMSManager;
import dev.frankheijden.insights.managers.WorldEditManager;
import dev.frankheijden.insights.Insights;
import dev.frankheijden.insights.config.Limit;
import dev.frankheijden.insights.entities.ScanResult;
import dev.frankheijden.insights.utils.MessageUtils;
import dev.frankheijden.insights.utils.TileUtils;
import net.frankheijden.wecompatibility.core.CustomBlock;
import net.frankheijden.wecompatibility.core.ExtentDelegate;
import net.frankheijden.wecompatibility.core.Vector;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.lang.reflect.Constructor;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Map;

public class WorldEditListener implements ExtentDelegate {

    private static final Insights plugin = Insights.getInstance();
    private static final WorldEditManager worldEditManager = WorldEditManager.getInstance();
    private static final CacheManager cacheManager = CacheManager.getInstance();

    private final World world;
    private final Player player;
    private final Extent extent;
    private final EditSession.Stage stage;

    private ScanResult scanResult;
    private Material replacement = null;
    private final Map<String, Boolean> permissionCache;
    private final Map<String, Limit> limitCache;

    private boolean didNotify;

    private WorldEditListener(World world, Player player, Extent extent, EditSession.Stage stage) {
        this.world = world;
        this.player = player;
        this.extent = extent;
        this.stage = stage;

        if (plugin.getConfiguration().GENERAL_WORLDEDIT_TYPE.equalsIgnoreCase("REPLACEMENT")) {
            try {
                replacement = Material.valueOf(plugin.getConfiguration().GENERAL_WORLDEDIT_REPLACEMENT);
            } catch (IllegalArgumentException ignored) {
                replacement = null;
            }
        }

        this.scanResult = new ScanResult();
        this.permissionCache = new HashMap<>();
        this.limitCache = new HashMap<>();
        this.didNotify = false;
    }

    public static String getPackage() {
        String version = NMSManager.getInstance().isPost(13) ? "we7" : "we6";
        return "net.frankheijden.wecompatibility." + version + ".WorldEditExtent";
    }

    public static Extent from(World world, Player player, Extent extent, EditSession.Stage stage) {
        WorldEditListener listener = new WorldEditListener(world, player, extent, stage);
        try {
            Class<?> clazz = Class.forName(getPackage());
            Constructor<?> c = clazz.getDeclaredConstructors()[0];
            Object worldEditExtent = c.newInstance(player, extent, stage, listener);

            Class<?> extentClazz = Extent.class;
            return (Extent) extentClazz.cast(worldEditExtent);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    private boolean hasPermission(String perm) {
        Boolean p = permissionCache.get(perm);
        if (p != null) return p;
        p = player.hasPermission(perm);
        permissionCache.put(perm, p);
        return p;
    }

    private boolean hasLimit(String name) {
        Limit limit = limitCache.computeIfAbsent(name, k -> InsightsAPI.getLimit(player, name));
        return limit != null && !hasPermission(limit.getPermission());
    }

    @Override
    public CustomBlock setBlock(Player player, Vector vector, Material material) {
        String name = material.name();
        boolean replace = hasLimit(name);

        if (!replace) {
            if (plugin.getConfiguration().GENERAL_WORLDEDIT_DISABLE_TILES && TileUtils.isTile(material) && !hasPermission("insights.worldedit.bypass")) {
                replace = true;
            }
        }

        if (!replace) {
            if (plugin.getConfiguration().GENERAL_WORLDEDIT_LIST.contains(name.toUpperCase())) {
                if (!plugin.getConfiguration().GENERAL_WORLDEDIT_WHITELIST && !hasPermission("insights.worldedit.bypass." + name)) {
                    replace = true;
                }
            } else {
                if (plugin.getConfiguration().GENERAL_WORLDEDIT_WHITELIST && !hasPermission("insights.worldedit.bypass." + name)) {
                    replace = true;
                }
            }
        }

        if (replace) {
            CustomBlock block = new CustomBlock(vector, material);
            scanResult.increment(name.toLowerCase());
            if (replacement != null) {
                block.setMaterial(replacement);
            }
            return block;
        }
        return null;
    }

    @Override
    public void onChange(Player player, Vector vector, Material from, Material to) {
        cacheManager.newCacheLocation(vector.toLocation(player.getWorld())).updateCache(from, to);
    }

    private boolean canNotify() {
        return !didNotify && scanResult.getSize() != 0;
    }

    public void tryNotify(Player player) {
        if (!canNotify()) return;
        this.didNotify = true;

        // Header
        MessageUtils.sendMessage(player, "messages.worldedit.header");

        // Entry
        NumberFormat nf = NumberFormat.getInstance();
        scanResult.forEach(e -> MessageUtils.sendMessage(player, "messages.worldedit.format",
                "%entry%", MessageUtils.getCustomName(e.getKey()),
                "%count%", nf.format(e.getValue())));

        // Total
        String total = nf.format(scanResult.getTotalCount());
        if (replacement != null) {
            MessageUtils.sendMessage(player, "messages.worldedit.replaced",
                    "%blocks%", total,
                    "%replacement%", MessageUtils.getCustomName(replacement.name()));
        } else {
            MessageUtils.sendMessage(player, "messages.worldedit.unchanged",
                    "%blocks%", total);
        }

        // Footer
        MessageUtils.sendMessage(player, "messages.worldedit.footer");
    }

    @Override
    public void onCommit(Player player) {
        tryNotify(player);
    }
}
