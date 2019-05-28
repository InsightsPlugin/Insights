package net.frankheijden.insights.commands;

import net.frankheijden.insights.Insights;
import net.frankheijden.insights.tasks.ScanTask;
import org.bukkit.Chunk;
import org.bukkit.ChunkSnapshot;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.BlockState;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.text.NumberFormat;
import java.util.*;

public class CommandScanradius implements CommandExecutor, TabExecutor {
    private Insights plugin;

    public CommandScanradius(Insights plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        boolean tilePerm = sender.hasPermission("insights.scanradius.tile");
        boolean entityPerm = sender.hasPermission("insights.scanradius.entity");

        if (tilePerm || entityPerm) {
            if (sender instanceof Player) {
                Player player = (Player) sender;

                if (args.length == 1) {
                    if (entityPerm && tilePerm) {
                        if (args[0].matches("-?(0|[1-9]\\d*)")) {
                            int radius = Integer.valueOf(args[0]);
                            if (radius >= 1 && radius <= 25) {
                                World world = player.getWorld();
                                int x = player.getLocation().getChunk().getX();
                                int z = player.getLocation().getChunk().getZ();

                                TreeMap<String, Integer> entryTreeMap = new TreeMap<>();
                                int totalEntityCount = 0;
                                int totalTileCount = 0;
                                for (int xc = x-radius; xc <= x+radius; xc++) {
                                    for (int zc = z-radius; zc <= z+radius; zc++) {
                                        Chunk chunk = plugin.utils.getChunk(world, xc, zc);
                                        if (!chunk.isLoaded()) {
                                            chunk.load();
                                        }

                                        for (Entity entity : chunk.getEntities()) {
                                            entryTreeMap.merge(entity.getType().name(), 1, Integer::sum);
                                            totalEntityCount++;
                                        }
                                        for (BlockState bs : chunk.getTileEntities()) {
                                            entryTreeMap.merge(bs.getType().name(), 1, Integer::sum);
                                            totalTileCount++;
                                        }
                                    }
                                }

                                if (entryTreeMap.size() > 0) {
                                    plugin.utils.sendMessage(sender, "messages.scanradius.both.header");

                                    for (Map.Entry<String, Integer> entry : entryTreeMap.entrySet()) {
                                        String name = plugin.utils.capitalizeName(entry.getKey().toLowerCase());
                                        plugin.utils.sendMessage(sender, "messages.scanradius.both.format", "%entry%", name, "%count%", NumberFormat.getIntegerInstance().format(entry.getValue()));

                                        totalEntityCount = totalEntityCount + entry.getValue();
                                    }

                                    plugin.utils.sendMessage(sender, "messages.scanradius.both.total", "%entities%", NumberFormat.getIntegerInstance().format(totalEntityCount), "%tiles%", NumberFormat.getIntegerInstance().format(totalTileCount));
                                    plugin.utils.sendMessage(sender, "messages.scanradius.both.footer");
                                } else {
                                    plugin.utils.sendMessage(sender, "messages.scanradius.both.no_entries");
                                }
                            } else {
                                plugin.utils.sendMessage(sender, "messages.scanradius.invalid_radius");
                            }
                        } else {
                            plugin.utils.sendMessage(sender, "messages.scanradius.invalid_number");
                        }
                    } else {
                        plugin.utils.sendMessage(sender, "messages.no_permission");
                    }
                    return true;
                } else if (args.length == 2) {
                    if (args[0].matches("-?(0|[1-9]\\d*)")) {
                        int radius = Integer.valueOf(args[0]);
                        if (radius >= 1 && radius <= 25) {

                            if (args[1].equalsIgnoreCase("entity")) {
                                if (entityPerm) {
                                    World world = player.getWorld();
                                    int x = player.getLocation().getChunk().getX();
                                    int z = player.getLocation().getChunk().getZ();

                                    TreeMap<String, Integer> entityTreeMap = new TreeMap<>();
                                    for (int xc = x-radius; xc <= x+radius; xc++) {
                                        for (int zc = z-radius; zc <= z+radius; zc++) {
                                            Chunk chunk = plugin.utils.getChunk(world, xc, zc);
                                            if (!chunk.isLoaded()) {
                                                chunk.load();
                                            }

                                            for (Entity entity : chunk.getEntities()) {
                                                entityTreeMap.merge(entity.getType().name(), 1, Integer::sum);
                                            }
                                        }
                                    }

                                    if (entityTreeMap.size() > 0) {
                                        plugin.utils.sendMessage(sender, "messages.scanradius.entity.header");

                                        int totalEntityCount = 0;
                                        for (Map.Entry<String, Integer> entry : entityTreeMap.entrySet()) {
                                            String name = plugin.utils.capitalizeName(entry.getKey().toLowerCase());
                                            plugin.utils.sendMessage(sender, "messages.scanradius.entity.format", "%entity%", name, "%count%", NumberFormat.getIntegerInstance().format(entry.getValue()));

                                            totalEntityCount = totalEntityCount + entry.getValue();
                                        }

                                        plugin.utils.sendMessage(sender, "messages.scanradius.entity.total", "%total_count%", NumberFormat.getIntegerInstance().format(totalEntityCount));
                                        plugin.utils.sendMessage(sender, "messages.scanradius.entity.footer");
                                    } else {
                                        plugin.utils.sendMessage(sender, "messages.scanradius.entity.no_entities");
                                    }
                                } else {
                                    plugin.utils.sendMessage(sender, "messages.no_permission");
                                }
                            } else if (args[1].equalsIgnoreCase("tile")) {
                                if (tilePerm) {
                                    World world = player.getWorld();
                                    int x = player.getLocation().getChunk().getX();
                                    int z = player.getLocation().getChunk().getZ();

                                    TreeMap<String, Integer> tileTreeMap = new TreeMap<>();
                                    for (int xc = x-radius; xc <= x+radius; xc++) {
                                        for (int zc = z-radius; zc <= z+radius; zc++) {
                                            Chunk chunk = plugin.utils.getChunk(world, xc, zc);
                                            if (!chunk.isLoaded()) {
                                                chunk.load();
                                            }

                                            for (BlockState bs : chunk.getTileEntities()) {
                                                tileTreeMap.merge(bs.getType().name(), 1, Integer::sum);
                                            }
                                        }
                                    }

                                    if (tileTreeMap.size() > 0) {
                                        plugin.utils.sendMessage(sender, "messages.scanradius.tile.header");

                                        int totalTileCount = 0;
                                        for (Map.Entry<String, Integer> entry : tileTreeMap.entrySet()) {
                                            String name = plugin.utils.capitalizeName(entry.getKey().toLowerCase());
                                            plugin.utils.sendMessage(sender, "messages.scanradius.tile.format", "%tile%", name, "%count%", NumberFormat.getIntegerInstance().format(entry.getValue()));

                                            totalTileCount = totalTileCount + entry.getValue();
                                        }

                                        plugin.utils.sendMessage(sender, "messages.scanradius.tile.total", "%total_count%", NumberFormat.getIntegerInstance().format(totalTileCount));
                                        plugin.utils.sendMessage(sender, "messages.scanradius.tile.footer");
                                    } else {
                                        plugin.utils.sendMessage(sender, "messages.scanradius.tile.no_tiles");
                                    }
                                } else {
                                    plugin.utils.sendMessage(sender, "messages.no_permission");
                                }
                            }
                        } else {
                            plugin.utils.sendMessage(sender, "messages.scanradius.invalid_radius");
                        }
                    } else {
                        plugin.utils.sendMessage(sender, "messages.scanradius.invalid_number");
                    }
                } else if (args.length > 2) {
                    if (args[0].matches("-?(0|[1-9]\\d*)")) {
                        int radius = Integer.valueOf(args[0]);
                        if (radius >= 1 && radius <= 25) {
                            if (args[1].equalsIgnoreCase("custom")) {
                                long now = System.currentTimeMillis();

                                ArrayList<Material> materials = new ArrayList<>();
                                ArrayList<EntityType> entityTypes = new ArrayList<>();
                                boolean isAll = false;
                                for (int i = 2; i < args.length; i++) {
                                    Material material = Material.getMaterial(args[i]);
                                    EntityType entityType = plugin.utils.getEntityType(args[i]);
                                    if (material != null) {
                                        if (sender.hasPermission("insights.scanradius.custom." + material.name())) {
                                            materials.add(material);
                                        } else {
                                            plugin.utils.sendMessage(sender, "messages.no_permission");
                                            return true;
                                        }
                                    } else if (entityType != null) {
                                        if (sender.hasPermission("insights.scanradius.custom." + entityType.name())) {
                                            entityTypes.add(entityType);
                                        } else {
                                            plugin.utils.sendMessage(sender, "messages.no_permission");
                                            return true;
                                        }
                                    } else if (args[i].equalsIgnoreCase("ALL")) {
                                        if (sender.hasPermission("insights.scan.custom.all")) {
                                            isAll = true;
                                        } else {
                                            plugin.utils.sendMessage(sender, "messages.no_permission");
                                            return true;
                                        }
                                    } else {
                                        plugin.utils.sendMessage(sender, "messages.scanradius.custom.invalid_argument", "%argument%", args[i]);
                                        return true;
                                    }
                                }

                                if (materials.isEmpty() && entityTypes.isEmpty() && !isAll) return true;

                                ChunkSnapshot[] chunks = new ChunkSnapshot[(radius+radius+1)*(radius+radius+1)];

                                World world = player.getWorld();
                                int x = player.getLocation().getChunk().getX();
                                int z = player.getLocation().getChunk().getZ();
                                int i = 0;
                                HashMap<String, Integer> entityHashMap = new HashMap<>();
                                for (int xc = x-radius; xc <= x+radius; xc++) {
                                    for (int zc = z - radius; zc <= z + radius; zc++) {
                                        Chunk chunk = plugin.utils.getChunk(world, xc, zc);
                                        if (!chunk.isLoaded()) {
                                            chunk.load(false);
                                        }

                                        if (!entityTypes.isEmpty() || isAll) {
                                            for (Entity entity : chunk.getEntities()) {
                                                if (entityTypes.contains(entity.getType()) || isAll) {
                                                    entityHashMap.merge(entity.getType().name(), 1, Integer::sum);
                                                }
                                            }
                                        }

                                        chunks[i] = chunk.getChunkSnapshot();
                                        i++;
                                    }
                                }

                                for (EntityType entityType : entityTypes) {
                                    if (!entityHashMap.containsKey(entityType.name())) {
                                        entityHashMap.put(entityType.name(), 0);
                                    }
                                }

                                ScanTask task = new ScanTask(plugin, chunks, world, sender, "messages.scanradius.custom", now, (isAll ? null : materials), entityHashMap);
                                task.setPriority(Thread.MIN_PRIORITY);
                                task.start();
                                return true;
                            } else {
                                return false;
                            }
                        } else {
                            plugin.utils.sendMessage(sender, "messages.scanradius.invalid_radius");
                        }
                    } else {
                        plugin.utils.sendMessage(sender, "messages.scanradius.invalid_number");
                    }
                } else {
                    return false;
                }
            } else {
                sender.sendMessage("This command cannot be executed from console!");
            }
        } else {
            plugin.utils.sendMessage(sender, "messages.no_permission");
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender.hasPermission("insights.scanradius.tab")) {
            if (args.length == 2) {
                List<String> list = Arrays.asList("custom", "entity", "tile");
                return StringUtil.copyPartialMatches(args[1], list, new ArrayList<>());
            } else if (args.length > 2 && args[1].equalsIgnoreCase("custom") && args[args.length-1].length() > 0) {
                List<String> list = new ArrayList<>();
                list.add("ALL");
                for (Material material : Material.values()) {
                    list.add(material.name());
                }
                for (EntityType entityType : EntityType.values()) {
                    list.add(entityType.name());
                }
                return StringUtil.copyPartialMatches(args[args.length-1], list, new ArrayList<>());
            }
        }
        return Collections.emptyList();
    }
}
