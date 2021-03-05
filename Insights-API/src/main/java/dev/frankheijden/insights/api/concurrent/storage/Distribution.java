package dev.frankheijden.insights.api.concurrent.storage;

import dev.frankheijden.insights.api.utils.MapUtils;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

public class Distribution<E> {

    protected final Map<E, Integer> distributionMap;

    public Distribution(Map<E, Integer> distributionMap) {
        this.distributionMap = distributionMap;
    }

    /**
     * Retrieves the distribution of all items.
     */
    public int count() {
        int count = 0;
        for (int distribution : distributionMap.values()) {
            count += distribution;
        }
        return count;
    }

    /**
     * Retrieves the distribution of a single item.
     */
    public int count(E item) {
        return item == null ? 0 : distributionMap.getOrDefault(item, 0);
    }

    /**
     * Retrieves the distribution of items which match the predicate.
     */
    public int count(Predicate<E> predicate) {
        int count = 0;
        for (Map.Entry<E, Integer> entry : distributionMap.entrySet()) {
            if (predicate.test(entry.getKey())) {
                count += entry.getValue();
            }
        }
        return count;
    }

    /**
     * Modifies the distribution of given item by an integer amount.
     */
    public void modify(E item, int amount) {
        if (item == null) return;
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
    public void mergeRight(Distribution<E> target) {
        MapUtils.mergeRight(this.distributionMap, target.distributionMap, Integer::sum);
    }

    /**
     * Copies this instance over to a new Distribution using given map.
     */
    public Distribution<E> copy(Map<E, Integer> map) {
        map.putAll(distributionMap);
        return new Distribution<>(map);
    }
}
