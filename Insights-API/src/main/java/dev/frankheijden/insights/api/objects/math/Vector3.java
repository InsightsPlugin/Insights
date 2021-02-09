package dev.frankheijden.insights.api.objects.math;

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

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
    }
}
