package dev.frankheijden.insights.api.addons;

import dev.frankheijden.insights.api.InsightsPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.event.Listener;
import java.io.Closeable;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.logging.Level;

public class AddonManager {

    private static final DirectoryStream.Filter<Path> addonsFilter = path -> Files.isRegularFile(path)
            && path.getFileName().toString().endsWith(".jar");

    private final InsightsPlugin plugin;
    private final Path addonsPath;
    private final Map<String, InsightsAddonContainer> addons;

    /**
     * Constructs a new AddonManager.
     */
    public AddonManager(InsightsPlugin plugin, Path addonsPath) {
        this.plugin = plugin;
        this.addonsPath = addonsPath;
        this.addons = new HashMap<>();
    }

    /**
     * Attempts to create the addons folder if it doesn't exist.
     */
    public void createAddonsFolder() throws IOException {
        if (!Files.isDirectory(addonsPath)) {
            Files.createDirectory(addonsPath);
        }
    }

    /**
     * Registers all addons from the addons directory.
     */
    public void registerAddons() throws IOException {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(addonsPath, addonsFilter)) {
            addonLoop:
            for (Path path : stream) {
                try {
                    InsightsAddonContainer container = loadAddon(path);

                    String addonId = container.addonInfo().addonId();
                    for (String pluginName : container.addonInfo().depends()) {
                        if (!InsightsPlugin.getInstance().isAvailable(pluginName)) {
                            plugin.getLogger().severe(
                                    "Error registering addon '" + addonId + "': " + pluginName + " is not enabled!"
                            );
                            continue addonLoop;
                        }
                    }

                    container.addon().init(plugin, container, addonsPath);
                    container.addon().enable();
                    this.addons.put(addonId, container);

                    if (container instanceof Listener) {
                        Bukkit.getPluginManager().registerEvents((Listener) container, plugin);
                        plugin.getLogger().info("Registered listener of addon '" + addonId + "'");
                    }

                    String version = container.addonInfo().version();
                    if (version.isBlank()) {
                        version = "<unknown>";
                    }

                    String authors = String.join(", ", container.addonInfo().authors());
                    if (authors.isBlank()) {
                        authors = "<unknown>";
                    }
                    plugin.getLogger().info(
                            "Registered addon '" + addonId + "' v" + version + " by " + authors
                    );
                } catch (Throwable th) {
                    plugin.getLogger().log(
                            Level.SEVERE,
                            "Unable to register addon '" + path.getFileName().toString() + "'",
                            th
                    );
                }
            }
        }
    }

    public void unregisterAddons() {
        new ArrayList<>(this.addons.keySet()).forEach(this::unregisterAddon);
    }

    /**
     * Unregisters an addon.
     */
    public void unregisterAddon(String addonId) {
        InsightsAddonContainer container = this.addons.remove(addonId);
        String version = container.addonInfo().version();
        try {
            container.addon().disable();
        } catch (Throwable th) {
            plugin.getLogger().log(
                    Level.SEVERE,
                    "Error occurred while disabling addon '" + addonId + "'",
                    th
            );
        }

        ClassLoader loader = container.addon().getClass().getClassLoader();
        if (loader instanceof Closeable closeable) {
            try {
                closeable.close();
            } catch (IOException ex) {
                plugin.getLogger().log(
                        Level.SEVERE,
                        "Error while unregistering addon '" + addonId + "'",
                        ex
                );
            }
        }

        plugin.getLogger().info("Unregistered addon '" + addonId + "' v" + version);
    }

    public InsightsAddonContainer loadAddon(Path path) throws AddonException, MalformedURLException {
        return loadAddon(path.toUri().toURL());
    }

    /**
     * Loads an addon from given URL.
     */
    public InsightsAddonContainer loadAddon(URL url) throws AddonException {
        ClassLoader classLoader = getClass().getClassLoader();
        InsightsAddonInfo info = null;
        InsightsAddon addon = null;
        try (
                URLClassLoader loader = new URLClassLoader(new URL[]{ url }, classLoader);
                JarInputStream in = new JarInputStream(url.openStream())
        ) {
            JarEntry entry = in.getNextJarEntry();
            while (entry != null) {
                String name = entry.getName();

                if (name.endsWith(".class")) {
                    name = name.replace("/", ".");
                    String className = name.substring(0, name.lastIndexOf(".class"));
                    Class<?> clazz = loader.loadClass(className);

                    InsightsAddonInfo addonInfo = clazz.getAnnotation(InsightsAddonInfo.class);
                    if (addonInfo != null) {
                        info = addonInfo;
                    }

                    if (InsightsAddon.class.isAssignableFrom(clazz)) {
                        addon = newAddonInstance(clazz);
                    }
                }
                entry = in.getNextJarEntry();
            }
        } catch (Exception ex) {
            throw new AddonException(ex);
        }

        String errorMsg = "";
        if (info == null) {
            errorMsg += "Could not find a class annotated with @" + InsightsAddonInfo.class.getName() + ".";
        }
        if (addon == null) {
            if (!errorMsg.isBlank()) errorMsg += " ";
            errorMsg += "Could not find a class that implements " + InsightsAddon.class.getName() + ".";
        }
        if (!errorMsg.isBlank()) {
            throw new AddonException(errorMsg);
        }
        return new InsightsAddonContainer(info, addon);
    }

    private InsightsAddon newAddonInstance(Class<?> clazz) throws AddonException {
        Constructor<?>[] constructors;
        try {
            constructors = clazz.getConstructors();
        } catch (Exception ex) {
            throw new AddonException(ex);
        }

        for (Constructor<?> constructor : constructors) {
            if (constructor.getParameterCount() == 0) {
                try {
                    return (InsightsAddon) constructor.newInstance();
                } catch (Exception ex) {
                    throw new AddonException(ex);
                }
            }
        }
        throw new AddonException('\'' + clazz.getName() + "' does not have a default constructor!");
    }

    public InsightsAddonContainer addonContainer(String addonId) {
        return addons.get(addonId);
    }

    /**
     * Looks up regions at a given location provided by the loaded addons.
     */
    public List<AddonRegion> regionsAt(Location location) {
        List<AddonRegion> addonRegions = new ArrayList<>();
        for (InsightsAddonContainer container : addons.values()) {
            addonRegions.addAll(container.addon().regionsAt(location));
        }
        return addonRegions;
    }
}
