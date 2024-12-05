package dev.frankheijden.insights.api.utils;

public class ClassUtils {

    private ClassUtils() {}

    public static boolean isClassLoaded(String className) {
        try {
            Class.forName(className);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}
