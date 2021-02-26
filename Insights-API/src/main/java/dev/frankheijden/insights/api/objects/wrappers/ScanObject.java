package dev.frankheijden.insights.api.objects.wrappers;

import dev.frankheijden.insights.api.utils.Constants;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import java.util.Collection;
import java.util.HashSet;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

public abstract class ScanObject<T extends Enum<T>> {

    private final T object;
    private final Type type;

    protected ScanObject(T object, Type type) {
        this.object = object;
        this.type = type;
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
     * Creates a new Set of ScanObjects based on given materials and entities.
     */
    public static Set<ScanObject<?>> of(
            Collection<? extends Material> materials,
            Collection<? extends EntityType> entities
    ) {
        Set<ScanObject<?>> scanObjects = new HashSet<>(materials.size() + entities.size());
        for (Material item : materials) {
            scanObjects.add(of(item));
        }
        for (EntityType item : entities) {
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
            if (Constants.BLOCKS.contains(material)) {
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

    public Type getType() {
        return type;
    }

    public String name() {
        return object.name();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ScanObject<?> that = (ScanObject<?>) o;
        return object.equals(that.object) && type == that.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(object, type);
    }

    public enum Type {
        MATERIAL,
        ENTITY
    }

    public static final class MaterialObject extends ScanObject<Material> {

        protected MaterialObject(Material object) {
            super(object, Type.MATERIAL);
        }
    }

    public static final class EntityObject extends ScanObject<EntityType> {

        protected EntityObject(EntityType object) {
            super(object, Type.ENTITY);
        }
    }
}
