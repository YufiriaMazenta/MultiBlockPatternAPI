package pers.yufiria.multiblockpatternapi.api;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.BoundingBox;

import java.util.List;
import java.util.Map;

/**
 * 多方块模式匹配引擎接口。
 * <p>
 * 提供模式注册、匹配检测等功能。
 */
public interface PatternMatcher {

    /**
     * 在指定位置检测模式匹配（默认方向）
     *
     * @param pattern 要匹配的模式
     * @param origin 原点位置
     * @return 匹配结果
     */
    MatchResult match(MultiBlockPattern pattern, Location origin);

    /**
     * 在指定位置检测模式匹配（指定旋转和镜像）
     *
     * @param pattern 要匹配的模式
     * @param origin 原点位置
     * @param rotation 旋转方向
     * @param mirror 镜像
     * @return 匹配结果
     */
    MatchResult match(MultiBlockPattern pattern, Location origin, RotationSupport.Rotation rotation, RotationSupport.Mirror mirror);

    /**
     * 在指定区域内搜索所有匹配
     *
     * @param pattern 要匹配的模式
     * @param world 世界
     * @param area 搜索区域
     * @return 所有匹配结果
     */
    List<MatchResult> matchAll(MultiBlockPattern pattern, World world, BoundingBox area);

    /**
     * 在指定区域内搜索所有匹配（指定旋转和镜像）
     *
     * @param pattern 要匹配的模式
     * @param world 世界
     * @param area 搜索区域
     * @param rotation 旋转方向
     * @param mirror 镜像
     * @return 所有匹配结果
     */
    List<MatchResult> matchAll(MultiBlockPattern pattern, World world, BoundingBox area, RotationSupport.Rotation rotation, RotationSupport.Mirror mirror);

    /**
     * 注册模式
     *
     * @param pattern 要注册的模式
     */
    void registerPattern(MultiBlockPattern pattern);

    /**
     * 注销模式
     *
     * @param patternId 模式ID
     */
    void unregisterPattern(String patternId);

    /**
     * 根据ID获取模式
     *
     * @param patternId 模式ID
     * @return 模式，不存在时返回 null
     */
    MultiBlockPattern getPattern(String patternId);

    /**
     * 获取所有已注册的模式
     *
     * @return 模式ID到模式的映射
     */
    Map<String, MultiBlockPattern> getAllPatterns();
}
