package dev.frankheijden.insights.api.concurrent.storage;

import dev.frankheijden.insights.api.utils.MapUtils;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

public class Distribution<E> {

    protected final Map<E, Long> distributionMap;

    public Distribution(Map<E, Long> distributionMap) {
        this.distributionMap = distributionMap;
    }

    /**
     * Retrieves the distribution of all items.
     */
    public long count() {
        long count = 0;
        for (long distribution : distributionMap.values()) {
            count += distribution;
        }
        return count;
    }

    /**
     * Retrieves the distribution of a single item.
     */
    public long count(E item) {
        return item == null ? 0 : distributionMap.getOrDefault(item, 0L);
    }

    /**
     * Retrieves the distribution of items which match the predicate.
     */
    public long count(Predicate<E> predicate) {
        long count = 0;
        for (Map.Entry<E, Long> entry : distributionMap.entrySet()) {
            if (predicate.test(entry.getKey())) {
                count += entry.getValue();
            }
        }
        return count;
    }

    /**
     * Modifies the distribution of given item by an amount.
     */
    public void modify(E item, long amount) {
        if (item == null) return;
        distributionMap.compute(item, (e, count) -> {
            if (count == null) count = 0L;
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
        MapUtils.mergeRight(this.distributionMap, target.distributionMap, Long::sum);
    }

    /**
     * Copies this instance over to a new Distribution using given map.
     */
    public Distribution<E> copy(Map<E, Long> map) {
        map.putAll(distributionMap);
        return new Distribution<>(map);
    }
}
