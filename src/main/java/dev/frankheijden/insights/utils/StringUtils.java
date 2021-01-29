package dev.frankheijden.insights.utils;

import java.util.Arrays;
import java.util.Collection;

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
        StringBuilder stringBuilder = new StringBuilder();
        for (String entry : name.toLowerCase().split("[_ ]")) {
            stringBuilder.append(org.apache.commons.lang.StringUtils.capitalize(entry)).append(" ");
        }
        return stringBuilder.substring(0, stringBuilder.length() - 1);
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

    public static String repeat(char c, int count) {
        char[] repeat = new char[count];
        Arrays.fill(repeat, c);
        return new String(repeat);
    }
}
