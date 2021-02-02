package dev.frankheijden.insights.api.utils;

import java.util.HashSet;
import java.util.Set;

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
}
