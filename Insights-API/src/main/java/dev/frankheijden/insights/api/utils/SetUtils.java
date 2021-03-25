package dev.frankheijden.insights.api.utils;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

public class SetUtils {

    public SetUtils() {}

    /**
     * Returns the intersection of two sets.
     */
    public static <T> Set<T> intersect(Set<T> a, Set<T> b) {
        Set<T> set = new HashSet<>(a);
        set.retainAll(b);
        return set;
    }

    /**
     * Returns the difference of two sets. This method does not assume anything about mutability of the two sets.
     */
    public static <T> Set<T> difference(Set<T> a, Set<T> b) {
        Set<T> set = new HashSet<>(a);
        set.removeAll(b);
        return set;
    }

    /**
     * Finds the first item in a given set that tests positive to the predicate.
     */
    public static <T> T findFirst(Set<T> set, Predicate<? super T> predicate) {
        for (T obj : set) {
            if (predicate.test(obj)) {
                return obj;
            }
        }
        return null;
    }
}
