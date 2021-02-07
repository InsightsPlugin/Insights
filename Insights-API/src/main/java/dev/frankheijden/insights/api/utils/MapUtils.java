package dev.frankheijden.insights.api.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BinaryOperator;

public class MapUtils {

    private MapUtils() {}

    /**
     * Converts a sequence of key-value pairs into a map.
     */
    @SafeVarargs
    public static <T> Map<T, T> toMap(T... objects) {
        if (objects.length % 2 != 0) throw new IllegalArgumentException("Must be a multiple of two");
        Map<T, T> map = new HashMap<>(objects.length >> 1);
        for (int i = 0; i < objects.length; i += 2) {
            map.put(objects[i], objects[i + 1]);
        }
        return map;
    }

    /**
     * Merges the left map into the right map (a into b).
     */
    public static <K, V> void mergeRight(Map<K, V> a, Map<K, V> b, BinaryOperator<V> combiner) {
        for (Map.Entry<K, V> entry : a.entrySet()) {
            b.merge(entry.getKey(), entry.getValue(), combiner);
        }
    }
}
