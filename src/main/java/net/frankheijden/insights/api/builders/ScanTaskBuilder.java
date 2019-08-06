package net.frankheijden.insights.api.builders;

import net.frankheijden.insights.Insights;
import net.frankheijden.insights.api.entities.ChunkLocation;
import net.frankheijden.insights.api.enums.ScanType;
import net.frankheijden.insights.api.interfaces.ScanCompleteEventListener;
import net.frankheijden.insights.tasks.LoadChunksTask;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

public class ScanTaskBuilder {
    private Insights plugin;
    private ScanType scanType;
    private World world;
    private List<ChunkLocation> chunkLocations;
    private UUID uuid = null;
    private String path = null;
    private List<Material> materials = null;
    private List<EntityType> entityTypes = null;
    private boolean console = false;
    private boolean saveWorld = false;
    private boolean debug = true;
    private ScanCompleteEventListener listener = null;

    public ScanTaskBuilder(Insights plugin, ScanType scanType, World world, List<ChunkLocation> chunkLocations) {
        this.plugin = plugin;
        this.scanType = scanType;
        this.world = world;
        this.chunkLocations = chunkLocations;
    }

    public ScanTaskBuilder setCommandSenderAndPath(CommandSender sender, String path) {
        if (sender instanceof Player) {
            this.uuid = ((Player) sender).getUniqueId();
        } else {
            this.setConsole(true);
        }
        this.path = path;
        return this;
    }

    public ScanTaskBuilder setUUIDAndPath(UUID uuid, String path) {
        this.uuid = uuid;
        this.path = path;
        return this;
    }

    public ScanTaskBuilder setMaterials(List<Material> materials) {
        if (materials != null && !materials.isEmpty()) {
            this.materials = materials;
        }
        return this;
    }

    public ScanTaskBuilder setEntityTypes(List<EntityType> entityTypes) {
        if (entityTypes != null && !entityTypes.isEmpty()) {
            this.entityTypes = entityTypes;
        }
        return this;
    }

    public ScanTaskBuilder setConsole(boolean console) {
        this.console = console;
        return this;
    }

    public ScanTaskBuilder setSaveWorld(boolean saveWorld) {
        this.saveWorld = saveWorld;
        return this;
    }

    public ScanTaskBuilder setDebug(boolean debug) {
        this.debug = debug;
        return this;
    }

    public ScanTaskBuilder setScanCompleteEventListener(ScanCompleteEventListener listener) {
        this.listener = listener;
        return this;
    }

    public LoadChunksTask build() {
        if (((materials == null || materials.isEmpty()) && (entityTypes == null || entityTypes.isEmpty())) && scanType == ScanType.CUSTOM) {
            scanType = ScanType.ALL;
        }
        return new LoadChunksTask(plugin, scanType, world, chunkLocations, uuid, path, materials, entityTypes, console, saveWorld, debug, listener);
    }
}
