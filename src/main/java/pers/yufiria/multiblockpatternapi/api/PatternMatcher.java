package pers.yufiria.multiblockpatternapi.api;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.BoundingBox;

import java.util.List;

public interface PatternMatcher {
    MatchResult match(MultiBlockPattern pattern, Location origin);
    MatchResult match(MultiBlockPattern pattern, Location origin, RotationSupport.Rotation rotation, RotationSupport.Mirror mirror);
    List<MatchResult> matchAll(MultiBlockPattern pattern, World world, BoundingBox area);
    List<MatchResult> matchAll(MultiBlockPattern pattern, World world, BoundingBox area, RotationSupport.Rotation rotation, RotationSupport.Mirror mirror);

    void registerPattern(MultiBlockPattern pattern);
    void unregisterPattern(String patternId);
    MultiBlockPattern getPattern(String patternId);
}
