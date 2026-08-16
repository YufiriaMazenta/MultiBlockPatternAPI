package pers.yufiria.multiblockpatternapi.api;

import org.bukkit.Material;
import org.bukkit.block.Block;

import java.util.function.Predicate;

/**
 * 方块匹配器接口，用于判断方块是否符合模式要求。
 * <p>
 * 可通过 {@link #ofMaterial(Material)} 或 {@link #ofPredicate(Predicate)} 创建实例。
 */
@FunctionalInterface
public interface BlockMatcher {

    /**
     * 判断指定方块是否匹配
     *
     * @param block 要检查的方块
     * @return 如果匹配返回 true
     */
    boolean matches(Block block);

    /**
     * 创建基于 Material 的匹配器
     *
     * @param material 要匹配的方块类型
     * @return 新的 BlockMatcher 实例
     */
    static BlockMatcher ofMaterial(Material material) {
        return block -> block.getType() == material;
    }

    /**
     * 创建基于 Predicate 的自定义匹配器
     *
     * @param predicate 自定义匹配逻辑
     * @return 新的 BlockMatcher 实例
     */
    static BlockMatcher ofPredicate(Predicate<Block> predicate) {
        return predicate::test;
    }
}
