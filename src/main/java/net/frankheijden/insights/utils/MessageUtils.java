package net.frankheijden.insights.utils;

import net.frankheijden.insights.Insights;
import net.frankheijden.insights.managers.BossBarManager;
import net.frankheijden.insights.managers.NMSManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.UUID;

public class MessageUtils {

    private static final Insights plugin = Insights.getInstance();

    public static void sendMessage(Object object, String path, String... placeholders) {
        if (object instanceof UUID) {
            sendMessage((UUID) object, path, placeholders);
        } else if (object instanceof CommandSender) {
            sendMessage((CommandSender) object, path, placeholders);
        }
    }

    public static void sendMessage(UUID uuid, String path, String... placeholders) {
        Player player = Bukkit.getPlayer(uuid);
        if (player != null) {
            sendMessage(player, path, placeholders);
        }
    }

    public static void sendMessage(CommandSender sender, String path, String... placeholders) {
        String msg = getMessage(path, placeholders);
        if (msg == null || msg.isEmpty()) return;
        sender.sendMessage(msg);
    }

    public static String getMessage(String path, String... placeholders) {
        String message = plugin.getMessages().getString(path);
        if (message != null) {
            for (int i = 0; i < placeholders.length; i++, i++) {
                message = message.replace(placeholders[i], placeholders[i + 1]);
            }
            return color(message);
        } else {
            System.err.println("[Insights] Missing locale in messages.yml at path '" + path + "'!");
        }
        return "";
    }

    public static String color(String string) {
        if (string == null) return null;
        return ChatColor.translateAlternateColorCodes('&', string);
    }

    public static void sendSpecialMessage(Player player, String path, double progress, String... placeholders) {
        String messageType = plugin.getConfiguration().GENERAL_NOTIFICATION_TYPE;
        if (messageType == null) messageType = "ACTIONBAR";
        if (messageType.toUpperCase().equals("BOSSBAR") && NMSManager.getInstance().isPost1_9()) {
            sendBossBar(player, path, progress, placeholders);
        } else {
            sendActionBar(player, path, placeholders);
        }
    }

    private static void sendBossBar(Player player, String path, double progress, String... placeholders) {
        BossBarManager.getInstance().displayBossBar(player, getMessage(path, placeholders), progress);
    }

    private static void sendActionBar(Player player, String path, String... placeholders) {
        sendActionbar(player, getMessage(path, placeholders));
    }

    public static void sendActionbar(Player player, String message) {
        try {
            Class<?> craftPlayerClass = Class.forName("org.bukkit.craftbukkit." + NMSManager.NMS + ".entity.CraftPlayer");
            Object craftPlayer = craftPlayerClass.cast(player);
            Object packet;
            Class<?> packetPlayOutChatClass = Class.forName("net.minecraft.server." + NMSManager.NMS + ".PacketPlayOutChat");
            Class<?> packetClass = Class.forName("net.minecraft.server." + NMSManager.NMS + ".Packet");
            if (!NMSManager.getInstance().isPost1_8_R2()) {
                Class<?> chatSerializerClass = Class.forName("net.minecraft.server." + NMSManager.NMS + ".ChatSerializer");
                Class<?> iChatBaseComponentClass = Class.forName("net.minecraft.server." + NMSManager.NMS + ".IChatBaseComponent");
                Method m3 = chatSerializerClass.getDeclaredMethod("a", String.class);
                Object cbc = iChatBaseComponentClass.cast(m3.invoke(chatSerializerClass, "{\"text\": \"" + message + "\"}"));
                packet = packetPlayOutChatClass.getConstructor(new Class<?>[]{iChatBaseComponentClass, byte.class}).newInstance(cbc, (byte) 2);
            } else {
                Class<?> chatComponentTextClass = Class.forName("net.minecraft.server." + NMSManager.NMS + ".ChatComponentText");
                Class<?> iChatBaseComponentClass = Class.forName("net.minecraft.server." + NMSManager.NMS + ".IChatBaseComponent");
                try {
                    Class<?> chatMessageTypeClass = Class.forName("net.minecraft.server." + NMSManager.NMS + ".ChatMessageType");
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
}
