package dev.frankheijden.insights.config;

import dev.frankheijden.insights.utils.CaseInsensitiveHashSet;
import dev.frankheijden.insights.utils.Utils;
import dev.frankheijden.insights.utils.YamlUtils;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public class RegionBlocks {

    private final boolean whitelist;
    private final String regex;
    private final Set<String> blocks;

    public RegionBlocks(boolean whitelist, String regex, Collection<? extends String> blocks) {
        this.whitelist = whitelist;
        this.regex = regex;
        this.blocks = new CaseInsensitiveHashSet(blocks);
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

    public Set<String> getBlocks() {
        return blocks;
    }
}
