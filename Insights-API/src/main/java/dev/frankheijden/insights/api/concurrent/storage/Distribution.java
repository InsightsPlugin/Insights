package dev.frankheijden.insights.api.concurrent.storage;

import dev.frankheijden.insights.api.utils.MapUtils;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

public class Distribution<E> {

    protected final Map<E, Integer> distributionMap;

    public Distribution(Map<E, Integer> distributionMap) {
        this.distributionMap = distributionMap;
    }

    /**
     * Retrieves the distribution of a single item.
     */
    public int count(E item) {
        return distributionMap.getOrDefault(item, 0);
    }

    /**
     * Retrieves and combines the distribution of specified items.
     */
    public int count(Collection<? extends E> items) {
        int count = 0;
        for (E item : items) {
            count += distributionMap.getOrDefault(item, 0);
        }
        return count;
    }

    /**
     * Modifies the distribution of given item by an integer amount.
     */
    public void modify(E item, int amount) {
        distributionMap.compute(item, (e, count) -> {
            if (count == null) count = 0;
            return Math.max(0, count + amount);
        });
    }

    /**
     * Returns the keys in this distribution.
     */
    public Set<E> keys() {
        return distributionMap.keySet();
    }

    /**
     * Merges the current instance into another distribution, summing their values.
     */
    public void merge(Distribution<E> target) {
        MapUtils.merge(target.distributionMap, this.distributionMap, Integer::sum);
    }

    /**
     * Copies this instance over to a new Distribution using given map.
     */
    public Distribution<E> copy(Map<E, Integer> map) {
        map.putAll(distributionMap);
        return new Distribution<>(map);
    }
}
