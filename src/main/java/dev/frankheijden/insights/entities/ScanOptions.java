package dev.frankheijden.insights.entities;

import dev.frankheijden.insights.interfaces.ScanCompleteListener;
import dev.frankheijden.insights.enums.ScanType;
import dev.frankheijden.insights.utils.CaseInsensitiveHashSet;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;

public class ScanOptions {
    private ScanType scanType;
    private World world;
    private Queue<PartialChunk> partials;
    private int chunkCount;
    private UUID uuid;
    private String path;
    private Set<String> materials;
    private Set<String> entityTypes;
    private boolean console;
    private boolean saveWorld;
    private ScanCompleteListener listener;

    public ScanOptions() {
        this.scanType = null;
        this.world = null;
        this.partials = new LinkedList<>();
        this.chunkCount = 0;
        this.uuid = null;
        this.path = null;
        this.materials = new CaseInsensitiveHashSet();
        this.entityTypes = new CaseInsensitiveHashSet();
        this.console = false;
        this.saveWorld = false;
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

    public Set<String> getMaterials() {
        return materials;
    }

    public void setMaterials(Collection<? extends String> materials) {
        this.materials = new CaseInsensitiveHashSet(materials);
    }

    public void addMaterial(String material) {
        materials.add(material);
    }

    public void addAllMaterials(Collection<? extends String> materials) {
        this.materials.addAll(materials);
    }

    public Set<String> getEntityTypes() {
        return entityTypes;
    }

    public void setEntityTypes(Collection<? extends String> entityTypes) {
        this.entityTypes = new CaseInsensitiveHashSet(entityTypes);
    }

    public void addEntityType(String entityType) {
        entityTypes.add(entityType);
    }

    public void addAllEntityTypes(Collection<? extends String> entityTypes) {
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
