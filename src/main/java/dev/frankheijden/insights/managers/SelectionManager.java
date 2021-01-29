package dev.frankheijden.insights.managers;

import dev.frankheijden.insights.entities.CuboidSelection;
import dev.frankheijden.insights.utils.MessageUtils;
import org.bukkit.Location;

import java.text.NumberFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class SelectionManager {

    private static SelectionManager instance;

    private final Set<UUID> selectionMode;
    private final Map<UUID, CuboidSelection> dataMap;

    public SelectionManager() {
        instance = this;
        this.selectionMode = new HashSet<>();
        this.dataMap = new HashMap<>();
    }

    public static SelectionManager getInstance() {
        return instance;
    }

    public void setSelecting(UUID uuid) {
        selectionMode.add(uuid);
    }

    public boolean isSelecting(UUID uuid) {
        return selectionMode.contains(uuid);
    }

    public void removeSelecting(UUID uuid) {
        selectionMode.remove(uuid);
    }

    public void setPos1(UUID uuid, Location pos1, boolean message) {
        CuboidSelection selection = getSelection(uuid);
        boolean success = selection.setPos1(pos1);
        setSelection(uuid, selection);

        if (message && success) {
            sendMessage(uuid, "first");
        }
    }

    public void setPos2(UUID uuid, Location pos2, boolean message) {
        CuboidSelection selection = getSelection(uuid);
        boolean success = selection.setPos2(pos2);
        setSelection(uuid, selection);

        if (message && success) {
            sendMessage(uuid, "second");
        }
    }

    public void sendMessage(UUID uuid, String what) {
        CuboidSelection selection = getSelection(uuid);
        String data = "";
        if (selection.isValid()) {
            data = MessageUtils.getMessage("messages.selection.create.data",
                    "%chunks%", NumberFormat.getInstance().format(selection.getChunkCount()),
                    "%blocks%", NumberFormat.getInstance().format(selection.getBlockCount()));
        }
        MessageUtils.sendMessage(uuid,"messages.selection.create." + what,
                "%data%", data);
    }

    public void setSelection(UUID uuid, CuboidSelection selection) {
        dataMap.put(uuid, selection);
    }

    public CuboidSelection getSelection(UUID uuid) {
        CuboidSelection selection = dataMap.get(uuid);
        if (selection == null) return new CuboidSelection();
        return selection;
    }
}
