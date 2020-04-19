package net.frankheijden.insights.entities;

import net.frankheijden.insights.enums.ScanType;
import net.frankheijden.insights.interfaces.ScanCompleteListener;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;

public class ScanOptions {
    private ScanType scanType;
    private World world;
    private Queue<PartialChunk> partials;
    private int chunkCount;
    private UUID uuid;
    private String path;
    private List<String> materials;
    private List<String> entityTypes;
    private boolean console;
    private boolean saveWorld;
    private boolean debug;
    private ScanCompleteListener listener;

    public ScanOptions() {
        this.scanType = null;
        this.world = null;
        this.partials = new LinkedList<>();
        this.chunkCount = 0;
        this.uuid = null;
        this.path = null;
        this.materials = new ArrayList<>();
        this.entityTypes = new ArrayList<>();
        this.console = false;
        this.saveWorld = false;
        this.debug = false;
        this.listener = null;
    }

    public ScanType getScanType() {
        return scanType;
    }

    public void setScanType(ScanType scanType) {
        this.scanType = scanType;
    }

    public World getWorld() {
        return world;
    }

    public void setWorld(World world) {
        this.world = world;
    }

    public Queue<PartialChunk> getPartialChunks() {
        return partials;
    }

    public void setPartialChunks(Collection<? extends PartialChunk> partials) {
        this.partials = new LinkedList<>(partials);
        this.chunkCount = getPartialChunksSize();
    }

    public void addPartialChunk(PartialChunk partial) {
        this.partials.add(partial);
        this.chunkCount++;
    }

    public void addAllPartialChunks(Collection<? extends PartialChunk> partials) {
        this.partials.addAll(partials);
        this.chunkCount = getPartialChunksSize();
    }

    public int getChunkCount() {
        return chunkCount;
    }

    public int getPartialChunksSize() {
        return partials.size();
    }

    public boolean hasUUID() {
        return uuid != null;
    }

    public UUID getUUID() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public List<String> getMaterials() {
        return materials;
    }

    public void setMaterials(List<String> materials) {
        this.materials = materials;
    }

    public void addMaterial(String material) {
        materials.add(material);
    }

    public void addAllMaterials(List<String> materials) {
        this.materials.addAll(materials);
    }

    public List<String> getEntityTypes() {
        return entityTypes;
    }

    public void setEntityTypes(List<String> entityTypes) {
        this.entityTypes = entityTypes;
    }

    public void addEntityType(String entityType) {
        entityTypes.add(entityType);
    }

    public void addAllEntityTypes(List<String> entityTypes) {
        this.entityTypes.addAll(entityTypes);
    }

    public boolean isConsole() {
        return console;
    }

    public void setConsole(boolean console) {
        this.console = console;
    }

    public boolean shouldSaveWorld() {
        return saveWorld;
    }

    public void setSaveWorld(boolean saveWorld) {
        this.saveWorld = saveWorld;
    }

    public boolean isDebug() {
        return debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public ScanCompleteListener getListener() {
        return listener;
    }

    public void setListener(ScanCompleteListener listener) {
        this.listener = listener;
    }

    public void setCommandSenderAndPath(CommandSender sender, String path) {
        if (sender instanceof Player) {
            this.uuid = ((Player) sender).getUniqueId();
        } else {
            this.console = true;
        }
        this.path = path;
    }

    public void setUUIDAndPath(UUID uuid, String path) {
        this.uuid = uuid;
        this.path = path;
    }
}
