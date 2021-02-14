package dev.frankheijden.insights.api.addons;

import dev.frankheijden.insights.api.InsightsPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.event.Listener;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.logging.Level;

public class AddonManager {

    private static final DirectoryStream.Filter<Path> addonsFilter = path -> Files.isRegularFile(path)
            && path.getFileName().toString().endsWith(".jar");

    private final InsightsPlugin plugin;
    private final Path addonsFolder;
    private final Map<String, InsightsAddon> addons;

    /**
     * Constructs a new AddonManager.
     */
    public AddonManager(InsightsPlugin plugin, Path addonsFolder) {
        this.plugin = plugin;
        this.addonsFolder = addonsFolder;
        this.addons = new HashMap<>();
    }

    /**
     * Attempts to create the addons folder if it doesn't exist.
     */
    public void createAddonsFolder() throws IOException {
        if (!Files.isDirectory(addonsFolder)) {
            Files.createDirectory(addonsFolder);
        }
    }

    /**
     * Loads all addons from the addons directory.
     */
    public void loadAddons() throws IOException {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(addonsFolder, addonsFilter)) {
            for (Path path : stream) {
                InsightsAddon addon;
                try {
                    addon = loadAddon(path);
                } catch (IOException ex) {
                    plugin.getLogger().log(
                            Level.SEVERE,
                            ex,
                            () -> "Error loading addon '" + path.getFileName().toString() + "'"
                    );
                    continue;
                }

                if (!InsightsPlugin.getInstance().isAvailable(addon.getPluginName())) {
                    plugin.getLogger().severe("Error loading addon: " + addon.getPluginName() + " is not enabled!");
                    continue;
                }

                if (addon instanceof Listener) {
                    Bukkit.getPluginManager().registerEvents((Listener) addon, plugin);
                    plugin.getLogger().info("Registered listener of addon '" + addon.getPluginName() + "'");
                }

                this.addons.put(addon.getPluginName(), addon);
                plugin.getLogger().info("Loaded addon '" + addon.getPluginName() + "' v" + addon.getVersion());
            }
        }
    }

    public InsightsAddon loadAddon(Path path) throws AddonException, MalformedURLException {
        return loadAddon(path.toUri().toURL());
    }

    /**
     * Loads an addon from given URL.
     */
    public InsightsAddon loadAddon(URL url) throws AddonException {
        ClassLoader classLoader = getClass().getClassLoader();
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
                    if (InsightsAddon.class.isAssignableFrom(clazz)) {
                        return newAddonInstance(clazz);
                    }
                }
                entry = in.getNextJarEntry();
            }
        } catch (Exception ex) {
            throw new AddonException(ex);
        }
        return null;
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

    public InsightsAddon getAddon(String pluginName) {
        return addons.get(pluginName);
    }

    /**
     * Looks up a region at given location provided by the loaded addons.
     */
    public Optional<Region> getRegion(Location location) {
        for (InsightsAddon addon : addons.values()) {
            Optional<Region> regionOptional = addon.getRegion(location);
            if (regionOptional.isPresent()) return regionOptional;
        }
        return Optional.empty();
    }
}
