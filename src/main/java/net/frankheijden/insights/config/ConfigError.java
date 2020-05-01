package net.frankheijden.insights.config;

public class ConfigError {

    private final String name;
    private final String path;
    private final String error;

    public ConfigError(String name, String path, String error) {
        this.name = name;
        this.path = path;
        this.error = error;
    }

    @Override
    public String toString() {
        return " &4" + name + " &c(&4" + path + "&c): " + error;
    }
}
