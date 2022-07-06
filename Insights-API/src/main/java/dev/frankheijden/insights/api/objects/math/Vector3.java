package dev.frankheijden.insights.api.objects.math;

import java.util.Objects;

public class Vector3 {

    protected final int x;
    protected final int y;
    protected final int z;

    /**
     * Constructs a new vector with given 3D coordinates.
     */
    public Vector3(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public int x() {
        return x;
    }

    public int y() {
        return y;
    }

    public int z() {
        return z;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Vector3 vector3 = (Vector3) o;
        return x == vector3.x && y == vector3.y && z == vector3.z;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, z);
    }

    @Override
    public String toString() {
        return "(" + x + ", " + y + ", " + z + ")";
    }
}
