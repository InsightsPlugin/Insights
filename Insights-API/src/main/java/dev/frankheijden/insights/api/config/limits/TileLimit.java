package dev.frankheijden.insights.api.config.limits;

import dev.frankheijden.insights.api.config.parser.YamlParseException;
import dev.frankheijden.insights.api.config.parser.YamlParser;

public class TileLimit extends Limit {

    protected TileLimit(Info info) {
        super(LimitType.TILE, info);
    }

    /**
     * Parses a TileLimit.
     */
    public static TileLimit parse(YamlParser parser, Info info) throws YamlParseException {
        return new TileLimit(info);
    }
}
