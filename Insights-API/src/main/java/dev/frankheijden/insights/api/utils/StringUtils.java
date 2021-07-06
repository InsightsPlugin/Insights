package dev.frankheijden.insights.api.utils;

import java.text.NumberFormat;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtils {

    private static final Pattern placeholderPattern = Pattern.compile("%[0-9a-zA-Z]+%");

    private StringUtils() {}

    /**
     * Efficient method for small placeholder replacements.
     */
    public static String replaceSmall(String str, String... replacements) {
        if (replacements.length % 2 != 0) throw new IllegalArgumentException("Must be a multiple of two");
        for (int i = 0; i < replacements.length; i += 2) {
            str = str.replace('%' + replacements[i] + '%', replacements[i + 1]);
        }
        return str;
    }

    public static String replace(String str, String... replacements) {
        return replacements.length < 10 ? replaceSmall(str, replacements) : replace(str, MapUtils.toMap(replacements));
    }

    /**
     * Efficient method for large placeholder replacements.
     */
    public static String replace(String str, Map<String, String> replacements) {
        StringBuffer sb = new StringBuffer(str.length());
        Matcher matcher = placeholderPattern.matcher(str);
        while (matcher.find()) {
            String group = matcher.group();
            matcher.appendReplacement(sb, replacements.getOrDefault(group.substring(1, group.length() - 1), group));
        }
        return sb.toString();
    }

    public static String pretty(long n) {
        return NumberFormat.getIntegerInstance().format(n);
    }

    /**
     * Pretty Duration string.
     * Adapted from https://stackoverflow.com/a/40487511
     */
    public static String pretty(Duration duration) {
        return duration.toString()
                .substring(2)
                .replaceAll("(\\d[HMS])(?!$)", "$1 ")
                .toLowerCase();
    }

    public static String prettyOneDecimal(double d) {
        return String.format("%,.1f", d);
    }

    /**
     * Finds strings in the collection that starts with the given input.
     * Note: collection must only contain LOWERCASE values.
     */
    public static List<String> findThatStartsWith(Collection<? extends String> collection, String input) {
        List<String> strings = new ArrayList<>();
        input = input.toLowerCase(Locale.ENGLISH);
        for (String str : collection) {
            if (str.startsWith(input)) {
                strings.add(str);
            }
        }
        return strings;
    }

    /**
     * Capitalizes a sentence (each word first letter uppercase).
     */
    public static String capitalizeSentence(String str) {
        StringBuilder sb = new StringBuilder(str.length());
        for (String s : str.split(" ")) {
            sb.append(' ').append(capitalize(s));
        }
        return sb.substring(1);
    }

    /**
     * Capitalizes given string, which must be in LOWERCASE.
     */
    public static String capitalize(String str) {
        return str.substring(0, 1).toUpperCase(Locale.ENGLISH) + str.substring(1);
    }

    public static String join(String[] strings, String delimiter, int fromIndex) {
        return join(strings, delimiter, fromIndex, strings.length);
    }

    /**
     * Joins the given string with a delimiter, starting at fromIndex (including) until toIndex (excluding).
     */
    public static String join(String[] strings, String delimiter, int fromIndex, int toIndex) {
        StringBuilder sb = new StringBuilder();
        for (int i = fromIndex; i < toIndex; i++) {
            sb.append(delimiter).append(strings[i]);
        }
        return sb.length() == 0 ? "" : sb.substring(delimiter.length());
    }
}
