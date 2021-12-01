package dev.frankheijden.insights.api.concurrent.storage;

import dev.frankheijden.insights.api.config.limits.Limit;
import dev.frankheijden.insights.api.config.limits.LimitType;
import dev.frankheijden.insights.api.objects.wrappers.ScanObject;
import java.util.Collection;
import java.util.Set;
import java.util.function.Predicate;

public interface Storage {

    Set<ScanObject<?>> keys();

    default long count() {
        return count(keys());
    }

    long count(ScanObject<?> item);

    /**
     * Counts the summed distribution for given ScanObjects.
     */
    default long count(Collection<? extends ScanObject<?>> items) {
        long count = 0;
        for (ScanObject<?> item : items) {
            count += count(item);
        }
        return count;
    }

    /**
     * Counts the distribution for all ScanObjects of a limit.
     */
    default long count(Limit limit) {
        return count(limit.getScanObjects());
    }

    /**
     * Counts the distribution for given limit and item.
     * Item must be of type Material or EntityType.
     */
    default long count(Limit limit, ScanObject<?> item) {
        return limit.getType() == LimitType.PERMISSION ? count(item) : count(limit);
    }

    /**
     * Counts the distribution of items which match the predicate.
     */
    default long count(Predicate<ScanObject<?>> predicate) {
        long count = 0;
        for (ScanObject<?> key : keys()) {
            if (predicate.test(key)) {
                count += count(key);
            }
        }
        return count;
    }

    void modify(ScanObject<?> item, long amount);

    void mergeRight(Distribution<ScanObject<?>> target);

}
