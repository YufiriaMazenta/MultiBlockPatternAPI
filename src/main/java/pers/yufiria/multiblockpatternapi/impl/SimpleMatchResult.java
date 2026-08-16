package pers.yufiria.multiblockpatternapi.impl;

import crypticlib.CrypticLib;
import crypticlib.Invoker;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.jetbrains.annotations.Nullable;
import pers.yufiria.multiblockpatternapi.api.MatchResult;
import pers.yufiria.multiblockpatternapi.api.MultiBlockPattern;
import pers.yufiria.multiblockpatternapi.api.RotationSupport;

import java.util.List;

public class SimpleMatchResult implements MatchResult {

    private final boolean isMatch;
    private final MultiBlockPattern pattern;
    private final Location origin;
    private final RotationSupport.Rotation rotation;
    private final List<Block> matchedBlocks;
    private final @Nullable Invoker trigger;
    private final @Nullable Block triggerBlock;

    public SimpleMatchResult(
        boolean isMatch,
        MultiBlockPattern pattern,
        Location origin,
        RotationSupport.Rotation rotation,
        List<Block> matchedBlocks,
        @Nullable Invoker trigger,
        @Nullable Block triggerBlock
    ) {
        this.isMatch = isMatch;
        this.pattern = pattern;
        this.origin = origin.clone();
        this.rotation = rotation;
        this.matchedBlocks = List.copyOf(matchedBlocks);
        this.trigger = trigger;
        this.triggerBlock = triggerBlock;
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
    public @Nullable Invoker getCauser() {
        return trigger;
    }

    @Override
    public @Nullable Block getTriggerBlock() {
        return triggerBlock;
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
}
