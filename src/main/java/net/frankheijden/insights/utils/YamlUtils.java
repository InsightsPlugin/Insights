package net.frankheijden.insights.utils;

import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.*;

public class YamlUtils {

    public static Set<String> getKeys(YamlConfiguration yml, String path) {
        MemorySection section = (MemorySection) yml.get(path);
        if (section == null) {
            System.err.println("[Insights] Configuration section in "
                    + yml.getName() + " not found at path '" + path + "'!");
            return new HashSet<>();
        }

        return section.getKeys(false);
    }

    public static Map<String, Integer> getMap(YamlConfiguration yml, String path) {
        Map<String, Integer> map = new HashMap<>();

        for (String key : getKeys(yml, path)) {
            String subPath = getPath(path, key);
            int value = yml.getInt(subPath, 0);
            if (value >= 0) {
                map.put(key, value);
            } else {
                System.err.println("[Insights/Config] Invalid configuration in "
                        + yml.getName() + " at path '"
                        + path + "." + key + "', value must be at least 0!");
            }
        }

        return map;
    }

    public static String getPath(String... paths) {
        return String.join(".", paths);
    }
}
