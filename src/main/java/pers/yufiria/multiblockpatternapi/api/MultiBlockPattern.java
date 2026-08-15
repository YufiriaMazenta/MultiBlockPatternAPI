package pers.yufiria.multiblockpatternapi.api;

import org.bukkit.Location;
import org.bukkit.Material;
import pers.yufiria.multiblockpatternapi.util.BlockVector;

import java.util.List;
import java.util.Map;

public interface MultiBlockPattern {

    enum Direction {
        HORIZONTAL,  // XZ平面（默认）
        VERTICAL     // XY平面（竖直方向）
    }

    String getId();
    String getDisplayName();
    Map<BlockVector, Character> getOffsetCharMap();
    Map<Character, Object> getCharMap();
    List<PatternAction> getActions();
    boolean isRotationEnabled();
    Direction getDirection();

    boolean hasTrigger();
    Material getTriggerMaterial();
    BlockVector getTriggerOffset();

    MatchResult checkMatch(Location origin);
    MatchResult checkMatch(Location origin, RotationSupport.Rotation rotation, RotationSupport.Mirror mirror);
}
