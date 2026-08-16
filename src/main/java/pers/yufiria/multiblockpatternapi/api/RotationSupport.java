package pers.yufiria.multiblockpatternapi.api;

import pers.yufiria.multiblockpatternapi.util.BlockVector;

/**
 * 旋转和镜像变换支持接口。
 */
public interface RotationSupport {

    /**
     * 旋转方向
     */
    enum Rotation {
        /** 北（0°） */
        NORTH(0),
        /** 东（90°） */
        EAST(90),
        /** 南（180°） */
        SOUTH(180),
        /** 西（270°） */
        WEST(270);

        private final int degrees;

        Rotation(int degrees) {
            this.degrees = degrees;
        }

        /**
         * 获取旋转角度
         *
         * @return 角度值
         */
        public int getDegrees() {
            return degrees;
        }
    }

    /**
     * 镜像方向
     */
    enum Mirror {
        /** 无镜像 */
        NONE,
        /** X轴镜像（南北翻转） */
        X,
        /** Y轴镜像（东西翻转） */
        Y
    }

    /**
     * 对偏移量应用旋转和镜像变换
     *
     * @param vector 原始偏移量
     * @param rotation 旋转方向
     * @param mirror 镜像方向
     * @return 变换后的偏移量
     */
    BlockVector applyTransform(BlockVector vector, Rotation rotation, Mirror mirror);
}
