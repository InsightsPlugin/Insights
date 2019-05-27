package net.frankheijden.blocklimiter.tasks;

import net.frankheijden.blocklimiter.BlockLimiter;
import org.bukkit.ChunkSnapshot;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ScanChunksTask extends Thread {
    private BlockLimiter plugin;
    private ChunkSnapshot[][] chunks;
    private Player player;
    private Material material;
    private int materialCount = 0;

    public ScanChunksTask(BlockLimiter plugin, ChunkSnapshot[][] chunks, Player player, Material material) {
        this.plugin = plugin;
        this.chunks = chunks;
        this.player = player;
        this.material = material;
    }

    private class Position {
        public int x;
        public int y;
        public int z;

        public Position(int x, int y, int z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        public String toString() {
            return this.x + " " + this.y + " " + this.z;
        }
    }

    private ArrayList<String> passthroughMaterials = new ArrayList<>(Arrays.asList(
            "AIR", // 1.8 - 1.12.2
            "WOOD_DOOR",
            "WOODEN_DOOR",
            "TRAP_DOOR",
            "IRON_DOOR_BLOCK",
            "IRON_TRAPDOOR",
            "LADDER",

            "CAVE_AIR", // 1.13 +
            "BUBBLE_COLUMN",
            "OAK_DOOR",
            "SPRUCE_DOOR",
            "BIRCH_DOOR",
            "JUNGLE_DOOR",
            "ACACIA_DOOR",
            "DARK_OAK_DOOR",
            "OAK_TRAPDOOR",
            "SPRUCE_TRAPDOOR",
            "BIRCH_TRAPDOOR",
            "JUNGLE_TRAPDOOR",
            "ACACIA_TRAPDOOR",
            "DARK_OAK_TRAPDOOR",
            "IRON_DOOR"
    ));

    @Override
    public void run() {
        int maxHeight = player.getWorld().getMaxHeight();
        int x, y, z;
        boolean[][][] examined = new boolean[this.chunks.length * 16][maxHeight][this.chunks.length * 16];
        for (x = 0; x < examined.length; x++) {
            for (y = 0; y < maxHeight; y++) {
                for (z = 0; z < examined[0][0].length; z++) {
                    examined[x][y][z] = false;
                }
            }
        }

        Position currentPosition = null;
        for (x = 0; x < examined.length && currentPosition == null; x++) {
            for (z = 0; z < examined[0][0].length && currentPosition == null; z++) {
                Position position = new Position(x, maxHeight - 1, z);
                if (this.getMaterialAt(position) == Material.AIR) {
                    currentPosition = position;
                }
            }
        }

        BukkitTask bukkitTask = new BukkitRunnable() {
            @Override
            public void run() {
                plugin.utils.sendMessage(player, "messages.scanradius.individual.progress", "%entry%", plugin.utils.capitalizeName(material.name().toLowerCase()), "%count%", String.valueOf(materialCount));
            }
        }.runTaskTimer(plugin, 20 * 10, 20 * 10);

        ConcurrentLinkedQueue<Position> unexaminedQueue = new ConcurrentLinkedQueue<>();
        try {
            assert currentPosition != null;
            examined[currentPosition.x][currentPosition.y][currentPosition.z] = true;
        } catch (ArrayIndexOutOfBoundsException ex) {
            ex.printStackTrace();
        }
        unexaminedQueue.add(currentPosition);

        while (!unexaminedQueue.isEmpty()) {
            currentPosition = unexaminedQueue.remove();
            Material examinedMaterial = this.getMaterialAt(currentPosition);

            if (examinedMaterial == null || currentPosition.y < 0) continue;

            ConcurrentLinkedQueue<Position> adjacentPositionQueue = new ConcurrentLinkedQueue<>();
            adjacentPositionQueue.add(new Position(currentPosition.x + 1, currentPosition.y, currentPosition.z));
            adjacentPositionQueue.add(new Position(currentPosition.x - 1, currentPosition.y, currentPosition.z));
            adjacentPositionQueue.add(new Position(currentPosition.x, currentPosition.y, currentPosition.z + 1));
            adjacentPositionQueue.add(new Position(currentPosition.x, currentPosition.y, currentPosition.z - 1));
            adjacentPositionQueue.add(new Position(currentPosition.x, currentPosition.y + 1, currentPosition.z));
            adjacentPositionQueue.add(new Position(currentPosition.x, currentPosition.y - 1, currentPosition.z));

            while (!adjacentPositionQueue.isEmpty()) {
                Position adjacentPosition = adjacentPositionQueue.remove();

                try {
                    if (!examined[adjacentPosition.x][adjacentPosition.y][adjacentPosition.z]) {
                        examined[adjacentPosition.x][adjacentPosition.y][adjacentPosition.z] = true;
                        unexaminedQueue.add(adjacentPosition);
                    }
                } catch (ArrayIndexOutOfBoundsException ignored) {}
            }

            if (examinedMaterial == material) {
                materialCount++;
            }
        }

        bukkitTask.cancel();
        plugin.utils.sendMessage(player, "messages.scanradius.individual.total", "%entry%", plugin.utils.capitalizeName(material.name().toLowerCase()), "%count%", String.valueOf(materialCount));
        /*
        ScanChunksMessageTask scanChunksMessageTask = new ScanChunksMessageTask(plugin, player, material, materialCount);
        plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, scanChunksMessageTask, 5L);*/
    }

    private Material getMaterialAt(Position position) {
        int chunkX = position.x / 16;
        int chunkZ = position.z / 16;

        try {
            ChunkSnapshot snapshot = this.chunks[chunkX][chunkZ];
            return plugin.utils.getMaterial(snapshot, position.x % 16, position.y, position.z % 16);
        } catch (IndexOutOfBoundsException ignored) {}

        return null;
    }
}
