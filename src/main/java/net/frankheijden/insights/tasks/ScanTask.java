package net.frankheijden.insights.tasks;

import net.frankheijden.insights.Insights;
import org.bukkit.ChunkSnapshot;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.text.NumberFormat;
import java.util.*;

public class ScanTask extends Thread {
    private Insights plugin;
    private ChunkSnapshot[] chunks;
    private World world;
    private CommandSender sender;
    private String path;
    private long startTime;
    private TreeMap<String, Integer> materialCounts = new TreeMap<>();
    private boolean isAll = false;
    private String[] replacements;

    private int chunkCount = 0;

    public ScanTask(Insights plugin, ChunkSnapshot[] chunks, World world, CommandSender sender, String path, long startTime, ArrayList<Material> materials, HashMap<String, Integer> entityTypes, String... replacements) {
        this.plugin = plugin;
        this.chunks = chunks;
        this.world = world;
        this.sender = sender;
        this.path = path;
        this.startTime = startTime;

        if (materials == null) {
            isAll = true;
        } else {
            for (Material material : materials) {
                this.materialCounts.put(material.name(), 0);
            }
        }

        this.materialCounts.putAll(entityTypes);
        this.replacements = replacements;
    }

    @Override
    public void run() {
        plugin.utils.sendMessage(sender, path + ".start", mergeStrings(replacements, "%chunks%", NumberFormat.getIntegerInstance().format(chunks.length)));

        BukkitTask bukkitTask = new BukkitRunnable() {
            @Override
            public void run() {
                plugin.utils.sendMessage(sender, path + ".progress", mergeStrings(replacements, "%count%", NumberFormat.getIntegerInstance().format(chunkCount), "%total%", NumberFormat.getIntegerInstance().format(chunks.length)));
            }
        }.runTaskTimer(plugin, 20 * 10, 20 * 10);

        for (ChunkSnapshot chunkSnapshot : chunks) {
            for (int x = 0; x < 16; x++) {
                for (int y = 0; y < world.getMaxHeight(); y++) {
                    for (int z = 0; z < 16; z++) {
                        Material material = plugin.utils.getMaterial(chunkSnapshot, x,y,z);
                        if (materialCounts.containsKey(material.name()) || isAll) {
                            materialCounts.merge(material.name(), 1, Integer::sum);
                        }
                    }
                }
            }
            chunkCount++;
        }

        bukkitTask.cancel();
        if (materialCounts.size() > 0) {
            plugin.utils.sendMessage(sender, path + ".end.header");
            for (Map.Entry<String, Integer> entry : materialCounts.entrySet()) {
                String name = plugin.utils.capitalizeName(entry.getKey().toLowerCase());
                plugin.utils.sendMessage(sender, path + ".end.format", mergeStrings(replacements, "%entry%", name, "%count%", NumberFormat.getIntegerInstance().format(entry.getValue())));
            }
            plugin.utils.sendMessage(sender, path + ".end.total", mergeStrings(replacements, "%chunks%", NumberFormat.getIntegerInstance().format(chunks.length), "%blocks%", NumberFormat.getIntegerInstance().format(chunks.length * 16 * 16 * 256), "%time%", plugin.utils.getDHMS(startTime)));
            plugin.utils.sendMessage(sender, path + ".end.footer");
        } else {
            plugin.utils.sendMessage(sender, path + ".end.no_entries");
        }
    }

    private String[] mergeStrings(String[] stringArray, String... strings) {
        List<String> list = new ArrayList<>();
        list.addAll(Arrays.asList(stringArray));
        list.addAll(Arrays.asList(strings));
        return list.toArray(new String[0]);
    }
}
