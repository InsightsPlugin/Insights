package dev.frankheijden.insights.api.config;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Monad<T> {

    private final T object;
    private final List<ConfigError> errors;

    public Monad(T object, List<ConfigError> errors) {
        this.object = object;
        this.errors = errors;
    }

    public T getObject() {
        return object;
    }

    public List<ConfigError> getErrors() {
        return errors;
    }

    /**
     * Completes the monad, logging any errors, if any.
     */
    public T exceptionally(Logger logger) {
        for (ConfigError error : errors) {
            logger.log(Level.SEVERE, error.toString());
        }
        return object;
    }
}
