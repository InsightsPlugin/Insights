package dev.frankheijden.insights.api.utils;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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

    /**
     * Retrieves a list of enums by regex for the given enum class.
     */
    public static <E extends Enum<E>> List<E> getEnumsByRegex(String regex, Class<E> enumClass) {
        Pattern pattern = Pattern.compile(regex);
        return EnumUtils.getValues(enumClass).stream()
                .filter(str -> pattern.matcher(str).matches())
                .map(str -> Enum.valueOf(enumClass, str))
                .collect(Collectors.toList());
    }
}
