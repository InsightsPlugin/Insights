package net.frankheijden.insights.tasks;

import net.frankheijden.insights.Insights;
import net.frankheijden.insights.managers.NotificationManager;
import org.bukkit.Bukkit;

public class NotifyTask implements Runnable {

    private static final Insights plugin = Insights.getInstance();

    private Integer taskID;

    public NotifyTask() {
        this.taskID = null;
    }

    @Override
    public void run() {
        NotificationManager.getInstance().removeExpired();
    }

    public void start() {
        taskID = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, this, 0, 5);
    }

    public void restart() {
        if (isRunning()) stop();
        start();
    }

    public boolean isRunning() {
        return taskID != null;
    }

    public void stop() {
        if (taskID == null) return;
        Bukkit.getScheduler().cancelTask(taskID);
        taskID = null;
    }
}
