package pers.yufiria.multiblockpatternapi.api;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import pers.yufiria.multiblockpatternapi.util.BlockVector;

import java.util.List;
import java.util.Map;
import java.util.function.BiPredicate;

/**
 * 多方块结构模式定义接口。
 * <p>
 * 定义了模式的布局、方块匹配规则、触发条件和回调动作。
 * 可通过 {@link PatternBuilder} 创建实例。
 */
public interface MultiBlockPattern {

    /**
     * 模式的空间方向
     */
    enum Direction {
        /** 水平方向（XZ平面） */
        HORIZONTAL,
        /** 竖直方向（XY平面） */
        VERTICAL
    }

    /**
     * 获取模式ID
     *
     * @return 模式唯一标识符
     */
    String getId();

    /**
     * 获取模式显示名称
     *
     * @return 显示名称
     */
    String getDisplayName();

    /**
     * 获取偏移量到字符的映射
     *
     * @return 偏移量-字符映射
     */
    Map<BlockVector, Character> getOffsetCharMap();

    /**
     * 获取字符到匹配器的映射
     *
     * @return 字符-匹配器映射
     */
    Map<Character, BlockMatcher> getCharMatcherMap();

    /**
     * 获取匹配成功后执行的动作列表
     *
     * @return 动作列表
     */
    List<PatternAction> getActions();

    /**
     * 是否启用旋转匹配
     *
     * @return 启用旋转返回 true
     */
    boolean isRotationEnabled();

    /**
     * 获取模式的空间方向
     *
     * @return 方向（水平或竖直）
     */
    Direction getDirection();

    /**
     * 是否有触发点（锚点）
     *
     * @return 有触发点返回 true
     */
    boolean hasTrigger();

    /**
     * 获取触发点字符
     *
     * @return 模式布局中的触发字符
     */
    char getTriggerChar();

    /**
     * 获取触发点匹配器
     *
     * @return 用于匹配触发方块的 BlockMatcher
     */
    BlockMatcher getTriggerMatcher();

    /**
     * 获取触发点偏移量
     *
     * @return 触发点相对于原点的偏移
     */
    BlockVector getTriggerOffset();

    /**
     * 获取触发类型
     *
     * @return BLOCK_CHANGE（方块变更）或 INTERACTION（玩家交互）
     */
    TriggerType getTriggerType();

    /**
     * 获取交互触发条件（仅 INTERACTION 模式有效）
     *
     * @return BiPredicate<Block, Player> 条件，无条件时为 null
     */
    BiPredicate<Block, Player> getInternalCondition();

    /**
     * 检查模式是否包含指定的匹配器
     *
     * @param matcher 要检查的匹配器
     * @return 包含返回 true
     */
    boolean containsMatcher(BlockMatcher matcher);

    /**
     * 在指定原点检测模式匹配（默认方向）
     *
     * @param origin 原点位置
     * @return 匹配结果
     */
    MatchResult checkMatch(Location origin);

    /**
     * 在指定原点检测模式匹配（指定旋转和镜像）
     *
     * @param origin 原点位置
     * @param rotation 旋转方向
     * @param mirror 镜像
     * @return 匹配结果
     */
    MatchResult checkMatch(Location origin, RotationSupport.Rotation rotation, RotationSupport.Mirror mirror);
}
