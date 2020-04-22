package net.frankheijden.insights.enums;

import java.util.logging.Level;

public enum LogType {
    INFO(Level.INFO),
    WARNING(Level.WARNING),
    DEBUG(Level.INFO);

    private final Level level;

    LogType(Level level) {
        this.level = level;
    }

    public Level getLevel() {
        return level;
    }
}
