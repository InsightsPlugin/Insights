package net.frankheijden.insights.utils;

import io.papermc.lib.PaperLib;
import net.frankheijden.insights.Insights;
import net.frankheijden.insights.api.entities.ChunkLocation;
import org.apache.commons.lang.StringUtils;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.boss.BossBar;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Utils {
    private Insights plugin;
    private Map<String, String> entityMap;

    public Utils(Insights plugin) {
        this.plugin = plugin;
        this.entityMap = new HashMap<>();
        this.entityMap.put("CHEST_MINECART", "MINECART_CHEST");
        this.entityMap.put("FURNACE_MINECART", "MINECART_FURNACE");
        this.entityMap.put("TNT_MINECART", "MINECART_TNT");
        this.entityMap.put("HOPPER_MINECART", "MINECART_HOPPER");
        this.entityMap.put("_BOAT", "BOAT");
    }

    private List<String> scannableMaterials = null;
    public List<String> getScannableMaterials() {
        if (scannableMaterials == null) {
            scannableMaterials = Stream.concat(
                    Arrays.stream(Material.values())
                            .filter(Material::isBlock)
                            .map(Enum::name),
                    Arrays.stream(EntityType.values())
                            .map(Enum::name)
            ).sorted().collect(Collectors.toList());
        }
        return scannableMaterials;
    }

    public List<ChunkLocation> getChunkLocations(Chunk[] chunks) {
        ArrayList<ChunkLocation> chunkLocations = new ArrayList<>();
        for (Chunk chunk : chunks) {
            chunkLocations.add(new ChunkLocation(chunk));
        }
        return chunkLocations;
    }

    public List<ChunkLocation> getChunkLocations(Chunk chunk, int radius) {
        int x = chunk.getX();
        int z = chunk.getZ();
        ArrayList<ChunkLocation> chunkLocations = new ArrayList<>();
        for (int xc = x-radius; xc <= x+radius; xc++) {
            for (int zc = z - radius; zc <= z + radius; zc++) {
                chunkLocations.add(new ChunkLocation(xc, zc));
            }
        }
        return chunkLocations;
    }

    public int getAmountInChunk(ChunkSnapshot chunkSnapshot, String materialString) {
        int count = 0;
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                for (int y = 0; y < 256; y++) {
                    if (getMaterial(chunkSnapshot,x,y,z).name().equals(materialString)) {
                        count++;
                    }
                }
            }
        }
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

    public Material getMaterial(ChunkSnapshot chunkSnapshot, int x, int y, int z) {
        if (chunkSnapshot == null) return null;

        try {
            Class<?> chunkSnapshotClass = Class.forName("org.bukkit.ChunkSnapshot");
            Object chunkSnap = chunkSnapshotClass.cast(chunkSnapshot);
            if (plugin.shouldUseNewAPI()) {
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

    public String getValidEntry(String name) {
        Material m = Material.getMaterial(name);
        if (m != null && m.isBlock()) {
            return name;
        }
        for (String key : entityMap.keySet()) {
            if (name.contains(key)) {
                return entityMap.get(key);
            }
        }
        return null;
    }

    private ArrayList<String> TILES_1_8 = new ArrayList<>(Arrays.asList(
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

    private ArrayList<String> TILES_1_13_KEYWORDS = new ArrayList<>(Arrays.asList(
            "SIGN",
            "BANNER",
            "_BED",
            "CHEST",
            "ENDER_CHEST",
            "SPAWNER",
            "END_PORTAL",
            "SHULKER_BOX"
    ));
    private ArrayList<String> TILES_1_13 = new ArrayList<>(Arrays.asList(
            "DISPENSER",
            "BARREL",
            "SMOKER",
            "BLAST_FURNACE",
            "CAMPFIRE",
            "LECTERN",
            "FURNACE",
            "BREWING_STAND",
            "HOPPER",
            "DROPPER",
            "BEACON",
            "NOTE_BLOCK",
            "JUKEBOX",
            "ENCHANTING_TABLE",
            "END_GATEWAY",
            "SKELETON_SKULL",
            "SKELETON_WALL_SKULL",
            "WITHER_SKELETON_SKULL",
            "WITHER_SKELETON_WALL_SKULL",
            "ZOMBIE_HEAD",
            "ZOMBIE_WALL_HEAD",
            "PLAYER_HEAD",
            "PLAYER_WALL_HEAD",
            "CREEPER_HEAD",
            "CREEPER_WALL_HEAD",
            "DRAGON_HEAD",
            "DRAGON_WALL_HEAD",
            "CHAIN_COMMAND_BLOCK",
            "REPEATING_COMMAND_BLOCK",
            "COMMAND_BLOCK",
            "STRUCTURE_BLOCK",
            "STRUCTURE_VOID",
            "JIGSAW",
            "DAYLIGHT_DETECTOR",
            "FLOWER_POT",
            "COMPARATOR",
            "CAULDRON",
            "CONDUIT",
            "BELL"
    ));

    public boolean isTile(Block block) {
        String name = block.getType().name();
        for (String key : TILES_1_13_KEYWORDS) {
            if (name.contains(key)) {
                return true;
            }
        }
        return TILES_1_8.contains(name) || TILES_1_13.contains(name);
    }

    public String capitalizeName(String name) {
        StringBuilder stringBuilder = new StringBuilder();
        for (String entry : name.split("_")) {
            stringBuilder.append(StringUtils.capitalize(entry.toLowerCase())).append(" ");
        }
        String build = stringBuilder.toString();
        return build.substring(0, build.length() - 1);
    }

    public void sendMessage(Object object, String path, String... placeholders) {
        if (object instanceof UUID) {
            sendMessage((UUID) object, path, placeholders);
        } else if (object instanceof CommandSender) {
            sendMessage((CommandSender) object, path, placeholders);
        }
    }

    public void sendMessage(UUID uuid, String path, String... placeholders) {
        Player player = Bukkit.getPlayer(uuid);
        if (player != null) {
            sendMessage(player, path, placeholders);
        }
    }

    public void sendMessage(CommandSender sender, String path, String... placeholders) {
        String message = plugin.getMessages().getString(path);
        if (message != null && !message.isEmpty()) {
            for (int i = 0; i < placeholders.length; i++, i++) {
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

    public void sendSpecialMessage(Player player, String path, double progress, String... placeholders) {
        String messageType = plugin.getConfiguration().GENERAL_NOTIFICATION_TYPE;
        if (messageType == null) messageType = "ACTIONBAR";
        if (messageType.toUpperCase().equals("BOSSBAR") && PaperLib.getMinecraftVersion() >= 9) {
            sendBossBar(player, path, progress, placeholders);
        } else {
            sendActionBar(player, path, placeholders);
        }
    }

    private void sendBossBar(Player player, String path, double progress, String... placeholders) {
        String message = plugin.getMessages().getString(path);
        if (message != null && !message.isEmpty()) {
            for (int i = 0; i < placeholders.length; i++,i++) {
                message = message.replace(placeholders[i], placeholders[i + 1]);
            }
            message = color(message);

            BossBar bossBar = plugin.getBossBarUtils().bossBarPlayers.get(player);
            if (bossBar == null) {
                bossBar = plugin.getBossBarUtils().createNewBossBar();
            }
            if (!bossBar.getPlayers().contains(player)) {
                bossBar.addPlayer(player);
            }
            bossBar.setTitle(message);
            bossBar.setProgress(progress);
            bossBar.setVisible(true);

            plugin.getBossBarUtils().bossBarPlayers.put(player, bossBar);
            plugin.getBossBarUtils().bossBarDurationPlayers.put(player, System.currentTimeMillis() + plugin.getBossBarUtils().bossBarDuration);
        } else {
            System.err.println("[Insights] Missing locale in messages.yml at path '" + path + "'!");
        }
    }

    private void sendActionBar(Player player, String path, String... placeholders) {
        String message = plugin.getMessages().getString(path);
        if (message != null && !message.isEmpty()) {
            for (int i = 0; i < placeholders.length; i++,i++) {
                message = message.replace(placeholders[i], placeholders[i + 1]);
            }
            message = color(message);

            sendActionbar(player, message);
        } else {
            System.err.println("[Insights] Missing locale in messages.yml at path '" + path + "'!");
        }
    }

    public void sendActionbar(Player player, String message) {
        try {
            Class<?> craftPlayerClass = Class.forName("org.bukkit.craftbukkit." + plugin.getNms() + ".entity.CraftPlayer");
            Object craftPlayer = craftPlayerClass.cast(player);
            Object packet;
            Class<?> packetPlayOutChatClass = Class.forName("net.minecraft.server." + plugin.getNms() + ".PacketPlayOutChat");
            Class<?> packetClass = Class.forName("net.minecraft.server." + plugin.getNms() + ".Packet");
            if (plugin.shouldUseOldActionBar()) {
                Class<?> chatSerializerClass = Class.forName("net.minecraft.server." + plugin.getNms() + ".ChatSerializer");
                Class<?> iChatBaseComponentClass = Class.forName("net.minecraft.server." + plugin.getNms() + ".IChatBaseComponent");
                Method m3 = chatSerializerClass.getDeclaredMethod("a", String.class);
                Object cbc = iChatBaseComponentClass.cast(m3.invoke(chatSerializerClass, "{\"text\": \"" + message + "\"}"));
                packet = packetPlayOutChatClass.getConstructor(new Class<?>[]{iChatBaseComponentClass, byte.class}).newInstance(cbc, (byte) 2);
            } else {
                Class<?> chatComponentTextClass = Class.forName("net.minecraft.server." + plugin.getNms() + ".ChatComponentText");
                Class<?> iChatBaseComponentClass = Class.forName("net.minecraft.server." + plugin.getNms() + ".IChatBaseComponent");
                try {
                    Class<?> chatMessageTypeClass = Class.forName("net.minecraft.server." + plugin.getNms() + ".ChatMessageType");
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
    }

    private String getHumanTime(int seconds) {
        String s_ = "second";
        String ss_ = "seconds";
        String m_ = "minute";
        String ms_ = "minutes";
        String h_ = "hour";
        String hs_ = "hours";
        String d_ = "day";
        String ds_ = "days";

        String and_ = "and";

        if (seconds < 60) {
            if (seconds == 1) {
                return seconds + " " + s_;
            } else {
                return seconds + " " + ss_;
            }
        } else {
            int minutes = seconds / 60;
            int s = 60 * minutes;
            int secondsLeft = seconds - s;
            if (minutes < 60) {
                if (secondsLeft > 0) {
                    String min = (minutes == 1) ? m_ : ms_;
                    String sec = (secondsLeft == 1) ? s_ : ss_;
                    return minutes + " " + min + " " + and_ + " " + secondsLeft + " " + sec;
                } else {
                    return (minutes == 1) ? minutes + " " + m_ : minutes + " " + ms_;
                }
            } else {
                String time;
                String h = hs_;
                String m = ms_;
                String se = ss_;
                String d = ds_;
                int days;
                int inMins;
                int leftOver;
                if (secondsLeft == 1) {
                    se = s_;
                }
                if (minutes < 1440) {
                    days = minutes / 60;
                    if (days == 1) {
                        h = h_;
                    }
                    time = days + " "+h+" ";
                    inMins = 60 * days;
                    leftOver = minutes - inMins;
                    if (leftOver == 1) {
                        m = m_;
                    }
                    if (leftOver >= 1) {
                        time = time + ", " + leftOver + " "+m+" ";
                    }

                    if (secondsLeft > 0) {
                        time = time + ", " + secondsLeft + " "+se;
                    }

                    return time;
                } else {
                    days = minutes / 1440;
                    if (days == 1) {
                        d = d_;
                    }
                    time = days + " "+d;
                    inMins = 1440 * days;
                    leftOver = minutes - inMins;
                    if (leftOver == 1) {
                        m = m_;
                    }
                    if (leftOver >= 1) {
                        if (leftOver < 60) {
                            time = time + ", " + leftOver + " "+m;
                        } else {
                            int hours = leftOver / 60;
                            if (hours == 1) {
                                h = h_;
                            }
                            int hoursInMins = 60 * hours;
                            int minsLeft = leftOver - hoursInMins;
                            if (minsLeft <= 0 && secondsLeft <= 0) {
                                time = time + " " + and_ + " " + hours + " "+h;
                            } else {
                                time = time + ", " + hours + " "+h;
                            }

                            if (secondsLeft > 0) {
                                if (minsLeft == 1) {
                                    time = time + ", " + minsLeft + " "+m_;
                                } else if (minsLeft >= 1) {
                                    time = time + ", " + minsLeft + " "+ms_;
                                }
                            } else {
                                if (minsLeft == 1) {
                                    time = time + " " + and_ + " " + minsLeft + " "+m_;
                                } else if (minsLeft >= 1) {
                                    time = time + " " + and_ + " " + minsLeft + " "+ms_;
                                }
                            }
                        }
                    }

                    if (secondsLeft > 0) {
                        time = time + " " + and_ + " " + secondsLeft + " "+se;
                    }
                    return time;
                }
            }
        }
    }

    public String getDHMS(long startTime) {
        return getHumanTime((int) TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - startTime));
    }
}
