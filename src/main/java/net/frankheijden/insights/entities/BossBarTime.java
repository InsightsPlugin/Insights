package net.frankheijden.insights.entities;

import org.bukkit.boss.BossBar;

public class BossBarTime {

    private final BossBar bossBar;
    private long endTime;

    public BossBarTime(BossBar bossBar, Long endTime) {
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
