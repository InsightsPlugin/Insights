package net.frankheijden.insights.config;

import net.frankheijden.insights.entities.Error;

public class ConfigError extends Error {

    private final String name;
    private final String path;

    public ConfigError(String name, String path, String error) {
        super(error);
        this.name = name;
        this.path = path;
    }

    @Override
    public String toString() {
        return " &4" + name + " &c(&4" + path + "&c): " + getError();
    }
}
