package dev.frankheijden.insights.api.config;

import dev.frankheijden.insights.api.config.limits.Limit;
import java.util.ArrayList;
import java.util.List;

public class Limits {

    private final List<Limit> limits;

    public Limits() {
        limits = new ArrayList<>();
    }

    public void addLimit(Limit limit) {
        this.limits.add(limit);
    }

    public void removeLimit(Limit limit) {
        this.limits.remove(limit);
    }

    public List<Limit> getLimits() {
        return new ArrayList<>(limits);
    }
}
