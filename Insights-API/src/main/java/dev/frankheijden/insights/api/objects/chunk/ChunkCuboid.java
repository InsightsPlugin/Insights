package dev.frankheijden.insights.api.objects.chunk;

public class ChunkCuboid {

    public static final ChunkCuboid MAX = new ChunkCuboid(ChunkVector.MIN, ChunkVector.MAX);

    private final ChunkVector min;
    private final ChunkVector max;

    public ChunkCuboid(ChunkVector min, ChunkVector max) {
        this.min = min;
        this.max = max;
    }

    public ChunkVector getMin() {
        return min;
    }

    public ChunkVector getMax() {
        return max;
    }
}
