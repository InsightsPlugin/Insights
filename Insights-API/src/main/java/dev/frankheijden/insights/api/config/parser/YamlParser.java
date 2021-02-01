package dev.frankheijden.insights.api.config.parser;

import dev.frankheijden.insights.api.config.ConfigError;
import dev.frankheijden.insights.api.utils.EnumUtils;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.file.YamlConfiguration;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Decorator class for a YamlConfiguration.
 * The main functionality of this class is parsing yaml contents into actual objects,
 * while appending errors which occur during parsing into the ConfigError.Builder object.
 */
public class YamlParser {

    private final YamlConfiguration yaml;
    private final String name;
    private final ConfigError.Builder errors;

    /**
     * Constructs a new YamlParser with given parameters.
     */
    public YamlParser(YamlConfiguration yaml, String name, ConfigError.Builder errors) {
        this.yaml = yaml;
        this.name = name;
        this.errors = errors;
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
                errors.append(name, path, "value must be at least " + min);
            } else if (value > max) {
                errors.append(name, path, "value must be at most " + max);
            } else {
                map.put(key, value);
            }
        }
        return map;
    }

    /**
     * Checks if an object exists at a given path.
     */
    private boolean checkExists(String path) {
        boolean exists = yaml.get(path) != null;
        if (!exists) errors.append(name, path, "object does not exist");
        return exists;
    }

    /**
     * Parses an integer at given path.
     */
    public int getInt(String path, int def, int min, int max) {
        if (!checkExists(path)) return def;
        if (!yaml.isInt(path)) {
            errors.append(name, path, "value is not an integer");
            return def;
        }

        int value = yaml.getInt(path, def);
        if (value < min) {
            errors.append(name, path, "value must be at least " + min);
            return def;
        } else if (value > max) {
            errors.append(name, path, "value must be at most " + max);
            return def;
        }
        return value;
    }

    /**
     * Parses a boolean at given path.
     */
    public boolean getBoolean(String path, boolean def) {
        if (!checkExists(path)) return def;
        if (!yaml.isBoolean(path)) {
            errors.append(name, path, "value is not a boolean");
            return def;
        }
        return yaml.getBoolean(path, def);
    }

    /**
     * Parses an enum at given path.
     */
    @SuppressWarnings("unchecked")
    public <E extends Enum<E>> E getEnum(String path, Enum<E> def) {
        return checkEnum(
                path,
                getString(path, def.name(), EnumUtils.getValues(def.getClass())),
                def.getDeclaringClass(),
                def,
                null
        );
    }

    /**
     * Parses an enum array at given path.
     */
    @SuppressWarnings("unchecked")
    public <E extends Enum<E>> E[] getEnums(String path, Class<E> clazz) {
        List<String> strings = getList(path);
        List<E> enums = new ArrayList<>(strings.size());
        for (String str : strings) {
            E e = checkEnum(path, str, clazz, null, null);
            if (e != null) {
                enums.add(e);
            }
        }
        return enums.toArray((E[]) Array.newInstance(clazz, 0));
    }

    private <E extends Enum<E>> E checkEnum(String path,
                                            String value,
                                            Class<E> clazz,
                                            Enum<E> def,
                                            String friendlyName) {
        String defVal = def == null ? null : def.name();
        String checked = checkString(value, defVal, path, EnumUtils.getValues(clazz), friendlyName);
        return checked == null ? null : Enum.valueOf(clazz, checked);
    }

    /**
     * Parses a string at given path.
     */
    public String getString(String path, String def) {
        if (!checkExists(path)) return def;
        if (!yaml.isString(path)) {
            errors.append(name, path, "value is not a string");
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
                errors.append(name, path, "not a valid " + friendlyName + " (" + value + ")");
            } else {
                String values = allowedValues.stream().collect(Collectors.joining(", ", "\"", "\""));
                errors.append(name, path, "value must be one of " + values);
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
                errors.append(name, path, "invalid key/value pair (" + str + ")");
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
