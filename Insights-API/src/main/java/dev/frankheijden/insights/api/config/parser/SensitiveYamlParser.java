package dev.frankheijden.insights.api.config.parser;

import org.bukkit.configuration.file.YamlConfiguration;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class SensitiveYamlParser extends YamlParser {

    protected SensitiveYamlParser(YamlConfiguration yaml, String name) {
        super(yaml, name, error -> {
            throw new YamlParseException(error.toString());
        });
    }

    public static SensitiveYamlParser load(File file) throws IOException {
        return load(file, null);
    }

    public static SensitiveYamlParser load(File file, InputStream defaultSettings) throws IOException {
        return new SensitiveYamlParser(loadYaml(file, defaultSettings), file.getName());
    }
}
