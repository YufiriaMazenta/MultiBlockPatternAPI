package pers.yufiria.multiblockpatternapi.api;

/**
 * 多方块结构匹配成功后执行的动作接口。
 * <p>
 * 通过 {@link MultiBlockPattern#getActions()} 注册，在匹配成功时自动执行。
 */
@FunctionalInterface
public interface PatternAction {

    /**
     * 匹配成功时执行
     *
     * @param result 匹配结果
     */
    void onMatch(MatchResult result);
}
