package dev.frankheijden.insights.api.utils;

import java.util.regex.Pattern;

public class VersionUtils {

    private static final Pattern integerPattern = Pattern.compile("[^0-9]");

    private VersionUtils() {}

    /**
     * Compares two versions in X.X.X format.
     * Returns true if version is newer than the old one.
     * @param oldVersion The old version.
     * @param newVersion The new version.
     * @return true iff new version is newer than old version.
     */
    public static boolean isNewVersion(String oldVersion, String newVersion) {
        if (oldVersion == null || newVersion == null) return false;
        String[] oldVersionSplit = oldVersion.split("-")[0].split("\\.");
        String[] newVersionSplit = newVersion.split("-")[0].split("\\.");

        int i = 0;
        while (i < oldVersionSplit.length && i < newVersionSplit.length) {
            int o = extractInteger(oldVersionSplit[i]);
            int n = extractInteger(newVersionSplit[i]);
            if (i != oldVersionSplit.length - 1 && i != newVersionSplit.length - 1) {
                if (n < o) return false;
            }
            if (n > o) return true;
            i++;
        }
        return false;
    }

    private static int extractInteger(String str) {
        return Integer.parseInt(integerPattern.matcher(str).replaceAll(""));
    }
}
