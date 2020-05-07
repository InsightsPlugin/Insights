package net.frankheijden.insights.entities;

import org.bukkit.boss.BossBar;

public class Notification {

    private final BossBar bossBar;
    private long endTime;

    public Notification(BossBar bossBar, Long endTime) {
        this.bossBar = bossBar;
        this.endTime = endTime;
    }

    public BossBar getBossBar() {
        return bossBar;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(Long endTime) {
        this.endTime = endTime;
    }
}
