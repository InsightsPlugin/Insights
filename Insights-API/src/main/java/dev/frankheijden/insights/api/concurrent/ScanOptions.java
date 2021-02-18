package dev.frankheijden.insights.api.concurrent;

public final class ScanOptions {

    private static ScanOptions ALL = new ScanOptions(true, true, true);
    private static ScanOptions NONE = new ScanOptions(false, false, false);

    private final boolean save;
    private final boolean track;
    private final boolean entities;

    private ScanOptions(boolean save, boolean track, boolean entities) {
        this.save = save;
        this.track = track;
        this.entities = entities;
    }

    public static ScanOptions all() {
        return ALL;
    }

    public static ScanOptions none() {
        return NONE;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public boolean save() {
        return save;
    }

    public boolean track() {
        return track;
    }

    public boolean entities() {
        return entities;
    }

    public static class Builder {

        private boolean save = false;
        private boolean track = false;
        private boolean entities = false;

        public Builder() {}

        public Builder save() {
            this.save = true;
            return this;
        }

        public Builder track() {
            this.track = true;
            return this;
        }

        public Builder entities() {
            this.entities = true;
            return this;
        }

        public ScanOptions build() {
            return new ScanOptions(save, track, entities);
        }
    }
}
