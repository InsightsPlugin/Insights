package dev.frankheijden.insights.api.exceptions;

import java.io.IOException;

public class ChunkIOException extends RuntimeException {

    public ChunkIOException(IOException cause) {
        super(cause);
    }
}
