package dev.frankheijden.insights.utils;

import dev.frankheijden.insights.Insights;
import dev.frankheijden.insights.entities.CacheAssistant;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

public class FileUtils {

    private static final Insights plugin = Insights.getInstance();
    public static final String ADDONS_DIRECTORY_NAME = "addons";

    public static void createInsightsDirectoriesIfNotExists() {
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }

        File addonsDir = new File(plugin.getDataFolder(), ADDONS_DIRECTORY_NAME);
        if (!addonsDir.exists()) {
            addonsDir.mkdirs();
        }
    }

    public static File copyResourceIfNotExists(String resource) {
        createInsightsDirectoriesIfNotExists();

        File file = new File(plugin.getDataFolder(), resource);
        if (!file.exists()) {
            Insights.logger.info(String.format("'%s' not found, creating!", resource));
            plugin.saveResource(resource, false);
        }
        return file;
    }

    public static File createFileIfNotExists(String fileName) {
        createInsightsDirectoriesIfNotExists();

        File file = new File(plugin.getDataFolder(), fileName);
        if (!file.exists()) {
            Insights.logger.info(String.format("'%s' not found, creating!", fileName));
            try {
                file.createNewFile();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return file;
    }

    public static List<Class<?>> loadAllAddons() {
        List<Class<?>> classes = new ArrayList<>();
        File[] files = getAddons();
        for (File file : files) {
            try {
                loadAddon(file, classes);
            } catch (MalformedURLException ex) {
                ex.printStackTrace();
            }
        }
        return classes;
    }

    public static void loadAddon(File file, List<Class<?>> classes) throws MalformedURLException {
        loadAddon(file.toURI().toURL(), classes);
    }

    public static void loadAddon(URL url, List<Class<?>> classes) {
        Class<?> clazz = CacheAssistant.class;
        try (URLClassLoader loader = new URLClassLoader(new URL[]{ url }, clazz.getClassLoader());
             JarInputStream in = new JarInputStream(url.openStream())) {

            JarEntry entry = in.getNextJarEntry();
            while (entry != null) {
                String name = entry.getName();
                if (name == null || name.isEmpty()) {
                    continue;
                }

                if (name.endsWith(".class")) {
                    name = name.replace("/", ".");
                    String cname = name.substring(0, name.lastIndexOf(".class"));

                    Class<?> c = loader.loadClass(cname);
                    if (clazz.isAssignableFrom(c)) {
                        classes.add(c);
                    }
                }

                entry = in.getNextJarEntry();
            }
        } catch (Exception ex) {
            //
        }
    }

    public static File[] getAddons() {
        createInsightsDirectoriesIfNotExists();
        File addonsDir = new File(plugin.getDataFolder(), ADDONS_DIRECTORY_NAME);
        return addonsDir.listFiles((File pathname) -> pathname.getName().endsWith(".jar"));
    }
}
