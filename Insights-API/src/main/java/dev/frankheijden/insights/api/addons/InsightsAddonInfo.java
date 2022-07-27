package dev.frankheijden.insights.api.addons;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface InsightsAddonInfo {

    /**
     * Unique ID of the addon (must be lowercase).
     */
    String addonId();

    /**
     * Default name of the addon's areas.
     */
    String defaultAreaName() default "";

    /**
     * Version of the addon.
     */
    String version();

    /**
     * Authors of the addon.
     */
    String[] authors();

    /**
     * Plugin name dependencies.
     */
    String[] depends();

}
