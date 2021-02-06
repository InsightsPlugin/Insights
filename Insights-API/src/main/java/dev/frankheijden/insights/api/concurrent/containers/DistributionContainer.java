package dev.frankheijden.insights.api.concurrent.containers;

import java.util.Map;

public abstract class DistributionContainer<E> implements SupplierContainer<Map<E, Integer>> {

    protected final Map<E, Integer> distributionMap;

    protected DistributionContainer(Map<E, Integer> distributionMap) {
        this.distributionMap = distributionMap;
    }

    @Override
    public Map<E, Integer> get() {
        run();
        return distributionMap;
    }
}
