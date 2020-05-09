package net.frankheijden.insights.utils;

import net.frankheijden.insights.config.ConfigError;
import net.frankheijden.insights.entities.Error;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.*;
import java.util.stream.Collectors;

public class YamlUtils {

    private final YamlConfiguration yml;
    private final String name;
    private final List<Error> errors;

    public YamlUtils(List<Error> errors, YamlConfiguration yml, String name) {
        this.yml = yml;
        this.name = name;
        this.errors = errors;
    }

    public List<Error> getErrors() {
        return errors;
    }

    public Set<String> getKeys(String path) {
        MemorySection section = (MemorySection) yml.get(path);
        if (section == null) {
            return new HashSet<>();
        }

        return section.getKeys(false);
    }

    public Map<String, Integer> getMap(String path) {
        Map<String, Integer> map = new HashMap<>();
        if (yml.get(path) == null) return map;

        for (String key : getKeys(path)) {
            String subPath = getPath(path, key);
            int value = yml.getInt(subPath, -1);
            if (value >= 0) {
                map.put(key, value);
            } else {
                errors.add(new ConfigError(name, path, "value must be at least 0"));
            }
        }

        return map;
    }

    public static String getPath(String... paths) {
        return String.join(".", paths);
    }

    public boolean exists(String path) {
        boolean exists = yml.get(path) != null;
        if (!exists) errors.add(new ConfigError(name, path, "section does not exist"));
        return exists;
    }

    public int getIntWithinRange(String path, int def, Integer min, Integer max) {
        if (!exists(path)) return def;
        int i = yml.getInt(path, -1);

        String error = null;
        if (min != null && i < min) {
            error = "at least &4" + min + "&c";
            i = def;
        }

        if (max != null && i > max) {
            String maxErr = "at most &4" + max + "&c";
            if (error == null) {
                error = maxErr;
            } else {
                error += " and " + maxErr;
            }
            i = def;
        }

        if (error != null) {
            errors.add(new ConfigError(name, path, "value must be " + error));
            return def;
        }
        return i;
    }

    public boolean getBoolean(String path, boolean def) {
        if (!exists(path)) return def;
        return yml.getBoolean(path, def);
    }

    public String getString(String path, String def) {
        if (!exists(path)) return def;
        return yml.getString(path, def);
    }

    public String getString(String path, String def, Set<String> possibleValues) {
        if (!exists(path)) return def;

        String str = yml.getString(path);
        if (str != null && possibleValues.contains(str.toUpperCase())) {
            return str;
        } else {
            String values = possibleValues.stream().collect(Collectors.joining(", ", "\"", "\""));
            errors.add(new ConfigError(name, path,"value must be one of " + values));
            return def;
        }
    }

    public String getString(String path, String def, Set<String> possibleValues, String what) {
        if (!exists(path)) return def;

        String str = yml.getString(path);
        if (str != null && possibleValues.contains(str.toUpperCase())) {
            return str;
        } else {
            errors.add(new ConfigError(name, path, "not a valid " + what + " (" + str + ")"));
            return def;
        }
    }

    public List<String> getStringList(String path) {
        return yml.getStringList(path);
    }

    public List<String> getStringList(String path, Set<String> possibleValues, String what) {
        List<String> values = yml.getStringList(path);
        Set<String> validValues = new HashSet<>();
        values.stream()
                .filter(s -> {
                    boolean valid = possibleValues.contains(s.toUpperCase());
                    if (valid) validValues.add(s);
                    return !valid;
                })
                .map(s -> new ConfigError(name, path, "not a valid " + what + " (" + s + ")"))
                .forEach(errors::add);
        return new ArrayList<>(validValues);
    }
}
