package net.frankheijden.insights.entities;

import org.bukkit.Location;

import java.util.List;

public class Area {

    private final CacheAssistant assistant;
    private final String id;
    private final List<CuboidSelection> selections;

    private Area(CacheAssistant assistant, String id, List<CuboidSelection> selections) {
        this.assistant = assistant;
        this.id = id;
        this.selections = selections;
    }

    public static Area from(CacheAssistant assistant, String id, List<CuboidSelection> selections) {
        if (selections.isEmpty()) return null;
        return new Area(assistant, id, selections);
    }

    public CacheAssistant getAssistant() {
        return assistant;
    }

    public String getId() {
        return id;
    }

    public List<CuboidSelection> getSelections() {
        return selections;
    }
}
