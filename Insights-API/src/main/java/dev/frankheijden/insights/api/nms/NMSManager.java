package dev.frankheijden.insights.api.nms;

import dev.frankheijden.minecraftreflection.MinecraftReflection;
import dev.frankheijden.minecraftreflection.exceptions.MinecraftReflectionException;

public class NMSManager {

    private final NMSChunkFactory factory;

    public NMSManager(NMSChunkFactory factory) {
        this.factory = factory;
    }

    public NMSChunkFactory getFactory() {
        return factory;
    }

    public static NMSManager init() throws MinecraftReflectionException {
        return new NMSManager(MinecraftReflection.of("dev.frankheijden.insights.nms.ChunkFactoryImpl").newInstance());
    }
}
