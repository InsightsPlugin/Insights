package net.frankheijden.insights.entities;

import net.frankheijden.insights.config.Limit;

import java.util.*;

public class ScanCache {

    private final Area selection;
    private final Map<String, Integer> scanResult;

    public ScanCache(Area selection) {
        this(selection, new ScanResult());
    }

    public ScanCache(Area selection, ScanResult scanResult) {
        this.selection = selection;
        this.scanResult = scanResult.getCounts();
    }

    public Area getSelectionEntity() {
        return selection;
    }

    private String getKey(String what) {
        return what.toUpperCase();
    }

    public Integer getCount(String what) {
        return scanResult.get(getKey(what));
    }

    public Integer getCount(Limit limit) {
        int count = 0;
        for (String m : limit.getMaterials()) {
            count += scanResult.getOrDefault(getKey(m), 0);
        }
        for (String e : limit.getEntities()) {
            count += scanResult.getOrDefault(getKey(e), 0);
        }

        return count == 0 ? null : count;
    }

    public void updateCache(String what, int d) {
        String key = getKey(what);
        Integer count = scanResult.get(key);
        if (count == null) count = 0;
        count += d;
        if (count < 0) count = 0;
        scanResult.put(key, count);
    }
}
