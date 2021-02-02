package dev.frankheijden.insights.api.concurrent.storage;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public abstract class DistributionStorage<T, E> {

    protected final Map<T, Map<E, Integer>> distributionMap;

    protected DistributionStorage(Map<T, Map<E, Integer>> distributionMap) {
        this.distributionMap = distributionMap;
    }

    public Set<T> getKeys() {
        return distributionMap.keySet();
    }

    public void put(T key, Map<E, Integer> map) {
        distributionMap.put(key, new ConcurrentHashMap<>(map));
    }

    public void remove(T key) {
        distributionMap.remove(key);
    }

    public boolean contains(T key) {
        return distributionMap.containsKey(key);
    }
}
