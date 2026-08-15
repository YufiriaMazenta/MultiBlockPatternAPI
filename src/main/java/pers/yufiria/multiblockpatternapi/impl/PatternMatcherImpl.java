package pers.yufiria.multiblockpatternapi.impl;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.util.BoundingBox;
import pers.yufiria.multiblockpatternapi.api.MatchResult;
import pers.yufiria.multiblockpatternapi.api.MultiBlockPattern;
import pers.yufiria.multiblockpatternapi.api.PatternMatcher;
import pers.yufiria.multiblockpatternapi.api.RotationSupport;
import pers.yufiria.multiblockpatternapi.util.BlockVector;

import crypticlib.CrypticLib;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

public class PatternMatcherImpl implements PatternMatcher {

    private static final PatternMatcherImpl INSTANCE = new PatternMatcherImpl();
    private final Map<String, MultiBlockPattern> patterns = new ConcurrentHashMap<>();
    private final RotationSupportImpl rotationSupport = RotationSupportImpl.getInstance();

    private PatternMatcherImpl() {}

    public static PatternMatcherImpl getInstance() {
        return INSTANCE;
    }

    @Override
    public MatchResult match(MultiBlockPattern pattern, Location origin) {
        return match(pattern, origin, RotationSupport.Rotation.NORTH, RotationSupport.Mirror.NONE);
    }

    @Override
    public MatchResult match(MultiBlockPattern pattern, Location origin, RotationSupport.Rotation rotation, RotationSupport.Mirror mirror) {
        Map<BlockVector, Character> offsetCharMap = pattern.getOffsetCharMap();
        Map<Character, Object> charMap = pattern.getCharMap();
        List<Block> matchedBlocks = new ArrayList<>(offsetCharMap.size());

        World world = origin.getWorld();
        if (world == null) {
            return new MatchResultImpl(false, pattern, origin, rotation, List.of());
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

            int blockX = baseX + transformed.getX();
            int blockY = baseY + transformed.getY();
            int blockZ = baseZ + transformed.getZ();

            if (blockY < 0 || blockY > world.getMaxHeight()) {
                CrypticLib.debug("[MBP] Block Y out of bounds: " + blockY);
                return new MatchResultImpl(false, pattern, origin, rotation, List.of());
            }

            Block block = world.getBlockAt(blockX, blockY, blockZ);

            // 获取该字符对应的匹配对象
            Object expected = charMap.get(expectedChar);

            CrypticLib.debug("[MBP] Checking offset " + offset.toString() + " char '" + expectedChar + "' at " + blockX + "," + blockY + "," + blockZ + " block: " + block.getType() + " expected: " + expected);

            // null 表示通配符，匹配任意方块
            if (expected == null) {
                matchedBlocks.add(block);
                continue;
            }

            // 检查方块是否匹配
            if (!matchesBlock(block, expected)) {
                CrypticLib.debug("[MBP] Block mismatch!");
                return new MatchResultImpl(false, pattern, origin, rotation, List.of());
            }

            matchedBlocks.add(block);
        }

        CrypticLib.debug("[MBP] Pattern matched successfully!");
        return new MatchResultImpl(true, pattern, origin, rotation, matchedBlocks);
    }

    private boolean matchesBlock(Block block, Object expected) {
        if (expected instanceof Material material) {
            return block.getType() == material;
        } else if (expected instanceof Predicate<?> predicate) {
            @SuppressWarnings("unchecked")
            Predicate<Block> blockPredicate = (Predicate<Block>) predicate;
            return blockPredicate.test(block);
        }
        return false;
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

    public Map<String, MultiBlockPattern> getAllPatterns() {
        return Map.copyOf(patterns);
    }
}
