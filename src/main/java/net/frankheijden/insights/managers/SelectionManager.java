package net.frankheijden.insights.managers;

import net.frankheijden.insights.entities.Selection;
import net.frankheijden.insights.utils.MessageUtils;
import org.bukkit.Location;

import java.text.NumberFormat;
import java.util.*;

public class SelectionManager {

    private static SelectionManager instance;

    private final Set<UUID> selectionMode;
    private final Map<UUID, Selection> dataMap;

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
        Selection selection = getSelection(uuid);
        boolean success = selection.setPos1(pos1);
        setSelection(uuid, selection);

        if (message && success) {
            sendMessage(uuid, "first");
        }
    }

    public void setPos2(UUID uuid, Location pos2, boolean message) {
        Selection selection = getSelection(uuid);
        boolean success = selection.setPos2(pos2);
        setSelection(uuid, selection);

        if (message && success) {
            sendMessage(uuid, "second");
        }
    }

    public void sendMessage(UUID uuid, String what) {
        Selection selection = getSelection(uuid);
        String data = "";
        if (selection.isValid()) {
            data = MessageUtils.getMessage("messages.selection.create.data",
                    "%chunks%", NumberFormat.getInstance().format(selection.getChunkCount()),
                    "%blocks%", NumberFormat.getInstance().format(selection.getBlockCount()));
        }
        MessageUtils.sendMessage(uuid,"messages.selection.create." + what,
                "%data%", data);
    }

    public void setSelection(UUID uuid, Selection selection) {
        dataMap.put(uuid, selection);
    }

    public Selection getSelection(UUID uuid) {
        Selection selection = dataMap.get(uuid);
        if (selection == null) return new Selection(null, null);
        return selection;
    }
}
