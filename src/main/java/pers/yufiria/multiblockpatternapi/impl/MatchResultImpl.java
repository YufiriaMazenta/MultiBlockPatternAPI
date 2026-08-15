package pers.yufiria.multiblockpatternapi.impl;

import crypticlib.CrypticLib;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import pers.yufiria.multiblockpatternapi.api.MatchResult;
import pers.yufiria.multiblockpatternapi.api.MultiBlockPattern;
import pers.yufiria.multiblockpatternapi.api.RotationSupport;

import java.util.List;
import java.util.Map;

public class MatchResultImpl implements MatchResult {

    private final boolean isMatch;
    private final MultiBlockPattern pattern;
    private final Location origin;
    private final RotationSupport.Rotation rotation;
    private final List<Block> matchedBlocks;

    public MatchResultImpl(
        boolean isMatch,
        MultiBlockPattern pattern,
        Location origin,
        RotationSupport.Rotation rotation,
        List<Block> matchedBlocks
    ) {
        this.isMatch = isMatch;
        this.pattern = pattern;
        this.origin = origin.clone();
        this.rotation = rotation;
        this.matchedBlocks = List.copyOf(matchedBlocks);
    }

    @Override
    public boolean isMatch() {
        return isMatch;
    }

    @Override
    public MultiBlockPattern getPattern() {
        return pattern;
    }

    @Override
    public Location getOrigin() {
        return origin.clone();
    }

    @Override
    public RotationSupport.Rotation getRotation() {
        return rotation;
    }

    @Override
    public List<Block> getMatchedBlocks() {
        return matchedBlocks;
    }

    @Override
    public void execute() {
        CrypticLib.debug("[MBP] Executing " + pattern.getActions().size() + " actions for pattern: " + pattern.getId());
        for (var action : pattern.getActions()) {
            CrypticLib.debug("[MBP] Executing action: " + action.getClass().getSimpleName());
            action.onMatch(this);
        }
        CrypticLib.debug("[MBP] Actions executed.");
    }

    @Override
    public void destroy(boolean dropItems) {
        StructureHandlerImpl.getInstance().destroy(this, dropItems);
    }

    @Override
    public void transform(Map<Material, Material> materialMap) {
        StructureHandlerImpl.getInstance().transform(this, materialMap);
    }
}
