package dev.frankheijden.insights.nms.core;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ReflectionUtils {

    private ReflectionUtils() {}

    /**
     * Finds a declared field in given class.
     */
    public static Field findDeclaredField(Class<?> clazz, Class<?> type, String name) {
        for (Field field : clazz.getDeclaredFields()) {
            if (field.getType().equals(type)) {
                field.setAccessible(true);
                return field;
            }
        }

        throw new IllegalStateException("Can't find field " + clazz.getName() + "#" + name);
    }

    /**
     * Finds a declared method in given class.
     */
    public static Method findDeclaredMethod(
            Class<?> clazz,
            Class<?>[] paramTypes,
            Class<?> returnType,
            String name
    ) {
        for (Method method : clazz.getDeclaredMethods()) {
            if (!method.getReturnType().equals(returnType)) continue;
            if (!Arrays.equals(paramTypes, method.getParameterTypes())) continue;

            method.setAccessible(true);
            return method;
        }

        throw new IllegalStateException("Can't find method " + clazz.getName() + "." + name + "");
    }

    /**
     * Retrieves all methods with given annotation.
     */
    public static List<Method> getAnnotatedMethods(Class<?> clazz, Class<? extends Annotation> annotationClass) {
        Method[] declaredMethods = clazz.getDeclaredMethods();
        List<Method> methods = new ArrayList<>(declaredMethods.length);
        for (Method method : declaredMethods) {
            if (method.isAnnotationPresent(annotationClass)) {
                methods.add(method);
            }
        }
        return methods;
    }
}
