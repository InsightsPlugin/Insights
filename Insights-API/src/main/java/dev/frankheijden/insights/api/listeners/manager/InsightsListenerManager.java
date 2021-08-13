package dev.frankheijden.insights.api.listeners.manager;

import java.lang.reflect.Method;
import java.util.Map;

public interface InsightsListenerManager {

    void register();

    void unregister();

    Map<String, Method> getAllowedDisableMethods();

    Map<String, Method> getAllowedPriorityOverrideMethods();
}
