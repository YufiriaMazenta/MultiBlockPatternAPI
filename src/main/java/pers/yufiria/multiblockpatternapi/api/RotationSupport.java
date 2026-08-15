package pers.yufiria.multiblockpatternapi.api;

import pers.yufiria.multiblockpatternapi.util.BlockVector;

public interface RotationSupport {

    enum Rotation {
        NORTH(0),
        EAST(90),
        SOUTH(180),
        WEST(270);

        private final int degrees;

        Rotation(int degrees) {
            this.degrees = degrees;
        }

        public int getDegrees() {
            return degrees;
        }
    }

    enum Mirror {
        NONE,
        X,
        Y
    }

    BlockVector applyTransform(BlockVector vector, Rotation rotation, Mirror mirror);
}
