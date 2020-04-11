package net.frankheijden.insights.config;

import net.frankheijden.insights.utils.YamlUtils;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.List;

public class RegionBlocks {

    private boolean whitelist;
    private String regex;
    private List<String> blocks;

    public RegionBlocks(boolean whitelist, String regex, List<String> blocks) {
        this.whitelist = whitelist;
        this.regex = regex;
        this.blocks = blocks;
    }

    public static RegionBlocks from(YamlConfiguration yml, String path) {
        boolean whitelist = yml.getBoolean(YamlUtils.getPath(path, "whitelist"));
        String regex = yml.getString(YamlUtils.getPath(path, "regex"));
        if (regex == null) {
            System.err.println("[Insights/Config] Invalid configuration in config.yml at path '"
                    + YamlUtils.getPath(path, "regex") + "'!");
        }
        List<String> blocks = yml.getStringList(YamlUtils.getPath(path, "list"));
        return new RegionBlocks(whitelist, regex, blocks);
    }

    public boolean isWhitelist() {
        return whitelist;
    }

    public String getRegex() {
        return regex;
    }

    public List<String> getBlocks() {
        return blocks;
    }
}
