package net.frankheijden.insights.listeners.worldedit;

import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.block.*;
import net.frankheijden.insights.Insights;
import net.frankheijden.insights.api.InsightsAPI;
import net.frankheijden.insights.config.Limit;
import net.frankheijden.insights.entities.ScanResult;
import net.frankheijden.insights.utils.MessageUtils;
import net.frankheijden.insights.utils.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Map;

public class WorldEditExtent extends AbstractDelegateExtent {

    private static final Insights plugin = Insights.getInstance();

    private final Player player;
    private final ScanResult scanResult;
    private BlockState blockState = null;

    private final Map<String, Boolean> permissionCache;

    public WorldEditExtent(Actor actor, Extent extent) {
        super(extent);
        this.player = Bukkit.getPlayer(actor.getName());
        this.scanResult = new ScanResult();

        if (plugin.getConfiguration().GENERAL_WORLDEDIT_TYPE.equalsIgnoreCase("REPLACEMENT")) {
            try {
                Material m = Material.valueOf(plugin.getConfiguration().GENERAL_WORLDEDIT_REPLACEMENT);
                blockState = BukkitAdapter.adapt(Bukkit.createBlockData(m));
            } catch (IllegalArgumentException ignored) {
                blockState = null;
            }
        }

        this.permissionCache = new HashMap<>();
    }

    private boolean hasPermission(String perm) {
        Boolean p = permissionCache.get(perm);
        if (p != null) return p;
        p = player.hasPermission(perm);
        permissionCache.put(perm, p);
        return p;
    }

    @Override
    public <T extends BlockStateHolder<T>> boolean setBlock(BlockVector3 location, T block) throws WorldEditException {
        if (!plugin.getConfiguration().GENERAL_WORLDEDIT_ENABLED) return super.setBlock(location, block);

        String name = BukkitAdapter.adapt(block.getBlockType()).name();
        Limit limit = InsightsAPI.getLimit(player, name);
        if (limit == null || hasPermission(limit.getPermission())) return super.setBlock(location, block);

        scanResult.increment(name);
        if (blockState != null) return super.setBlock(location, blockState);
        return false;
    }

    private boolean canNotify() {
        return scanResult.getSize() != 0;
    }

    public void tryNotify() {
        if (!canNotify()) return;

        // Header
        MessageUtils.sendMessage(player, "messages.worldedit.header");

        // Entry
        NumberFormat nf = NumberFormat.getInstance();
        scanResult.forEach(e -> {
            MessageUtils.sendMessage(player, "messages.worldedit.format",
                    "%entry%", StringUtils.capitalizeName(e.getKey().toLowerCase()),
                    "%count%", nf.format(e.getValue()));
        });

        // Total
        String total = nf.format(scanResult.getTotalCount());
        if (blockState != null) {
            MessageUtils.sendMessage(player, "messages.worldedit.replaced",
                    "%blocks%", total,
                    "%replacement%", blockState.getBlockType().getName());
        } else {
            MessageUtils.sendMessage(player, "messages.worldedit.unchanged",
                    "%blocks%", total);
        }

        // Footer
        MessageUtils.sendMessage(player, "messages.worldedit.footer");
    }

    @Override
    public Operation commit() {
        Operation o = super.commit();
        tryNotify();
        return o;
    }
}
