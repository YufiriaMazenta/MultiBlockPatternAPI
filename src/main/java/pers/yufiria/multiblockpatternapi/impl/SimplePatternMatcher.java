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

        CrypticLib.debug("[MBP] Matching pattern: " + pattern.getId() + " at origin: " + baseX + "," + baseY + "," + baseZ + " rotation: " + rotation);
        CrypticLib.debug("[MBP] offsetCharMap size: " + offsetCharMap.size());

        for (Map.Entry<BlockVector, Character> entry : offsetCharMap.entrySet()) {
            BlockVector offset = entry.getKey();
            char expectedChar = entry.getValue();

            BlockVector transformed = rotationSupport.applyTransform(offset, rotation, mirror);

            int blockX = baseX + transformed.x();
            int blockY = baseY + transformed.y();
            int blockZ = baseZ + transformed.z();

            if (blockY < 0 || blockY > world.getMaxHeight()) {
                CrypticLib.debug("[MBP] Block Y out of bounds: " + blockY);
                return new SimpleMatchResult(false, pattern, origin, rotation, List.of(), null, null);
            }

            Block block = world.getBlockAt(blockX, blockY, blockZ);

            BlockMatcher matcher = charMatcherMap.get(expectedChar);

            CrypticLib.debug("[MBP] Checking offset " + offset + " char '" + expectedChar + "' at " + blockX + "," + blockY + "," + blockZ + " block: " + block.getType() + " matcher: " + (matcher != null ? matcher.toString() : "wildcard"));

            if (matcher == null) {
                continue;
            }

            if (!matcher.matches(block)) {
                CrypticLib.debug("[MBP] Block mismatch!");
                return new SimpleMatchResult(false, pattern, origin, rotation, List.of(), null, null);
            }

            matchedBlocks.add(block);
        }

        CrypticLib.debug("[MBP] Pattern matched successfully!");
        return new SimpleMatchResult(true, pattern, origin, rotation, matchedBlocks, null, null);
    }

    @Override
    public List<MatchResult> matchAll(MultiBlockPattern pattern, World world, BoundingBox area) {
        return matchAll(pattern, world, area, RotationSupport.Rotation.NORTH, RotationSupport.Mirror.NONE);
    }

    @Override
    public List<MatchResult> matchAll(MultiBlockPattern pattern, World world, BoundingBox area, RotationSupport.Rotation rotation, RotationSupport.Mirror mirror) {
        List<MatchResult> results = new ArrayList<>();

        int minX = (int) area.getMinX();
        int minY = (int) area.getMinY();
        int minZ = (int) area.getMinZ();
        int maxX = (int) area.getMaxX();
        int maxY = (int) area.getMaxY();
        int maxZ = (int) area.getMaxZ();

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
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
    }

    @Override
    public void unregisterPattern(String patternId) {
        patterns.remove(patternId);
    }

    @Override
    public MultiBlockPattern getPattern(String patternId) {
        return patterns.get(patternId);
    }

    @Override
    public Map<String, MultiBlockPattern> getAllPatterns() {
        return Map.copyOf(patterns);
    }

    public RotationSupport rotationSupport() {
        return rotationSupport;
    }

    public void setRotationSupport(RotationSupport rotationSupport) {
        this.rotationSupport = rotationSupport;
    }
}
