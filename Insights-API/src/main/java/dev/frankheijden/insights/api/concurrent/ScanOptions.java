package dev.frankheijden.insights.api.concurrent;

public final class ScanOptions {

    private static final ScanOptions ALL = new ScanOptions(true, true, true, true);
    private static final ScanOptions SCAN = new ScanOptions(false, false, true, true);
    private static final ScanOptions MATERIALS = new ScanOptions(false, false, true, false);
    private static final ScanOptions ENTITIES = new ScanOptions(false, false, false, true);

    private final boolean save;
    private final boolean track;
    private final boolean materials;
    private final boolean entities;

    private ScanOptions(boolean save, boolean track, boolean materials, boolean entities) {
        this.save = save;
        this.track = track;
        this.materials = materials;
        this.entities = entities;
    }

    public static ScanOptions all() {
        return ALL;
    }

    public static ScanOptions scanOnly() {
        return SCAN;
    }

    public static ScanOptions materialsOnly() {
        return MATERIALS;
    }

    public static ScanOptions entitiesOnly() {
        return ENTITIES;
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

    public boolean materials() {
        return materials;
    }

    public boolean entities() {
        return entities;
    }

    public static class Builder {

        private boolean save = false;
        private boolean track = false;
        private boolean materials = false;
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

        public Builder materials() {
            this.materials = true;
            return this;
        }

        public Builder entities() {
            this.entities = true;
            return this;
        }

        public ScanOptions build() {
            return new ScanOptions(save, track, materials, entities);
        }
    }
}
