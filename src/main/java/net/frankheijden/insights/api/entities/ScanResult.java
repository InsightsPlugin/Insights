package net.frankheijden.insights.api.entities;

import java.util.*;

public class ScanResult implements Iterable<Map.Entry<String, Integer>> {
    private final TreeMap<String, Integer> counts;

    /**
     * Initializes ScanResult with empty map
     */
    public ScanResult() {
        this.counts = new TreeMap<>();
    }

    /**
     * Initializes ScanResult with pre-initialised items.
     * All entries will be set to 0. This is for "Custom"
     * scan, to notify user item count was 0.
     *
     * @param materials Scan materials
     * @param entityTypes Scan entities
     */
    public ScanResult(List<String> materials, List<String> entityTypes) {
        this.counts = new TreeMap<>();

        if (materials != null) initialize(materials);
        if (entityTypes != null) initialize(entityTypes);
    }

    private void initialize(List<String> values) {
        values.forEach(v -> counts.put(v, 0));
    }

    public void increment(String str) {
        counts.merge(str, 1, Integer::sum);
    }

    public TreeMap<String, Integer> getCounts() {
        return counts;
    }

    public int getSize() {
        return counts.size();
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public Iterator<Map.Entry<String, Integer>> iterator() {
        return counts.entrySet().iterator();
    }
}
