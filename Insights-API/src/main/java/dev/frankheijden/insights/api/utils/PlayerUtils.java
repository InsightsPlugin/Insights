package dev.frankheijden.insights.api.utils;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;
import java.util.Collection;

public class PlayerUtils {

    private PlayerUtils() {}

    /**
     * Sends an actionbar to players with given content.
     */
    public static void sendActionBar(Collection<? extends Player> players, String content) {
        BaseComponent[] components = TextComponent.fromLegacyText(content);
        for (Player player : players) {
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, components);
        }
    }

    /**
     * Sends an actionbar to the player with given content.
     */
    public static void sendActionBar(Player player, String content) {
        BaseComponent[] components = TextComponent.fromLegacyText(content);
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, components);
    }
}
