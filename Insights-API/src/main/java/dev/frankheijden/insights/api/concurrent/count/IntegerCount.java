package dev.frankheijden.insights.api.concurrent.count;

public class IntegerCount {

    private final int[] counts;
    private int total = 0;

    public IntegerCount(int size) {
        this.counts = new int[size];
    }

    public int getTotal() {
        return total;
    }

    /**
     * Increments the count at given position.
     */
    public int increment(int pos) {
        counts[pos] += 1;
        total += 1;

        return total;
    }

    public void reset(int pos) {
        total -= counts[pos];
        counts[pos] = 0;
    }
}
