package net.frankheijden.insights.utils;

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
}
