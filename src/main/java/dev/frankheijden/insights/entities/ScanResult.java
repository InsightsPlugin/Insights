package dev.frankheijden.insights.entities;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

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
    public ScanResult(Collection<? extends String> materials, Collection<? extends String> entityTypes) {
        this.counts = new TreeMap<>();

        if (materials != null) initialize(materials);
        if (entityTypes != null) initialize(entityTypes);
    }

    private void initialize(Collection<? extends String> values) {
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

    public int getTotalCount() {
        return counts.values().stream()
                .mapToInt(Integer::intValue)
                .sum();
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public Iterator<Map.Entry<String, Integer>> iterator() {
        return counts.entrySet().iterator();
    }
}
