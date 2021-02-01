package dev.frankheijden.insights.api.utils;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class EnumUtils {

    private EnumUtils() {}

    /**
     * Retrieves the values of an enumerator.
     * @return An unmodifiable set of values of given enum values.
     */
    public static <E extends Enum<E>> Set<String> getValues(Class<E> enumClass) {
        return getValues(enumClass.getEnumConstants());
    }

    /**
     * Retrieves the values of an enumerator.
     * @return An unmodifiable set of values of given enum values.
     */
    public static <E extends Enum<E>> Set<String> getValues(Enum<E>[] enums) {
        Set<String> values = new HashSet<>();
        for (Enum<E> e : enums) {
            values.add(e.name());
        }
        return Collections.unmodifiableSet(values);
    }
}
