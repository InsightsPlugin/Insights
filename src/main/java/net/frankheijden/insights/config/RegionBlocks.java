package net.frankheijden.insights.config;

import net.frankheijden.insights.utils.Utils;
import net.frankheijden.insights.utils.YamlUtils;

import java.util.List;

public class RegionBlocks {

    private final boolean whitelist;
    private final String regex;
    private final List<String> blocks;

    public RegionBlocks(boolean whitelist, String regex, List<String> blocks) {
        this.whitelist = whitelist;
        this.regex = regex;
        this.blocks = blocks;
    }

    public static RegionBlocks from(YamlUtils utils, String path) {
        boolean whitelist = utils.getBoolean(YamlUtils.getPath(path, "whitelist"), true);
        String regex = utils.getString(YamlUtils.getPath(path, "regex"), "");
        List<String> blocks = utils.getStringList(YamlUtils.getPath(path, "list"), Utils.SCANNABLE_BLOCKS, "block");
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
