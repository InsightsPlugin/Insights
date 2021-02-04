package dev.frankheijden.insights.api.utils;

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
}
