package dev.frankheijden.insights.api.util;

import dev.frankheijden.insights.api.objects.chunk.ChunkLocation;
import dev.frankheijden.insights.api.objects.chunk.ChunkPart;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.Map;
import org.bukkit.World;

public class LazyChunkPartRadiusIterator implements Iterator<ChunkPart>, Iterable<ChunkPart> {

    private static final EnumMap<Direction, Direction> nextDirections = new EnumMap<>(Map.of(
            Direction.NORTH, Direction.EAST,
            Direction.EAST, Direction.SOUTH,
            Direction.SOUTH, Direction.WEST,
            Direction.WEST, Direction.NORTH
    ));

    private final World world;
    private final int chunkCount;
    private int currentChunkCount;
    private int currentChunkX;
    private int currentChunkZ;
    private Direction currentDirection;
    private int currentEdge;
    private int currentEdgeStep;
    private int currentEdgeStepMax;

    /**
     * Constructs a new LazyChunkPartRadiusIterator.
     */
    public LazyChunkPartRadiusIterator(World world, int chunkX, int chunkZ, int radius) {
        this.world = world;

        int edge = (2 * radius) + 1;
        this.chunkCount = edge * edge;

        this.currentChunkCount = 0;
        this.currentChunkX = chunkX;
        this.currentChunkZ = chunkZ;
        this.currentDirection = Direction.NORTH;
        this.currentEdge = 0;
        this.currentEdgeStep = 0;
        this.currentEdgeStepMax = 1;
    }

    @Override
    public boolean hasNext() {
        return currentChunkCount < chunkCount;
    }

    @Override
    public ChunkPart next() {
        @SuppressWarnings("VariableDeclarationUsageDistance")
        ChunkPart part = new ChunkLocation(world, currentChunkX, currentChunkZ).toPart();

        currentChunkCount++;
        currentChunkX += switch (currentDirection) {
            case EAST -> 1;
            case WEST -> -1;
            default -> 0;
        };
        currentChunkZ += switch (currentDirection) {
            case NORTH -> 1;
            case SOUTH -> -1;
            default -> 0;
        };

        if (++currentEdgeStep >= currentEdgeStepMax) {
            currentEdgeStep = 0;
            currentDirection = nextDirections.get(currentDirection);
            if (++currentEdge == 2) {
                currentEdge = 0;
                currentEdgeStepMax++;
            }
        }

        return part;
    }

    @Override
    public Iterator<ChunkPart> iterator() {
        return this;
    }

    public int getChunkCount() {
        return chunkCount;
    }

    enum Direction {
        NORTH,
        EAST,
        SOUTH,
        WEST,
    }
}
