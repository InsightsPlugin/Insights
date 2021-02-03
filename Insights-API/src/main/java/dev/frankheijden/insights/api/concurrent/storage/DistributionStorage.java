package dev.frankheijden.insights.api.concurrent.storage;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
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

    /**
     * Counts the distribution of specified items at given key.
     */
    public Optional<Integer> count(T key, Collection<? extends E> items) {
        Map<E, Integer> distribution = distributionMap.get(key);
        if (distribution == null) return Optional.empty();

        int count = 0;
        for (E item : items) {
            count += distribution.getOrDefault(item, 0);
        }
        return Optional.of(count);
    }

    /**
     * Modifies the cache of given key & item by amount.
     */
    public void modify(T key, E item, int amount) {
        Map<E, Integer> distribution = distributionMap.get(key);
        if (distribution == null) return;
        int count = distribution.getOrDefault(item, 0);
        distribution.put(item, Math.max(0, count + amount));
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
