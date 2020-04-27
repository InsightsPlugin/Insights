package net.frankheijden.insights.listeners;

import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.extent.Extent;
import net.frankheijden.insights.Insights;
import net.frankheijden.insights.api.InsightsAPI;
import net.frankheijden.insights.config.Limit;
import net.frankheijden.insights.entities.ScanResult;
import net.frankheijden.insights.managers.NMSManager;
import net.frankheijden.insights.utils.MessageUtils;
import net.frankheijden.insights.utils.StringUtils;
import net.frankheijden.wecompatibility.core.*;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.lang.reflect.Constructor;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Map;

public class WorldEditListener implements ExtentDelegate {

    private static final Insights plugin = Insights.getInstance();

    private final WorldEditPlugin wePlugin;
    private final Player player;
    private final Extent extent;

    private ScanResult scanResult;
    private Material replacement = null;
    private final Map<String, Boolean> permissionCache;

    private WorldEditListener(Player player, Extent extent) {
        this.wePlugin = (WorldEditPlugin) Bukkit.getPluginManager().getPlugin("WorldEdit");
        this.player = player;
        this.extent = extent;

        if (plugin.getConfiguration().GENERAL_WORLDEDIT_TYPE.equalsIgnoreCase("REPLACEMENT")) {
            try {
                replacement = Material.valueOf(plugin.getConfiguration().GENERAL_WORLDEDIT_REPLACEMENT);
            } catch (IllegalArgumentException ignored) {
                replacement = null;
            }
        }

        this.scanResult = new ScanResult();
        this.permissionCache = new HashMap<>();
    }

    public static String getPackage() {
        String version = NMSManager.getInstance().isPost1_13() ? "we7" : "we6";
        return "net.frankheijden.wecompatibility." + version + ".WorldEditExtent";
    }

    public static Extent from(Player player, Extent extent) {
        WorldEditListener listener = new WorldEditListener(player, extent);
        try {
            Class<?> clazz = Class.forName(getPackage());
            Constructor<?> c = clazz.getDeclaredConstructors()[0];
            Object worldEditExtent = c.newInstance(listener.wePlugin, player, extent, listener);

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
        if (!plugin.getConfiguration().GENERAL_WORLDEDIT_ENABLED) return null;

        String name = material.name();
        Limit limit = InsightsAPI.getLimit(player, name);
        if (limit == null || hasPermission(limit.getPermission())) return null;

        CustomBlock block = new CustomBlock(vector, material);

        scanResult.increment(name.toLowerCase());
        if (replacement != null) {
            block.setMaterial(replacement);
        }
        return block;
    }

    private boolean canNotify() {
        return scanResult.getSize() != 0;
    }

    public void tryNotify(Player player) {
        if (!canNotify()) return;

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
