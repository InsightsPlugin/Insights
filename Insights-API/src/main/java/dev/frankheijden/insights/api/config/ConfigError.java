package dev.frankheijden.insights.api.config;

import java.util.ArrayList;
import java.util.List;

public class ConfigError {

    private final String fileName;
    private final String path;
    private final String error;

    /**
     * Constructs a new ConfigError using given parameters.
     */
    public ConfigError(String fileName, String path, String error) {
        this.fileName = fileName;
        this.path = path;
        this.error = error;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    @Override
    public String toString() {
        return "File '" + fileName + "' at path '" + path + "': " + error;
    }

    public static final class Builder {

        private final List<ConfigError> errors;

        public Builder() {
            this.errors = new ArrayList<>();
        }

        public Builder append(String fileName, String path, String error) {
            return append(new ConfigError(fileName, path, error));
        }

        public Builder append(ConfigError error) {
            this.errors.add(error);
            return this;
        }

        public List<ConfigError> getErrors() {
            return errors;
        }
    }
}
