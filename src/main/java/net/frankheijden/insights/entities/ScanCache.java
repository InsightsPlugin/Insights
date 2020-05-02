package net.frankheijden.insights.entities;

import java.util.*;

public class ScanCache {

    private final Selection selection;
    private final Map<String, Integer> scanResult;

    public ScanCache(Selection selection) {
        this.selection = selection;
        this.scanResult = new TreeMap<>();
    }

    public ScanCache(Selection selection, ScanResult scanResult) {
        this.selection = selection;
        this.scanResult = scanResult.getCounts();
    }

    public Selection getSelection() {
        return selection;
    }

    private String getKey(String what) {
        return what.toUpperCase();
    }

    public boolean isOverLimit(String what, int limit, int d) {
        Integer count = scanResult.get(getKey(what));
        if (count == null) return false;
        return (count + d) > limit;
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
