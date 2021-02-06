package dev.frankheijden.insights.api.config.limits;

import dev.frankheijden.insights.api.config.parser.YamlParseException;

public class LimitParseException extends YamlParseException {

    public LimitParseException(String s) {
        super(s);
    }
}
