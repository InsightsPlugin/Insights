package dev.frankheijden.insights.api.config.parser;

import dev.frankheijden.insights.api.config.ConfigError;
import dev.frankheijden.insights.api.utils.EnumUtils;
import dev.frankheijden.insights.api.utils.YamlUtils;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.file.YamlConfiguration;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Decorator class for a YamlConfiguration.
 * The main functionality of this class is parsing yaml contents into actual objects,
 * while appending errors which occur during parsing into the ConfigError.Builder object.
 */
public abstract class YamlParser {

    private final YamlConfiguration yaml;
    private final String name;
    private final Consumer<ConfigError> errorConsumer;

    /**
     * Constructs a new YamlParser with given parameters.
     */
    protected YamlParser(YamlConfiguration yaml, String name, Consumer<ConfigError> errorConsumer) {
        this.yaml = yaml;
        this.name = name;
        this.errorConsumer = errorConsumer;
    }

    public static YamlConfiguration loadYaml(File file) throws IOException {
        return loadYaml(file, null);
    }

    /**
     * Loads the specified File into a YamlParser, given an InputStream of a default configuration.
     * Nodes are automatically added and removed (if unused).
     */
    public static YamlConfiguration loadYaml(File file, InputStream defaultSettings) throws IOException {
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        if (defaultSettings != null) {
            YamlConfiguration def = YamlConfiguration.loadConfiguration(new InputStreamReader(defaultSettings));
            YamlUtils.update(yaml, def);
            YamlUtils.removeUnusedKeys(yaml, def);
            yaml.save(file);
        }
        return yaml;
    }

    /**
     * Joins the given strings into a YAML dot-delimited key.
     */
    public static String joinPaths(String... paths) {
        return String.join(".", paths);
    }

    /**
     * Returns the keys at a given path. If no keys exist, an empty set is returned.
     */
    public Set<String> getKeys(String path) {
        MemorySection section = (MemorySection) yaml.get(path);
        return section == null ? new HashSet<>() : section.getKeys(false);
    }

    /**
     * Parses an integer map at given path.
     */
    public Map<String, Integer> getIntegerMap(String path, int min, int max, int def) {
        return getIntegerMap(path, min, max, def, new HashMap<>());
    }

    /**
     * Parses an integer map at given path.
     */
    public Map<String, Integer> getIntegerMap(String path, int min, int max, int def, Map<String, Integer> map) {
        for (String key : getKeys(path)) {
            String subPath = joinPaths(path, key);
            int value = yaml.getInt(subPath, def);
            if (value < min) {
                errorConsumer.accept(new ConfigError(name, path, "value must be at least " + min));
            } else if (value > max) {
                errorConsumer.accept(new ConfigError(name, path, "value must be at most " + max));
            } else {
                map.put(key, value);
            }
        }
        return map;
    }

    /**
     * Checks if an object exists at a given path.
     */
    public boolean checkExists(String path) {
        boolean exists = yaml.get(path) != null;
        if (!exists) errorConsumer.accept(new ConfigError(name, path, "object does not exist"));
        return exists;
    }

    /**
     * Parses an integer at given path.
     */
    public int getInt(String path, int def, int min, int max) {
        if (!yaml.isInt(path)) {
            errorConsumer.accept(new ConfigError(name, path, "value is not an integer"));
            return def;
        }

        int value = yaml.getInt(path, def);
        if (value < min) {
            errorConsumer.accept(new ConfigError(name, path, "value must be at least " + min));
            return def;
        } else if (value > max) {
            errorConsumer.accept(new ConfigError(name, path, "value must be at most " + max));
            return def;
        }
        return value;
    }

    public boolean getBoolean(String path, boolean def) {
        return getBoolean(path, def, true);
    }

    /**
     * Parses a boolean at given path.
     */
    public boolean getBoolean(String path, boolean def, boolean logError) {
        if (!yaml.isBoolean(path)) {
            if (logError) errorConsumer.accept(new ConfigError(name, path, "value is not a boolean"));
            return def;
        }
        return yaml.getBoolean(path, def);
    }

    public <E extends Enum<E>> E getEnum(String path, Enum<E> def) {
        return getEnum(path, def, def.getDeclaringClass());
    }

    public <E extends Enum<E>> E getEnum(String path, Class<E> clazz) {
        return getEnum(path, null, clazz);
    }

    /**
     * Parses an enum at given path, with nullable default.
     */
    public <E extends Enum<E>> E getEnum(String path, Enum<E> def, Class<E> clazz) {
        return checkEnum(
                path,
                getString(path, def == null ? null : def.name(), EnumUtils.getValues(clazz)),
                clazz,
                def,
                null
        );
    }

    /**
     * Parses an enum array at given path.
     */
    public <E extends Enum<E>> List<E> getEnums(String path, Class<E> clazz) {
        return getEnums(path, clazz, null);
    }

    /**
     * Parses an enum array at given path.
     */
    public <E extends Enum<E>> List<E> getEnums(String path, Class<E> clazz, String friendlyName) {
        List<String> strings = getList(path);
        List<E> enums = new ArrayList<>(strings.size());
        for (String str : strings) {
            E e = checkEnum(path, str, clazz, null, friendlyName);
            if (e != null) {
                enums.add(e);
            }
        }
        return enums;
    }

    /**
     * Checks whether the given enum value is valid.
     */
    public <E extends Enum<E>> E checkEnum(String path,
                                            String value,
                                            Class<E> clazz,
                                            Enum<E> def,
                                            String friendlyName) {
        String defVal = def == null ? null : def.name();
        String checked = checkString(value, defVal, path, EnumUtils.getValues(clazz), friendlyName);
        return checked == null ? null : Enum.valueOf(clazz, checked);
    }

    public String getRawString(String path) {
        String str = yaml.getString(path);
        return (str == null || str.isEmpty()) ? null : str;
    }

    public String getString(String path, String def) {
        return getString(path, def, true);
    }

    /**
     * Parses a string at given path.
     */
    public String getString(String path, String def, boolean logError) {
        if (!yaml.isString(path)) {
            if (logError) errorConsumer.accept(new ConfigError(name, path, "value is not a string"));
            return def;
        }
        return yaml.getString(path, def);
    }

    /**
     * Parses a string at given path.
     * Note: allowedValues must be in uppercase.
     */
    public String getString(String path, String def, String... allowedValues) {
        return getString(path, def, new HashSet<>(Arrays.asList(allowedValues)), null);
    }

    /**
     * Parses a string at given path.
     * Note: allowedValues must be in uppercase.
     */
    public String getString(String path, String def, Set<String> allowedValues) {
        return getString(path, def, allowedValues, null);
    }

    /**
     * Parses a string at given path.
     * Note: allowedValues must be in uppercase.
     */
    public String getString(String path, String def, Set<String> allowedValues, String friendlyName) {
        String value = getString(path, null);
        return value == null ? def : checkString(value, def, path, allowedValues, friendlyName);
    }

    /**
     * Checks if the given string is a valid value, ignore case, and returning null if invalid.
     * Note: allowedValues must be in uppercase.
     */
    private String checkString(String value, String def, String path, Set<String> allowedValues, String friendlyName) {
        String upperCased = value.toUpperCase();
        if (!allowedValues.contains(upperCased)) {
            if (friendlyName != null) {
                errorConsumer.accept(new ConfigError(name, path,
                        "'" + value + "' is not a valid " + friendlyName + "!"));
            } else {
                String values = allowedValues.stream().collect(Collectors.joining(", ", "\"", "\""));
                errorConsumer.accept(new ConfigError(name, path,
                        "'" + value + "' is not valid, it must be one of " + values + "!"));
            }
            return def;
        }
        return upperCased;
    }

    /**
     * Parses a map from a list at given path, using the delimiter to split each list entry.
     */
    public Map<String, Set<String>> getMapFromList(String path, String delimiter) {
        return getMapFromList(path, delimiter, new HashMap<>());
    }

    /**
     * Parses a map from a list at given path, using the delimiter to split each list entry.
     */
    public Map<String, Set<String>> getMapFromList(String path, String delimiter, Map<String, Set<String>> map) {
        for (String str : getList(path)) {
            String[] kv = str.split(delimiter, 2);
            if (kv.length == 1) {
                map.put(kv[0], null);
            } else if (kv.length == 2) {
                map.computeIfAbsent(kv[0], k -> new HashSet<>()).add(kv[1]);
            } else { // Should never happen, split only returns >= 1, and is limited by limit = 2.
                errorConsumer.accept(new ConfigError(name, path, "invalid key/value pair (" + str + ")"));
            }
        }
        return map;
    }

    /**
     * Parses a set at given path.
     */
    public Set<String> getSet(String path) {
        return new HashSet<>(getList(path));
    }

    /**
     * Parses a set at given path.
     * Note: allowedValues must be in uppercase.
     */
    public Set<String> getSet(String path, Set<String> allowedValues, String friendlyName) {
        return new HashSet<>(getList(path, allowedValues, friendlyName));
    }

    /**
     * Parses a set at given path.
     * Note: allowedValues must be in uppercase.
     */
    public Set<String> getSet(String path, Set<String> allowedValues, String friendlyName, List<String> list) {
        return new HashSet<>(getList(path, allowedValues, friendlyName, list));
    }

    /**
     * Parses a list at given path.
     */
    public List<String> getList(String path) {
        return yaml.getStringList(path);
    }

    /**
     * Parses a list at given path.
     * Note: allowedValues must be in uppercase.
     */
    public List<String> getList(String path, Set<String> allowedValues, String friendlyName) {
        return getList(path, allowedValues, friendlyName, new ArrayList<>());
    }

    /**
     * Parses a list at given path.
     * Note: allowedValues must be in uppercase.
     */
    public List<String> getList(String path, Set<String> allowedValues, String friendlyName, List<String> list) {
        for (String str : yaml.getStringList(path)) {
            String checked = checkString(str, null, path, allowedValues, friendlyName);
            if (checked != null) {
                list.add(checked);
            }
        }
        return list;
    }
}
