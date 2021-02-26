package dev.frankheijden.insights.api.objects.wrappers;

import dev.frankheijden.insights.api.utils.MaterialUtils;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import java.util.Collection;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public abstract class ScanObject<T extends Enum<T>> {

    private final T object;

    protected ScanObject(T object) {
        this.object = object;
    }

    public static MaterialObject of(Material material) {
        return new MaterialObject(material);
    }

    public static EntityObject of(EntityType entityType) {
        return new EntityObject(entityType);
    }

    /**
     * Converts an object into a ScanObject.
     * @throws IllegalArgumentException iff conversion from the given object failed.
     */
    public static ScanObject<?> of(Object item) {
        if (item instanceof Material) {
            return new MaterialObject((Material) item);
        } else if (item instanceof EntityType) {
            return new EntityObject((EntityType) item);
        } else {
            throw new IllegalArgumentException("Unknown ScanObject of type " + item.getClass());
        }
    }

    /**
     * Converts a set of objects into a set of ScanObjects.
     * @throws IllegalArgumentException iff conversion from the given object(s) failed.
     */
    public static Set<ScanObject<?>> of(Collection<? extends Object> items) {
        Set<ScanObject<?>> scanObjects = new HashSet<>(items.size());
        for (Object item : items) {
            scanObjects.add(of(item));
        }
        return scanObjects;
    }

    /**
     * Parses the given string to a ScanObject.
     * @throws IllegalArgumentException iff parsing of the given object failed.
     */
    public static ScanObject<?> parse(String str) {
        String upperCased = str.toUpperCase(Locale.ENGLISH);
        try {
            Material material = Material.valueOf(upperCased);
            if (MaterialUtils.BLOCKS.contains(material)) {
                return of(material);
            }
        } catch (IllegalArgumentException ignored) {
            //
        }

        try {
            return of(EntityType.valueOf(upperCased));
        } catch (IllegalArgumentException ignored) {
            //
        }
        throw new IllegalArgumentException("Unknown ScanObject '" + str + "'");
    }

    public T getObject() {
        return object;
    }

    public String name() {
        return object.name();
    }

    public abstract Type getType();

    public enum Type {
        MATERIAL,
        ENTITY
    }

    public static final class MaterialObject extends ScanObject<Material> {

        protected MaterialObject(Material object) {
            super(object);
        }

        @Override
        public Type getType() {
            return Type.MATERIAL;
        }
    }

    public static final class EntityObject extends ScanObject<EntityType> {

        protected EntityObject(EntityType object) {
            super(object);
        }

        @Override
        public Type getType() {
            return Type.ENTITY;
        }
    }
}
