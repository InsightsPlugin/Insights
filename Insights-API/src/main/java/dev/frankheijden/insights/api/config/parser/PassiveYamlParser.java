package dev.frankheijden.insights.api.config.parser;

import dev.frankheijden.insights.api.config.ConfigError;
import dev.frankheijden.insights.api.config.Monad;
import org.bukkit.configuration.file.YamlConfiguration;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class PassiveYamlParser extends YamlParser {

    private final ConfigError.Builder errors;

    protected PassiveYamlParser(YamlConfiguration yaml, String name, ConfigError.Builder errors) {
        super(yaml, name, errors::append);
        this.errors = errors;
    }

    public static PassiveYamlParser load(File file) throws IOException {
        return load(file, null);
    }

    public static PassiveYamlParser load(File file, InputStream defaultSettings) throws IOException {
        return new PassiveYamlParser(loadYaml(file, defaultSettings), file.getName(), ConfigError.newBuilder());
    }

    public <T> Monad<T> toMonad(T obj) {
        return new Monad<>(obj, errors.getErrors());
    }
}
