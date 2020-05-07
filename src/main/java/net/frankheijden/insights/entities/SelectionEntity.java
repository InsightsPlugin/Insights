package net.frankheijden.insights.entities;

import org.bukkit.Location;

public class SelectionEntity extends Selection {

    private final CacheAssistant assistant;

    public SelectionEntity(Location pos1, Location pos2, CacheAssistant assistant) {
        super(pos1, pos2);
        this.assistant = assistant;
    }

    public static SelectionEntity from(Selection selection, CacheAssistant assistant) {
        if (selection == null) return null;
        return new SelectionEntity(selection.getPos1(), selection.getPos2(), assistant);
    }

    public CacheAssistant getAssistant() {
        return assistant;
    }
}
