package pers.yufiria.multiblockpatternapi.impl;

import pers.yufiria.multiblockpatternapi.api.RotationSupport;
import pers.yufiria.multiblockpatternapi.util.BlockVector;

public class SimpleRotationSupport implements RotationSupport {

    private static final SimpleRotationSupport INSTANCE = new SimpleRotationSupport();

    public static SimpleRotationSupport getInstance() {
        return INSTANCE;
    }

    @Override
    public BlockVector applyTransform(BlockVector vector, Rotation rotation, Mirror mirror) {
        int x = vector.x();
        int y = vector.y();
        int z = vector.z();

        // Apply rotation (on XZ plane, Y unchanged)
        switch (rotation) {
            case EAST: {
                // 90° clockwise: (x,z) -> (z,-x)
                int temp = x;
                x = z;
                z = -temp;
                break;
            }
            case SOUTH: {
                // 180°: (x,z) -> (-x,-z)
                x = -x;
                z = -z;
                break;
            }
            case WEST: {
                // 270° clockwise (90° CCW): (x,z) -> (-z,x)
                int temp = x;
                x = -z;
                z = temp;
                break;
            }
            case NORTH:
            default:
                // 0° - no change
                break;
        }

        // Apply mirror
        switch (mirror) {
            case X:
                z = -z;
                break;
            case Y:
                x = -x;
                break;
            case NONE:
            default:
                break;
        }

        return new BlockVector(x, y, z);
    }
}
