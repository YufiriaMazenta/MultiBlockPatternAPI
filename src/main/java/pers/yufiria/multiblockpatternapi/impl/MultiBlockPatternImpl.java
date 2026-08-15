package pers.yufiria.multiblockpatternapi.impl;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import pers.yufiria.multiblockpatternapi.api.MatchResult;
import pers.yufiria.multiblockpatternapi.api.MultiBlockPattern;
import pers.yufiria.multiblockpatternapi.api.PatternAction;
import pers.yufiria.multiblockpatternapi.api.RotationSupport;
import pers.yufiria.multiblockpatternapi.util.BlockVector;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public class MultiBlockPatternImpl implements MultiBlockPattern {

    private final String id;
    private final String displayName;
    private final Map<BlockVector, Character> offsetCharMap;
    private final Map<Character, Object> charMap;
    private final List<PatternAction> actions;
    private final boolean rotationEnabled;
    private final Direction direction;
    private final Map<Character, Material> materialMap;
    private final Map<Character, Predicate<Block>> predicateMap;
    private final Map<Character, String> predicateDescMap;
    private final Material triggerMaterial;
    private final BlockVector triggerOffset;

    public MultiBlockPatternImpl(
        String id,
        String displayName,
        Map<BlockVector, Character> offsetCharMap,
        Map<Character, Object> charMap,
        List<PatternAction> actions,
        boolean rotationEnabled,
        Direction direction,
        Map<Character, Material> materialMap,
        Map<Character, Predicate<Block>> predicateMap,
        Map<Character, String> predicateDescMap,
        Material triggerMaterial,
        BlockVector triggerOffset
    ) {
        this.id = id;
        this.displayName = displayName;
        this.offsetCharMap = offsetCharMap;
        this.charMap = charMap;
        this.actions = actions;
        this.rotationEnabled = rotationEnabled;
        this.direction = direction;
        this.materialMap = materialMap;
        this.predicateMap = predicateMap;
        this.predicateDescMap = predicateDescMap;
        this.triggerMaterial = triggerMaterial;
        this.triggerOffset = triggerOffset;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    @Override
    public Map<BlockVector, Character> getOffsetCharMap() {
        return offsetCharMap;
    }

    @Override
    public Map<Character, Object> getCharMap() {
        return charMap;
    }

    @Override
    public List<PatternAction> getActions() {
        return actions;
    }

    @Override
    public boolean isRotationEnabled() {
        return rotationEnabled;
    }

    @Override
    public Direction getDirection() {
        return direction;
    }

    @Override
    public boolean hasTrigger() {
        return triggerMaterial != null && triggerOffset != null;
    }

    @Override
    public Material getTriggerMaterial() {
        return triggerMaterial;
    }

    @Override
    public BlockVector getTriggerOffset() {
        return triggerOffset;
    }

    @Override
    public MatchResult checkMatch(Location origin) {
        return checkMatch(origin, RotationSupport.Rotation.NORTH, RotationSupport.Mirror.NONE);
    }

    @Override
    public MatchResult checkMatch(Location origin, RotationSupport.Rotation rotation, RotationSupport.Mirror mirror) {
        return PatternMatcherImpl.getInstance().match(this, origin, rotation, mirror);
    }

    public Map<Character, Material> getMaterialMap() {
        return materialMap;
    }

    public Map<Character, Predicate<Block>> getPredicateMap() {
        return predicateMap;
    }

    public Map<Character, String> getPredicateDescMap() {
        return predicateDescMap;
    }
}
