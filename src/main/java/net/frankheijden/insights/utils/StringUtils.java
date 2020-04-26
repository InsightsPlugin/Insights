package net.frankheijden.insights.utils;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class StringUtils {

    public static boolean matches(Collection<String> regexStrings, String str) {
        return matchesWith(regexStrings, str) != null;
    }

    public static String matchesWith(Collection<String> regexStrings, String str) {
        for (String regex : regexStrings) {
            if (str.matches(regex)) {
                return regex;
            }
        }
        return null;
    }

    public static String capitalizeName(String name) {
        name = name.toLowerCase();
        StringBuilder stringBuilder = new StringBuilder();
        for (String entry : name.split("[_ ]")) {
            stringBuilder.append(org.apache.commons.lang.StringUtils.capitalize(entry.toLowerCase())).append(" ");
        }
        String build = stringBuilder.toString();
        return build.substring(0, build.length() - 1);
    }

    public static boolean isNewVersion(String oldVersion, String newVersion) {
        String[] oldVersionSplit = oldVersion.split("\\.");
        String[] newVersionSplit = newVersion.split("\\.");

        int i = 0;
        while (i < oldVersionSplit.length && i < newVersionSplit.length) {
            int o = Integer.parseInt(oldVersionSplit[i]);
            int n = Integer.parseInt(newVersionSplit[i]);
            if (i != oldVersionSplit.length - 1 && i != newVersionSplit.length - 1) {
                if (n < o) return false;
            }
            if (n > o) return true;
            i++;
        }
        return false;
    }

    public static List<String> lowercase(Collection<? extends String> collection) {
        return collection.stream()
                .map(String::toLowerCase)
                .collect(Collectors.toList());
    }
}
