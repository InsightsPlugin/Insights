package dev.frankheijden.insights.api.concurrent.containers;

import dev.frankheijden.insights.api.concurrent.storage.Distribution;
import java.util.Map;

public abstract class DistributionContainer<E> implements SupplierContainer<Distribution<E>> {

    protected final Map<E, Integer> distributionMap;

    protected DistributionContainer(Map<E, Integer> distributionMap) {
        this.distributionMap = distributionMap;
    }

    @Override
    public Distribution<E> get() {
        run();
        return new Distribution<>(distributionMap);
    }
}
