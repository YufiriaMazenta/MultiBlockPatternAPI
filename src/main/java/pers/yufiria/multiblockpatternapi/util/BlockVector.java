package pers.yufiria.multiblockpatternapi.util;

import org.jetbrains.annotations.NotNull;

/**
 * 不可变的三维向量，用于表示方块偏移量。
 *
 * @param x X轴偏移
 * @param y Y轴偏移
 * @param z Z轴偏移
 */
public record BlockVector(int x, int y, int z) {

    /**
     * 向量加法
     *
     * @param other 另一个向量
     * @return 新的向量
     */
    public BlockVector add(BlockVector other) {
        return new BlockVector(x + other.x, y + other.y, z + other.z);
    }

    /**
     * 向量减法
     *
     * @param other 另一个向量
     * @return 新的向量
     */
    public BlockVector subtract(BlockVector other) {
        return new BlockVector(x - other.x, y - other.y, z - other.z);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BlockVector(int x1, int y1, int z1))) return false;
        return x == x1 && y == y1 && z == z1;
    }

    @Override
    public @NotNull String toString() {
        return x + "," + y + "," + z;
    }

}
