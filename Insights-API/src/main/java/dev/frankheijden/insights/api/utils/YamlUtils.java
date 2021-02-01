package dev.frankheijden.insights.api.utils;

import org.bukkit.configuration.ConfigurationSection;

public class YamlUtils {

    private YamlUtils() {}

    /**
     * Appends any keys not present in the current configuration.
     */
    public static void update(ConfigurationSection conf, ConfigurationSection def) {
        for (String key : def.getKeys(false)) {
            Object confValue = conf.get(key);
            Object defValue = def.get(key);
            if (confValue == null) {
                conf.set(key, defValue);
            } else if (defValue instanceof ConfigurationSection) {
                if (confValue instanceof ConfigurationSection) {
                    update((ConfigurationSection) confValue, (ConfigurationSection) defValue);
                } else {
                    conf.set(key, defValue);
                }
            }
        }
    }

    /**
     * Removes any unused keys (ie keys not present in the default configuration).
     */
    public static void removeUnusedKeys(ConfigurationSection conf, ConfigurationSection def) {
        for (String key : conf.getKeys(false)) {
            Object confValue = conf.get(key);
            Object defValue = def.get(key);
            if (defValue == null) {
                conf.set(key, null);
            } else if (confValue instanceof ConfigurationSection) {
                if (defValue instanceof ConfigurationSection) {
                    update((ConfigurationSection) confValue, (ConfigurationSection) defValue);
                } else {
                    conf.set(key, null);
                }
            }
        }
    }
}
