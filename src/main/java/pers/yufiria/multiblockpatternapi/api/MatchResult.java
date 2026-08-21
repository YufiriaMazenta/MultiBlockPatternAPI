package pers.yufiria.multiblockpatternapi.api;

import crypticlib.Invoker;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * 多方块结构匹配结果。
 * <p>
 * 包含匹配状态、匹配的方块、触发者等信息。
 */
public interface MatchResult {

    /**
     * 是否匹配成功
     *
     * @return 匹配成功返回 true
     */
    boolean isMatch();

    /**
     * 获取匹配的模式
     *
     * @return 匹配的多方块模式
     */
    MultiBlockPattern getPattern();

    /**
     * 获取模式的原点位置
     *
     * @return 原点 Location
     */
    Location getOrigin();

    /**
     * 获取匹配时使用的旋转方向
     *
     * @return 旋转方向
     */
    RotationSupport.Rotation getRotation();

    /**
     * 获取匹配到的方块列表（不包含通配符匹配的方块）
     *
     * @return 匹配方块列表
     */
    List<Block> getMatchedBlocks();

    /**
     * 获取触发者（玩家或实体）
     *
     * @return 触发者，非玩家触发时为 null
     */
    @Nullable Invoker getCauser();

    /**
     * 获取触发方块（被放置或交互的方块）
     *
     * @return 触发方块，匹配失败时为 null
     */
    @Nullable Block getTriggerBlock();

    /**
     * 执行该模式注册的所有 action
     */
    void execute();

    /**
     * 创建带有触发者信息的匹配结果副本
     *
     * @param causer 触发者
     * @param triggerBlock 触发方块
     * @return 带有触发信息的新 MatchResult
     */
    MatchResult withTrigger(@Nullable Invoker causer, @Nullable Block triggerBlock);
}
