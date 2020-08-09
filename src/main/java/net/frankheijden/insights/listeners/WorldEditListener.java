package net.frankheijden.insights.listeners;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.extent.Extent;
import net.frankheijden.insights.Insights;
import net.frankheijden.insights.api.InsightsAPI;
import net.frankheijden.insights.config.Limit;
import net.frankheijden.insights.entities.ScanResult;
import net.frankheijden.insights.managers.*;
import net.frankheijden.insights.utils.*;
import net.frankheijden.wecompatibility.core.*;
import net.frankheijden.wecompatibility.core.Vector;
import org.bukkit.*;
import org.bukkit.entity.Player;

import java.lang.reflect.Constructor;
import java.text.NumberFormat;
import java.util.*;

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

    @Override
    public CustomBlock setBlock(Player player, Vector vector, Material material) {
        boolean replace = false;

        String name = material.name();
        Limit limit = InsightsAPI.getLimit(player, name);
        if (limit != null && !hasPermission(limit.getPermission())) {
            replace = true;
        }

        if (!replace) {
            if (plugin.getConfiguration().GENERAL_WORLDEDIT_DISABLE_TILES && TileUtils.isTile(material) && !hasPermission("insights.worldedit.bypass")) {
                replace = true;
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
        cacheManager.updateCache(vector.toLocation(player.getWorld()), from, to);
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
                "%entry%", StringUtils.capitalizeName(e.getKey().toLowerCase()),
                "%count%", nf.format(e.getValue())));

        // Total
        String total = nf.format(scanResult.getTotalCount());
        if (replacement != null) {
            MessageUtils.sendMessage(player, "messages.worldedit.replaced",
                    "%blocks%", total,
                    "%replacement%", StringUtils.capitalizeName(replacement.name().toLowerCase()));
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
