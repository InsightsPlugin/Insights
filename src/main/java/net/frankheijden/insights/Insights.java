package net.frankheijden.insights;

import io.papermc.lib.PaperLib;
import net.frankheijden.insights.commands.*;
import net.frankheijden.insights.config.Config;
import net.frankheijden.insights.entities.Error;
import net.frankheijden.insights.listeners.*;
import net.frankheijden.insights.managers.*;
import net.frankheijden.insights.placeholders.InsightsPlaceholderAPIExpansion;
import net.frankheijden.insights.tasks.UpdateCheckerTask;
import net.frankheijden.insights.utils.*;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.lang.reflect.*;
import java.text.NumberFormat;
import java.util.*;
import java.util.stream.Collectors;

public class Insights extends JavaPlugin {

    private static Insights insights;

    private FileConfiguration messages;

    private Config config;
    private SQLite sqLite;

    private NMSManager nmsManager = null;
    private NotificationManager notificationManager = null;
    private WorldEditManager worldEditManager = null;
    private WorldGuardManager worldGuardManager = null;
    private HookManager hookManager = null;
    private ScanManager scanManager = null;
    private SelectionManager selectionManager = null;
    private CacheManager cacheManager = null;
    private VersionManager versionManager = null;
    private MetricsManager metricsManager = null;
    private FreezeManager freezeManager = null;

    private boolean placeholderAPIHook = false;

    @Override
    public void onEnable() {
        long start = System.currentTimeMillis();
        insights = this;

        PaperLib.suggestPaper(this);

        List<Error> errors = new ArrayList<>();
        setupConfiguration(errors);
        setupSQLite();
        setupManagers();
        setupClasses();
        setupPlaceholderAPIHook();
        registerAllAddons(errors);

        if (!errors.isEmpty()) {
            MessageUtils.printErrors(errors, false);
        }

        checkForUpdates();

        long end = System.currentTimeMillis();
        long millis = end - start;
        Bukkit.getLogger().info("[Insights] Enabled Insights in "
                + NumberFormat.getInstance().format(millis) + "ms!");
    }

    @Override
    public void onDisable() {
        super.onDisable();
        unregisterCommands();
    }

    public static Insights getInstance() {
        return insights;
    }

    private void setupConfiguration(List<Error> errors) {
        config = new Config();
        config.reload(errors);

        File messagesFile = FileUtils.copyResourceIfNotExists("messages.yml");
        messages = YamlConfiguration.loadConfiguration(messagesFile);
    }

    private void setupSQLite() {
        sqLite = new SQLite();
        sqLite.load();
    }

    private void setupClasses() {
        InteractListener interactListener = new InteractListener();
        Bukkit.getPluginManager().registerEvents(interactListener, this);
        MainListener mainListener = new MainListener(interactListener);
        Bukkit.getPluginManager().registerEvents(mainListener, this);
        Bukkit.getPluginManager().registerEvents(new EntityListener(mainListener), this);
        if (nmsManager.isPost1_13()) {
            Bukkit.getPluginManager().registerEvents(new Post1_13Listeners(), this);
        } else {
            Bukkit.getPluginManager().registerEvents(new Pre1_13Listeners(mainListener), this);
        }
        Bukkit.getPluginManager().registerEvents(new CacheListener(), this);
        Bukkit.getPluginManager().registerEvents(new FreezeListener(), this);
        Objects.requireNonNull(this.getCommand("autoscan")).setExecutor(new CommandAutoscan());
        Objects.requireNonNull(this.getCommand("insights")).setExecutor(new CommandInsights());
        Objects.requireNonNull(this.getCommand("check")).setExecutor(new CommandCheck());
        Objects.requireNonNull(this.getCommand("checkworlds")).setExecutor(new CommandCheckworlds());
        Objects.requireNonNull(this.getCommand("scan")).setExecutor(new CommandScan());
        Objects.requireNonNull(this.getCommand("scanradius")).setExecutor(new CommandScanradius());
        Objects.requireNonNull(this.getCommand("scanworld")).setExecutor(new CommandScanworld());
        Objects.requireNonNull(this.getCommand("selection")).setExecutor(new CommandSelection());
        Objects.requireNonNull(this.getCommand("togglecheck")).setExecutor(new CommandTogglecheck());
        Objects.requireNonNull(this.getCommand("cancelscan")).setExecutor(new CommandCancelscan());
    }

    public boolean isAvailable(String pluginName) {
        Plugin plugin = Bukkit.getPluginManager().getPlugin(pluginName);
        return plugin != null && plugin.isEnabled();
    }

    private void setupManagers() {
        nmsManager = new NMSManager();

        if (nmsManager.isPost1_9()) {
            notificationManager = new NotificationManager();
            notificationManager.start();
        }

        hookManager = new HookManager();

        if (isAvailable("WorldEdit")) {
            worldEditManager = new WorldEditManager();
            Bukkit.getLogger().info("[Insights] Successfully hooked into WorldEdit!");
        }

        if (isAvailable("WorldGuard")) {
            worldGuardManager = new WorldGuardManager();
            Bukkit.getLogger().info("[Insights] Successfully hooked into WorldGuard!");
        }

        scanManager = new ScanManager();
        selectionManager = new SelectionManager();
        cacheManager = new CacheManager();
        versionManager = new VersionManager();
        metricsManager = new MetricsManager();
        freezeManager = new FreezeManager();

        String version = String.format("1.%d.%d", PaperLib.getMinecraftVersion(), PaperLib.getMinecraftPatchVersion());
        if (PaperLib.getMinecraftVersion() <= 7) {
            Bukkit.getLogger().warning("[Insights] Minecraft version '" + version + "' detected, "
                    + "please note that Insights may not support this version!");
        } else {
            Bukkit.getLogger().info("[Insights] Minecraft version '" + version + "' detected!");
        }
    }

    private void setupPlaceholderAPIHook() {
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            InsightsPlaceholderAPIExpansion expansion = new InsightsPlaceholderAPIExpansion();
            if (expansion.register()) {
                Bukkit.getLogger().info("[Insights] Successfully hooked into PlaceholderAPI!");
            }

            placeholderAPIHook = expansion.isRegistered();
            if (!placeholderAPIHook) {
                Bukkit.getLogger().warning("[Insights] Couldn't hook into PlaceholderAPI.");
            }
        }
    }

    private void registerAllAddons(List<Error> errors) {
        cacheManager.unregisterAllAddons();
        cacheManager.registerAllAddons(errors, FileUtils.loadAllAddons().stream()
                .map(c -> ReflectionUtils.createCacheAssistant(errors, c))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet()));
    }

    private void checkForUpdates() {
        if (config.GENERAL_UPDATES_CHECK) {
            UpdateCheckerTask.start(Bukkit.getConsoleSender(), true);
        }
    }

    public List<Error> reload() {
        List<Error> errors = new ArrayList<>();
        setupConfiguration(errors);
        setupSQLite();
        setupPlaceholderAPIHook();
        registerAllAddons(errors);
        return errors;
    }

    public FileConfiguration getMessages() {
        return messages;
    }

    public Config getConfiguration() {
        return config;
    }

    public SQLite getSqLite() {
        return sqLite;
    }

    public NMSManager getNMSManager() {
        return nmsManager;
    }

    public NotificationManager getNotificationManager() {
        return notificationManager;
    }

    public WorldEditManager getWorldEditManager() {
        return worldEditManager;
    }

    public WorldGuardManager getWorldGuardManager() {
        return worldGuardManager;
    }

    public HookManager getHookManager() {
        return hookManager;
    }

    public ScanManager getScanManager() {
        return scanManager;
    }

    public SelectionManager getSelectionManager() {
        return selectionManager;
    }

    public CacheManager getCacheManager() {
        return cacheManager;
    }

    public VersionManager getVersionManager() {
        return versionManager;
    }

    public MetricsManager getMetricsManager() {
        return metricsManager;
    }

    public FreezeManager getFreezeManager() {
        return freezeManager;
    }

    public boolean hasPlaceholderAPIHook() {
        return placeholderAPIHook;
    }

    private void unregisterCommands() {
        Map<String, Command> map;
        try {
            map = getKnownCommands();
        } catch (Exception ex) {
            ex.printStackTrace();
            return;
        }
        this.getDescription().getCommands().keySet().forEach(map::remove);
    }

    /*
     * These methods need to be in this class due to
     * unloading of the plugin when disabling.
     */
    public static SimpleCommandMap getCommandMap() throws NoSuchFieldException, IllegalAccessException {
        Field commandMap = Bukkit.getServer().getClass().getDeclaredField("commandMap");
        commandMap.setAccessible(true);
        return (SimpleCommandMap) commandMap.get(Bukkit.getServer());
    }

    public static Map<String, Command> getKnownCommands() throws NoSuchFieldException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        return getKnownCommands(getCommandMap());
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Command> getKnownCommands(SimpleCommandMap map) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        Object mapObject;
        try {
            Field knownCommands = map.getClass().getDeclaredField("knownCommands");
            knownCommands.setAccessible(true);
            mapObject = knownCommands.get(map);
        } catch (NoSuchFieldException ex) {
            Method getKnownCommands = map.getClass().getDeclaredMethod("getKnownCommands");
            mapObject = getKnownCommands.invoke(map);
        }
        return (Map<String, Command>) mapObject;
    }
}
