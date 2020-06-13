package net.frankheijden.insights.managers;

import net.frankheijden.insights.utils.TileUtils;
import org.bukkit.*;
import org.bukkit.block.Block;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Filter;

public class TileManager {

    private static TileManager instance;
    private final Set<Material> tiles;

    public TileManager() {
        instance = this;
        this.tiles = new HashSet<>();
    }

    public static TileManager getInstance() {
        return instance;
    }

    public void calculateTiles(Location location) {
        Filter filterBackup = Bukkit.getLogger().getFilter();
        Bukkit.getLogger().setFilter(record -> false);

        Block block = location.getBlock();
        Material materialBackup = block.getType();
        for (Material m : Material.values()) {
            if (!m.isBlock()) continue;

            block.setType(m, false);
            if (TileUtils.isTile(block)) {
                tiles.add(m);
            }
        }
        block.setType(materialBackup);

        Bukkit.getLogger().setFilter(filterBackup);
    }

    public Set<Material> getTiles() {
        return tiles;
    }

    public boolean isTile(Material material) {
        return tiles.contains(material);
    }
}
