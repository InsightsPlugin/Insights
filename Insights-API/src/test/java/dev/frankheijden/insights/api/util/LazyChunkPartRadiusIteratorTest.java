package dev.frankheijden.insights.api.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import dev.frankheijden.insights.api.objects.chunk.ChunkLocation;
import dev.frankheijden.insights.api.objects.chunk.ChunkPart;
import org.bukkit.World;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;
import java.util.stream.Stream;

class LazyChunkPartRadiusIteratorTest {

    private static final Random random = new Random();

    @ParameterizedTest(name = "Chunk[{0}, {1}], Radius = {2}")
    @MethodSource("radiusGenerator")
    void determineChunkParts(int chunkX, int chunkZ, int radius) {
        World world = Mockito.mock(World.class);

        int edge = (2 * radius) + 1;
        int chunkCount = edge * edge;
        List<ChunkPart> expectedChunkParts = new ArrayList<>(chunkCount);
        for (int x = chunkX - radius; x <= chunkX + radius; x++) {
            for (int z = chunkZ - radius; z <= chunkZ + radius; z++) {
                expectedChunkParts.add(new ChunkLocation(world, x, z).toPart());
            }
        }

        List<ChunkPart> actualChunkParts = new ArrayList<>(chunkCount);
        LazyChunkPartRadiusIterator it = new LazyChunkPartRadiusIterator(world, chunkX, chunkZ, radius);
        while (it.hasNext()) {
            actualChunkParts.add(it.next());

            if (actualChunkParts.size() > expectedChunkParts.size()) {
                fail("Expected ChunkPart count exceeded of " + expectedChunkParts.size());
            }
        }

        assertThat(actualChunkParts).containsExactlyInAnyOrderElementsOf(expectedChunkParts);
    }

    private static Stream<Arguments> radiusGenerator() {
        return Stream.concat(
                Stream.of(
                        Arguments.of(0, 0, 10)
                ),
                IntStream.rangeClosed(0, 20)
                        .mapToObj(r -> Arguments.of(random.nextInt(10000), random.nextInt(10000), r))
        );
    }
}
