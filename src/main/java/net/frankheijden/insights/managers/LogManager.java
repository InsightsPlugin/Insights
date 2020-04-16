package net.frankheijden.insights.managers;

import net.frankheijden.insights.Insights;
import net.frankheijden.insights.enums.LogType;
import org.bukkit.Bukkit;

public class LogManager {

    public static void log(LogType logType, String message) {
        log(logType, message, null);
    }

    public static void log(LogType logType, String message, Integer taskID) {
        if (logType == LogType.DEBUG && !Insights.getInstance().getConfiguration().GENERAL_DEBUG) {
            return;
        }
        Bukkit.getLogger().info("[Insights] [" + logType.name() + "] " + ((taskID != null) ? ("[TASK #" + taskID + "] ") : "") + message);
    }
}
