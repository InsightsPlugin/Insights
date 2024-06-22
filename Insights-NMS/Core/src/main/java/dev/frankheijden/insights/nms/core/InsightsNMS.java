package dev.frankheijden.insights.nms.core;

import org.bukkit.Chunk;
import org.bukkit.World;
import java.io.IOException;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class InsightsNMS {

    protected final String CHUNK_ERROR = "Recoverable errors when loading section [%d, %d, %d]: %s";
    protected static Logger logger = Logger.getLogger("Insights");

    /**
     * Gets an InsightsNMS instance for given version.
     * TODO: can be yeeted later or reused if we decide to do backwards compatibility again
     */
    @SuppressWarnings("unchecked")
    public static <T extends InsightsNMS> T get() {
        try {
            Class<?> clazz = Class.forName("dev.frankheijden.insights.nms.current.InsightsNMSImpl");
            return (T) clazz.getDeclaredConstructor().newInstance();
        } catch (ReflectiveOperationException e) {
            logger.log(Level.SEVERE, "Unable to get InsightsNMSImpl", e);
            throw new RuntimeException(e);
        }
    }

    public abstract void getLoadedChunkSections(Chunk chunk, Consumer<ChunkSection> sectionConsumer);

    public abstract void getUnloadedChunkSections(
            World world,
            int chunkX,
            int chunkZ,
            Consumer<ChunkSection> sectionConsumer
    );

    public abstract void getLoadedChunkEntities(Chunk chunk, Consumer<ChunkEntity> entityConsumer);

    public abstract void getUnloadedChunkEntities(
            World world,
            int chunkX,
            int chunkZ,
            Consumer<ChunkEntity> entityConsumer
    ) throws IOException;
}
