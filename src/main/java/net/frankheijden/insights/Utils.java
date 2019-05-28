package net.frankheijden.insights;

import org.apache.commons.lang.StringUtils;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class Utils {
    private Insights plugin;
    private boolean isPaper;

    Utils(Insights plugin) {
        this.plugin = plugin;

        try {
            Class.forName("com.destroystokyo.paper.event.server.PaperServerListPingEvent");
            isPaper = true;
        } catch (ClassNotFoundException ignored) {
            isPaper = false;
        }
    }

    public Set<Map.Entry<String, Integer>> getEntitiesInChunk(Chunk chunk) {
        TreeMap<String, Integer> entryTreeMap = new TreeMap<>();
        for (Entity entity : chunk.getEntities()) {
            entryTreeMap.merge(entity.getType().name(), 1, Integer::sum);
        }
        return entryTreeMap.entrySet();
    }

    public Set<Map.Entry<String, Integer>> getTilesInChunk(Chunk chunk) {
        TreeMap<String, Integer> entryTreeMap = new TreeMap<>();
        for (BlockState bs : chunk.getTileEntities()) {
            entryTreeMap.merge(bs.getType().name(), 1, Integer::sum);
        }
        return entryTreeMap.entrySet();
    }

    public Set<Map.Entry<String, Integer>> getEntitiesAndTilesInChunk(Chunk chunk) {
        TreeMap<String, Integer> entryTreeMap = new TreeMap<>();
        for (Entity entity : chunk.getEntities()) {
            entryTreeMap.merge(entity.getType().name(), 1, Integer::sum);
        }
        for (BlockState bs : chunk.getTileEntities()) {
            entryTreeMap.merge(bs.getType().name(), 1, Integer::sum);
        }
        return entryTreeMap.entrySet();
    }

    public int getAmountInChunk(Chunk chunk, EntityType entityType) {
        int count = 0;
        for (Entity entity : chunk.getEntities()) {
            if (entity.getType() == entityType) {
                count++;
            }
        }
        return count;
    }

    public int getAmountInChunk(Chunk chunk, Material material) {
        ChunkSnapshot chunkSnapshot = chunk.getChunkSnapshot();

        int count = 0;
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                for (int y = 0; y < 256; y++) {
                    if (material == getMaterial(chunkSnapshot,x,y,z)) {
                        count++;
                    }
                }
            }
        }
        return count;
    }

    public int updateCachedAmountInChunk(Chunk chunk, Material material, Boolean isBreak) {
        String chunkName = chunk.getX() + "_" + chunk.getZ();

        if (plugin.chunkSnapshotHashMap.containsKey(chunkName) && plugin.chunkSnapshotHashMap.get(chunkName).containsKey(material)) {
            HashMap<Material, Integer> materials = plugin.chunkSnapshotHashMap.get(chunkName);
            Integer i = materials.get(material);
            int newCount = (i == null ? 0 : i);
            if (isBreak != null) {
                if (!isBreak) {
                    newCount = newCount + 1;
                } else {
                    newCount = newCount - 1;
                }
            }

            materials.put(material, newCount);
            plugin.chunkSnapshotHashMap.put(chunkName, materials);

            return newCount;
        }

        int count = getAmountInChunk(chunk, material);

        HashMap<Material, Integer> materials;
        if (plugin.chunkSnapshotHashMap.containsKey(chunkName)) {
            materials = plugin.chunkSnapshotHashMap.get(chunkName);
        } else {
            materials = new HashMap<>();
        }
        materials.put(material, count);
        plugin.chunkSnapshotHashMap.put(chunkName, materials);

        return count;
    }
    
    public EntityType getEntityType(String entityType) {
        try {
            return EntityType.valueOf(entityType);
        } catch (IllegalArgumentException ignored) {
            //
        }
        return null;
    }

    public Chunk getChunk(World world, int x, int z) {
        try {
            if (isPaper) {
                Class<?> worldClass = Class.forName("org.bukkit.World");
                Object worldObject = worldClass.cast(world);
                Method m = worldClass.getDeclaredMethod("getChunkAtAsync", int.class, int.class);
                return (Chunk) m.invoke(worldObject, x, z);
            } else {
                return world.getChunkAt(x, z);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public Material getMaterial(ChunkSnapshot chunkSnapshot, int x, int y, int z) {
        try {
            Class<?> chunkSnapshotClass = Class.forName("org.bukkit.ChunkSnapshot");
            Object chunkSnap = chunkSnapshotClass.cast(chunkSnapshot);
            if (plugin.useNewAPI) {
                Method m = chunkSnapshotClass.getDeclaredMethod("getBlockType", int.class, int.class, int.class);
                return (Material) m.invoke(chunkSnap, x, y, z);
            } else {
                Method m = chunkSnapshotClass.getDeclaredMethod("getBlockTypeId", int.class, int.class, int.class);
                int id = (int) m.invoke(chunkSnap, x, y, z);

                Class<?> materialClass = Class.forName("org.bukkit.Material");
                Method m1 = materialClass.getDeclaredMethod("getMaterial", int.class);
                return (Material) m1.invoke(materialClass, id);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    private ArrayList<String> names = new ArrayList<>(Arrays.asList(
            "SIGN",
            "SIGN_POST",
            "WALL_SIGN",
            "BANNER",
            "STANDING_BANNER",
            "WALL_BANNER",
            "CHEST",
            "TRAPPED_CHEST",
            "ENDER_CHEST",
            "DISPENSER",
            "FURNACE",
            "BREWING_STAND",
            "HOPPER",
            "DROPPER",
            "BEACON",
            "MOB_SPAWNER",
            "NOTE_BLOCK",
            "JUKEBOX",
            "ENCHANTMENT_TABLE",
            "ENDER_PORTAL",
            "ENDER_PORTAL_FRAME",
            "SKULL",
            "DAYLIGHT_DETECTOR",
            "DAYLIGHT_DETECTOR_INVERTED",
            "FLOWER_POT",
            "REDSTONE_COMPARATOR",
            "REDSTONE_COMPARATOR_ON",
            "REDSTONE_COMPARATOR_OFF",
            "BED",
            "BED_BLOCK",
            "CAULDRON"
    ));

    public boolean isTile(Block block) {
        if (block.getState().getClass().getName().contains("CraftBlockState")) {
            if (!plugin.oldActionBar && !isOldTile(block)) {
                return false;
            }
            return true;
        }
        return false;
    }

    public boolean isOldTile(Block block) {
        return names.contains(block.getType().name());
    }

    public String capitalizeName(String name) {
        StringBuilder stringBuilder = new StringBuilder();
        for (String entry : name.split("_")) {
            stringBuilder.append(StringUtils.capitalize(entry)).append(" ");
        }
        String build = stringBuilder.toString();
        return build.substring(0, build.length() - 1);
    }

    public void sendMessage(CommandSender sender, String path, String... placeholders) {
        String message = plugin.messages.getString(path);
        if (message != null && !message.isEmpty()) {
            for (int i = 0; i < placeholders.length; i++,i++) {
                message = message.replace(placeholders[i], placeholders[i + 1]);
            }
            sender.sendMessage(color(message));
        } else {
            System.err.println("[Insights] Missing locale in messages.yml at path '" + path + "'!");
        }
    }

    public String color(String string) {
        if (string == null) return null;
        return ChatColor.translateAlternateColorCodes('&', string);
    }

    public void sendActionBar(Player player, String path, String... placeholders) {
        if (!player.isOnline()) {
            return;
        }

        String message = plugin.messages.getString(path);
        if (message != null && !message.isEmpty()) {
            for (int i = 0; i < placeholders.length; i++,i++) {
                message = message.replace(placeholders[i], placeholders[i + 1]);
            }
            message = color(message);

            try {
                Class<?> craftPlayerClass = Class.forName("org.bukkit.craftbukkit." + plugin.nms + ".entity.CraftPlayer");
                Object craftPlayer = craftPlayerClass.cast(player);
                Object packet;
                Class<?> packetPlayOutChatClass = Class.forName("net.minecraft.server." + plugin.nms + ".PacketPlayOutChat");
                Class<?> packetClass = Class.forName("net.minecraft.server." + plugin.nms + ".Packet");
                if (plugin.oldActionBar) {
                    Class<?> chatSerializerClass = Class.forName("net.minecraft.server." + plugin.nms + ".ChatSerializer");
                    Class<?> iChatBaseComponentClass = Class.forName("net.minecraft.server." + plugin.nms + ".IChatBaseComponent");
                    Method m3 = chatSerializerClass.getDeclaredMethod("a", String.class);
                    Object cbc = iChatBaseComponentClass.cast(m3.invoke(chatSerializerClass, "{\"text\": \"" + message + "\"}"));
                    packet = packetPlayOutChatClass.getConstructor(new Class<?>[]{iChatBaseComponentClass, byte.class}).newInstance(cbc, (byte) 2);
                } else {
                    Class<?> chatComponentTextClass = Class.forName("net.minecraft.server." + plugin.nms + ".ChatComponentText");
                    Class<?> iChatBaseComponentClass = Class.forName("net.minecraft.server." + plugin.nms + ".IChatBaseComponent");
                    try {
                        Class<?> chatMessageTypeClass = Class.forName("net.minecraft.server." + plugin.nms + ".ChatMessageType");
                        Object[] chatMessageTypes = chatMessageTypeClass.getEnumConstants();
                        Object chatMessageType = null;
                        for (Object obj : chatMessageTypes) {
                            if (obj.toString().equals("GAME_INFO")) {
                                chatMessageType = obj;
                            }
                        }
                        Object chatCompontentText = chatComponentTextClass.getConstructor(new Class<?>[]{String.class}).newInstance(message);
                        packet = packetPlayOutChatClass.getConstructor(new Class<?>[]{iChatBaseComponentClass, chatMessageTypeClass}).newInstance(chatCompontentText, chatMessageType);
                    } catch (ClassNotFoundException cnfe) {
                        Object chatCompontentText = chatComponentTextClass.getConstructor(new Class<?>[]{String.class}).newInstance(message);
                        packet = packetPlayOutChatClass.getConstructor(new Class<?>[]{iChatBaseComponentClass, byte.class}).newInstance(chatCompontentText, (byte) 2);
                    }
                }
                Method craftPlayerHandleMethod = craftPlayerClass.getDeclaredMethod("getHandle");
                Object craftPlayerHandle = craftPlayerHandleMethod.invoke(craftPlayer);
                Field playerConnectionField = craftPlayerHandle.getClass().getDeclaredField("playerConnection");
                Object playerConnection = playerConnectionField.get(craftPlayerHandle);
                Method sendPacketMethod = playerConnection.getClass().getDeclaredMethod("sendPacket", packetClass);
                sendPacketMethod.invoke(playerConnection, packet);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } else {
            System.err.println("[Insights] Missing locale in messages.yml at path '" + path + "'!");
        }
    }

    public void reload() {
        File configFile = new File(plugin.getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            Bukkit.getLogger().info("[Insights] config.yml not found, creating!");
            plugin.saveDefaultConfig();
        }
        plugin.config = YamlConfiguration.loadConfiguration(configFile);

        File messagesFile = new File(plugin.getDataFolder(), "messages.yml");
        if (!messagesFile.exists()) {
            Bukkit.getLogger().info("[Insights] messages.yml not found, creating!");
            plugin.saveResource("messages.yml", false);
        }
        plugin.messages = YamlConfiguration.loadConfiguration(messagesFile);

        plugin.max = plugin.config.getInt("general.limit");
    }

    private String getTime(int seconds) {
        if (seconds < 60) {
            if (seconds == 1) {
                return seconds + " second";
            } else {
                return seconds + " seconds";
            }
        } else {
            int minutes = seconds / 60;
            int s = 60 * minutes;
            int secondsLeft = seconds - s;
            if (minutes < 60) {
                if (secondsLeft > 0) {
                    String min = "minutes";
                    String sec = "seconds";
                    if (minutes == 1) {
                        min = "minute";
                    }
                    if (secondsLeft == 1) {
                        sec = "second";
                    }
                    return minutes + " " + min + " " + secondsLeft + " " + sec;
                } else {
                    return minutes == 1 ? minutes + " minute" : minutes + " minutes";
                }
            } else {
                String time;
                String h = "hours";
                String m = "minutes";
                String se = "seconds";
                String d = "days";
                int days;
                int inMins;
                int leftOver;
                if (secondsLeft == 1) {
                    se = "second";
                }
                if (minutes < 1440) {
                    days = minutes / 60;
                    if (days == 1) {
                        h = "hour";
                    }
                    time = days + " "+h+" ";
                    inMins = 60 * days;
                    leftOver = minutes - inMins;
                    if (leftOver == 1) {
                        m = "minute";
                    }
                    if (leftOver >= 1) {
                        time = time + " " + leftOver + " "+m+" ";
                    }

                    if (secondsLeft > 0) {
                        time = time + " " + secondsLeft + " "+se;
                    }

                    return time;
                } else {
                    days = minutes / 1440;
                    if (days == 1) {
                        d = "day";
                    }
                    time = days + " "+d;
                    inMins = 1440 * days;
                    leftOver = minutes - inMins;
                    if (leftOver == 1) {
                        m = "minute";
                    }
                    if (leftOver >= 1) {
                        if (leftOver < 60) {
                            time = time + " " + leftOver + " "+m;
                        } else {
                            int hours = leftOver / 60;
                            if (hours == 1) {
                                h = "hour";
                            }
                            time = time + " " + hours + " "+h;
                            int hoursInMins = 60 * hours;
                            int minsLeft = leftOver - hoursInMins;
                            if (leftOver >= 1) {
                                time = time + " " + minsLeft + " "+m;
                            }
                        }
                    }

                    if (secondsLeft > 0) {
                        time = time + " " + secondsLeft + " "+se;
                    }
                    return time;
                }
            }
        }
    }

    public String getDHMS(long startTime) {
        return getTime((int) TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - startTime));
    }
}
