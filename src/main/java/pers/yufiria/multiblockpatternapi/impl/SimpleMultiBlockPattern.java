package pers.yufiria.multiblockpatternapi.impl;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import pers.yufiria.multiblockpatternapi.api.*;
import pers.yufiria.multiblockpatternapi.util.BlockVector;

import java.util.List;
import java.util.Map;
import java.util.function.BiPredicate;

public class SimpleMultiBlockPattern implements MultiBlockPattern {

    private final String id;
    private final String displayName;
    private final Map<BlockVector, Character> offsetCharMap;
    private final Map<Character, BlockMatcher> charMatcherMap;
    private final List<Action> actions;
    private final boolean rotationEnabled;
    private final Direction direction;
    private final char triggerChar;
    private final BlockMatcher triggerMatcher;
    private final BlockVector triggerOffset;
    private final TriggerType triggerType;
    private final BiPredicate<Block, Player> internalCondition;

    public SimpleMultiBlockPattern(
        String id,
        String displayName,
        Map<BlockVector, Character> offsetCharMap,
        Map<Character, BlockMatcher> charMatcherMap,
        List<Action> actions,
        boolean rotationEnabled,
        Direction direction,
        char triggerChar,
        BlockMatcher triggerMatcher,
        BlockVector triggerOffset,
        TriggerType triggerType,
        BiPredicate<Block, Player> internalCondition
    ) {
        this.id = id;
        this.displayName = displayName;
        this.offsetCharMap = offsetCharMap;
        this.charMatcherMap = charMatcherMap;
        this.actions = actions;
        this.rotationEnabled = rotationEnabled;
        this.direction = direction;
        this.triggerChar = triggerChar;
        this.triggerMatcher = triggerMatcher;
        this.triggerOffset = triggerOffset;
        this.triggerType = triggerType;
        this.internalCondition = internalCondition;
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
    public Map<Character, BlockMatcher> getCharMatcherMap() {
        return charMatcherMap;
    }

    @Override
    public List<Action> getActions() {
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
        return triggerMatcher != null && triggerOffset != null;
    }

    @Override
    public char getTriggerChar() {
        return triggerChar;
    }

    @Override
    public BlockMatcher getTriggerMatcher() {
        return triggerMatcher;
    }

    @Override
    public BlockVector getTriggerOffset() {
        return triggerOffset;
    }

    @Override
    public TriggerType getTriggerType() {
        return triggerType;
    }

    @Override
    public BiPredicate<Block, Player> getInternalCondition() {
        return internalCondition;
    }

    @Override
    public boolean containsMatcher(BlockMatcher matcher) {
        return charMatcherMap.containsValue(matcher);
    }

    @Override
    public MatchResult checkMatch(Location origin) {
        return checkMatch(origin, RotationSupport.Rotation.NORTH, RotationSupport.Mirror.NONE);
    }

    @Override
    public MatchResult checkMatch(Location origin, RotationSupport.Rotation rotation, RotationSupport.Mirror mirror) {
        return SimplePatternMatcher.INSTANCE.match(this, origin, rotation, mirror);
    }
}
