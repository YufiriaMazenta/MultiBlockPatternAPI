package pers.yufiria.multiblockpatternapi.api;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;

import java.util.List;
import java.util.Map;

public interface MatchResult {
    boolean isMatch();
    MultiBlockPattern getPattern();
    Location getOrigin();
    RotationSupport.Rotation getRotation();
    List<Block> getMatchedBlocks();

    void execute();
    void destroy(boolean dropItems);
    void transform(Map<Material, Material> materialMap);
}
