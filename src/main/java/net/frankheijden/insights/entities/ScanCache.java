package net.frankheijden.insights.entities;

import java.util.*;

public class ScanCache {

    private final SelectionEntity selection;
    private final Map<String, Integer> scanResult;

    public ScanCache(SelectionEntity selection) {
        this(selection, new ScanResult());
    }

    public ScanCache(SelectionEntity selection, ScanResult scanResult) {
        this.selection = selection;
        this.scanResult = scanResult.getCounts();
    }

    public SelectionEntity getSelectionEntity() {
        return selection;
    }

    private String getKey(String what) {
        return what.toUpperCase();
    }

    public Integer getCount(String what) {
        return scanResult.get(getKey(what));
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
