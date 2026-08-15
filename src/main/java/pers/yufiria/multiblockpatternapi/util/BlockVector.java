package pers.yufiria.multiblockpatternapi.util;

import java.util.Objects;

public final class BlockVector {

    private final int x;
    private final int y;
    private final int z;

    public BlockVector(int x, int y, int z) {
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

    public BlockVector add(BlockVector other) {
        return new BlockVector(x + other.x, y + other.y, z + other.z);
    }

    public BlockVector subtract(BlockVector other) {
        return new BlockVector(x - other.x, y - other.y, z - other.z);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BlockVector that)) return false;
        return x == that.x && y == that.y && z == that.z;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, z);
    }

    @Override
    public String toString() {
        return x + "," + y + "," + z;
    }
}
