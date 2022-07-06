package dev.frankheijden.insights.api.config.limits;

public class LimitInfo {

    private final String name;
    private final int limit;

    /**
     * Constructs a new LimitInfo object containing the name of the limit and the actual limit.
     */
    public LimitInfo(String name, int limit) {
        this.name = name;
        this.limit = limit;
    }

    public String name() {
        return name;
    }

    public int limit() {
        return limit;
    }
}
