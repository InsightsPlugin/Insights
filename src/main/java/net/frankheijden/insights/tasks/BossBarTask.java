package net.frankheijden.insights.tasks;

import net.frankheijden.insights.Insights;
import net.frankheijden.insights.managers.BossBarManager;
import org.bukkit.Bukkit;

public class BossBarTask implements Runnable {

    private static final Insights plugin = Insights.getInstance();

    private Integer taskID;

    public BossBarTask() {
        this.taskID = null;
    }

    @Override
    public void run() {
        BossBarManager.getInstance().removeExpiredBossBars();
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
