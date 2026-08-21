package pers.yufiria.multiblockpatternapi.impl;

import crypticlib.CrypticLib;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.util.BoundingBox;
import pers.yufiria.multiblockpatternapi.api.*;
import pers.yufiria.multiblockpatternapi.util.BlockVector;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public enum SimplePatternMatcher implements PatternMatcher {

    INSTANCE;

    private final Map<String, MultiBlockPattern> patterns = new ConcurrentHashMap<>();
    private volatile List<MultiBlockPattern> triggerPatterns = List.of();
    private volatile List<MultiBlockPattern> noTriggerPatterns = List.of();
    private volatile List<MultiBlockPattern> interactionPatterns = List.of();
    private RotationSupport rotationSupport = SimpleRotationSupport.getInstance();

    @Override
    public MatchResult match(MultiBlockPattern pattern, Location origin) {
        return match(pattern, origin, RotationSupport.Rotation.NORTH, RotationSupport.Mirror.NONE);
    }

    @Override
    public MatchResult match(MultiBlockPattern pattern, Location origin, RotationSupport.Rotation rotation, RotationSupport.Mirror mirror) {
        Map<BlockVector, Character> offsetCharMap = pattern.getOffsetCharMap();
        Map<Character, BlockMatcher> charMatcherMap = pattern.getCharMatcherMap();
        List<Block> matchedBlocks = new ArrayList<>(offsetCharMap.size());

        World world = origin.getWorld();
        if (world == null) {
            return new SimpleMatchResult(false, pattern, origin, rotation, List.of(), null, null);
        }

        int baseX = origin.getBlockX();
        int baseY = origin.getBlockY();
        int baseZ = origin.getBlockZ();

        if (CrypticLib.debug) {
            CrypticLib.debug("[MBP] Matching pattern: " + pattern.getId() + " at origin: " + baseX + "," + baseY + "," + baseZ + " rotation: " + rotation);
            CrypticLib.debug("[MBP] offsetCharMap size: " + offsetCharMap.size());
        }

        for (Map.Entry<BlockVector, Character> entry : offsetCharMap.entrySet()) {
            BlockVector offset = entry.getKey();
            char expectedChar = entry.getValue();

            BlockVector transformed = rotationSupport.applyTransform(offset, rotation, mirror);

            int blockX = baseX + transformed.x();
            int blockY = baseY + transformed.y();
            int blockZ = baseZ + transformed.z();

            if (blockY < 0 || blockY > world.getMaxHeight()) {
                if (CrypticLib.debug) {
                    CrypticLib.debug("[MBP] Block Y out of bounds: " + blockY);
                }
                return new SimpleMatchResult(false, pattern, origin, rotation, List.of(), null, null);
            }

            Block block = world.getBlockAt(blockX, blockY, blockZ);

            BlockMatcher matcher = charMatcherMap.get(expectedChar);

            if (CrypticLib.debug) {
                CrypticLib.debug("[MBP] Checking offset " + offset + " char '" + expectedChar + "' at " + blockX + "," + blockY + "," + blockZ + " block: " + block.getType() + " matcher: " + (matcher != null ? matcher.toString() : "wildcard"));
            }

            if (matcher == null) {
                continue;
            }

            if (!matcher.matches(block)) {
                if (CrypticLib.debug) {
                    CrypticLib.debug("[MBP] Block mismatch!");
                }
                return new SimpleMatchResult(false, pattern, origin, rotation, List.of(), null, null);
            }

            matchedBlocks.add(block);
        }

        if (CrypticLib.debug) {
            CrypticLib.debug("[MBP] Pattern matched successfully!");
        }
        return new SimpleMatchResult(true, pattern, origin, rotation, matchedBlocks, null, null);
    }

    @Override
    public List<MatchResult> matchAll(MultiBlockPattern pattern, World world, BoundingBox area) {
        return matchAll(pattern, world, area, RotationSupport.Rotation.NORTH, RotationSupport.Mirror.NONE);
    }

    @Override
    public List<MatchResult> matchAll(MultiBlockPattern pattern, World world, BoundingBox area, RotationSupport.Rotation rotation, RotationSupport.Mirror mirror) {
        List<MatchResult> results = new ArrayList<>();

        int areaMinX = (int) area.getMinX();
        int areaMinY = (int) area.getMinY();
        int areaMinZ = (int) area.getMinZ();
        int areaMaxX = (int) area.getMaxX();
        int areaMaxY = (int) area.getMaxY();
        int areaMaxZ = (int) area.getMaxZ();

        // 计算模式在变换后的空间包围盒，缩小搜索范围
        Map<BlockVector, Character> offsetCharMap = pattern.getOffsetCharMap();
        int minOffX = Integer.MAX_VALUE, maxOffX = Integer.MIN_VALUE;
        int minOffY = Integer.MAX_VALUE, maxOffY = Integer.MIN_VALUE;
        int minOffZ = Integer.MAX_VALUE, maxOffZ = Integer.MIN_VALUE;
        for (BlockVector offset : offsetCharMap.keySet()) {
            BlockVector transformed = rotationSupport.applyTransform(offset, rotation, mirror);
            minOffX = Math.min(minOffX, transformed.x());
            maxOffX = Math.max(maxOffX, transformed.x());
            minOffY = Math.min(minOffY, transformed.y());
            maxOffY = Math.max(maxOffY, transformed.y());
            minOffZ = Math.min(minOffZ, transformed.z());
            maxOffZ = Math.max(maxOffZ, transformed.z());
        }

        // 原点范围：确保模式至少有一个方块落在搜索区域内
        int startX = Math.max(areaMinX, areaMinX - maxOffX);
        int startY = Math.max(areaMinY, areaMinY - maxOffY);
        int startZ = Math.max(areaMinZ, areaMinZ - maxOffZ);
        int endX = Math.min(areaMaxX, areaMaxX - minOffX);
        int endY = Math.min(areaMaxY, areaMaxY - minOffY);
        int endZ = Math.min(areaMaxZ, areaMaxZ - minOffZ);

        for (int x = startX; x <= endX; x++) {
            for (int y = startY; y <= endY; y++) {
                for (int z = startZ; z <= endZ; z++) {
                    Location origin = new Location(world, x, y, z);
                    MatchResult result = match(pattern, origin, rotation, mirror);
                    if (result.isMatch()) {
                        results.add(result);
                    }
                }
            }
        }

        return results;
    }

    @Override
    public void registerPattern(MultiBlockPattern pattern) {
        patterns.put(pattern.getId(), pattern);
        rebuildCategorizedLists();
    }

    @Override
    public void unregisterPattern(String patternId) {
        patterns.remove(patternId);
        rebuildCategorizedLists();
    }

    @Override
    public MultiBlockPattern getPattern(String patternId) {
        return patterns.get(patternId);
    }

    @Override
    public Map<String, MultiBlockPattern> getAllPatterns() {
        return Map.copyOf(patterns);
    }

    @Override
    public void clearPatterns() {
        patterns.clear();
        rebuildCategorizedLists();
    }

    @Override
    public List<MultiBlockPattern> getTriggerPatterns() {
        return triggerPatterns;
    }

    @Override
    public List<MultiBlockPattern> getNoTriggerPatterns() {
        return noTriggerPatterns;
    }

    @Override
    public List<MultiBlockPattern> getInteractionPatterns() {
        return interactionPatterns;
    }

    private void rebuildCategorizedLists() {
        List<MultiBlockPattern> all = List.copyOf(patterns.values());
        List<MultiBlockPattern> triggers = new ArrayList<>();
        List<MultiBlockPattern> noTriggers = new ArrayList<>();
        List<MultiBlockPattern> interactions = new ArrayList<>();
        for (MultiBlockPattern p : all) {
            if (p.hasTrigger()) {
                if (p.getTriggerType() == TriggerType.INTERACTION) {
                    interactions.add(p);
                }
                triggers.add(p);
            } else {
                noTriggers.add(p);
            }
        }
        this.triggerPatterns = List.copyOf(triggers);
        this.noTriggerPatterns = List.copyOf(noTriggers);
        this.interactionPatterns = List.copyOf(interactions);
    }

    public RotationSupport rotationSupport() {
        return rotationSupport;
    }

    public void setRotationSupport(RotationSupport rotationSupport) {
        this.rotationSupport = rotationSupport;
    }
}
