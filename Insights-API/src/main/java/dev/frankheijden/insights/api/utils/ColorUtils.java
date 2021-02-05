package dev.frankheijden.insights.api.utils;

import org.bukkit.ChatColor;

public class ColorUtils {

    private ColorUtils() {}

    public static String[] colorize(String... strings) {
        String[] colored = new String[strings.length];
        for (int i = 0; i < strings.length; i++) {
            colored[i] = colorize(strings[i]);
        }
        return colored;
    }

    public static String colorize(String color) {
        return ChatColor.translateAlternateColorCodes('&', color);
    }
}
