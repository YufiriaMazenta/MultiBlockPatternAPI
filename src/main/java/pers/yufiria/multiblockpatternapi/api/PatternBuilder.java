package pers.yufiria.multiblockpatternapi.api;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import pers.yufiria.multiblockpatternapi.impl.SimpleMultiBlockPattern;
import pers.yufiria.multiblockpatternapi.util.BlockVector;

import crypticlib.CrypticLib;

import java.util.*;
import java.util.function.BiPredicate;

/**
 * 多方块模式构建器。
 * <p>
 * 使用链式调用定义模式的布局、匹配规则和触发条件。
 * <p>
 * 示例：
 * <pre>{@code
 * MultiBlockPattern pattern = PatternBuilder.create("my_pattern")
 *     .displayName("我的模式")
 *     .layer(" B ", "BSB", " B ")
 *     .block('B', BlockMatcher.ofMaterial(Material.BEACON))
 *     .block('S', BlockMatcher.ofMaterial(Material.STONE))
 *     .triggerBlock('B', BlockMatcher.ofMaterial(Material.BEACON))
 *     .triggerType(TriggerType.INTERACTION)
 *     .internalCondition((block, player) -> player.isSneaking())
 *     .action(result -> result.getCauser().sendMsg("激活!"))
 *     .build();
 * }</pre>
 */
public class PatternBuilder {

    /** 通配符，匹配任意方块 */
    public static final char WILDCARD = ' ';

    private final String id;
    private String displayName;
    private final List<List<String>> layers = new ArrayList<>();
    private final Map<Character, BlockMatcher> matcherMap = new HashMap<>();
    private final List<Action> actions = new ArrayList<>();
    private boolean rotationEnabled = false;
    private MultiBlockPattern.Direction direction = MultiBlockPattern.Direction.HORIZONTAL;

    private char triggerChar = '\0';
    private BlockMatcher triggerMatcher = null;
    private TriggerType triggerType = TriggerType.BLOCK_CHANGE;
    private BiPredicate<Block, Player> internalCondition = null;

    private PatternBuilder(String id) {
        this.id = id;
    }

    /**
     * 创建构建器
     *
     * @param id 模式ID
     * @return 新的构建器实例
     */
    public static PatternBuilder create(String id) {
        return new PatternBuilder(id);
    }

    /**
     * 设置显示名称
     *
     * @param displayName 显示名称
     * @return this
     */
    public PatternBuilder displayName(String displayName) {
        this.displayName = displayName;
        return this;
    }

    /**
     * 添加一层（水平方向）
     * <p>
     * 每个字符串代表一行（Z轴），字符串长度为 X 轴宽度。
     * 空格字符表示通配符（匹配任意方块）。
     *
     * @param rows 该层的行
     * @return this
     */
    public PatternBuilder layer(String... rows) {
        List<String> layer = new ArrayList<>(Arrays.asList(rows));
        layers.add(layer);
        return this;
    }

    /**
     * 映射字符到方块匹配器
     *
     * @param c 模式中的字符
     * @param matcher 对应的匹配器
     * @return this
     */
    public PatternBuilder block(char c, BlockMatcher matcher) {
        matcherMap.put(c, matcher);
        return this;
    }

    /**
     * 注册匹配成功后的动作
     *
     * @param action 动作
     * @return this
     */
    public PatternBuilder action(Action action) {
        actions.add(action);
        return this;
    }

    /**
     * 启用旋转匹配
     *
     * @param enabled 是否启用
     * @return this
     */
    public PatternBuilder rotationEnabled(boolean enabled) {
        this.rotationEnabled = enabled;
        return this;
    }

    /**
     * 设置模式的空间方向
     *
     * @param direction 方向（水平或竖直）
     * @return this
     */
    public PatternBuilder direction(MultiBlockPattern.Direction direction) {
        this.direction = direction;
        return this;
    }

    /**
     * 设置触发点（锚点）
     * <p>
     * 触发点是模式中用于定位的基准方块。当该方块被放置或交互时，
     * 插件会以该方块为基准检测整个模式是否匹配。
     *
     * @param triggerChar 模式布局中的触发字符
     * @param matcher 触发方块的匹配器
     * @return this
     */
    public PatternBuilder triggerBlock(char triggerChar, BlockMatcher matcher) {
        this.triggerChar = triggerChar;
        this.triggerMatcher = matcher;
        matcherMap.put(triggerChar, matcher);
        return this;
    }

    /**
     * 设置触发模式
     *
     * @param type BLOCK_CHANGE（方块变更）或 INTERACTION（玩家交互）
     * @return this
     */
    public PatternBuilder triggerType(TriggerType type) {
        this.triggerType = type;
        return this;
    }

    /**
     * 设置交互触发条件
     * <p>
     * 仅在 triggerMode 为 INTERACTION 时生效。
     * 条件接收被交互的方块和玩家，返回 true 表示可以触发。
     *
     * @param condition 触发条件
     * @return this
     */
    public PatternBuilder internalCondition(BiPredicate<Block, Player> condition) {
        this.internalCondition = condition;
        return this;
    }

    /**
     * 构建多方块模式
     *
     * @return 构建完成的模式
     * @throws IllegalStateException 如果没有定义任何层
     */
    public MultiBlockPattern build() {
        if (layers.isEmpty()) {
            throw new IllegalStateException("Pattern must have at least one layer");
        }

        Map<BlockVector, Character> offsetCharMap = new LinkedHashMap<>();
        Map<Character, BlockMatcher> charMatcherMap = new HashMap<>();
        BlockVector triggerOffset = null;

        charMatcherMap.put('_', BlockMatcher.ofMaterial(Material.AIR));
        charMatcherMap.putAll(matcherMap);

        CrypticLib.debug("[MBP] Building pattern: " + id + " direction: " + direction);

        for (int layerIndex = 0; layerIndex < layers.size(); layerIndex++) {
            List<String> layer = layers.get(layerIndex);
            for (int rowIndex = 0; rowIndex < layer.size(); rowIndex++) {
                String row = layer.get(rowIndex);
                for (int colIndex = 0; colIndex < row.length(); colIndex++) {
                    char c = row.charAt(colIndex);

                    if (c != WILDCARD && !charMatcherMap.containsKey(c)) {
                        throw new IllegalStateException("Unknown character '" + c + "' in pattern.");
                    }

                    if (c != WILDCARD) {
                        BlockVector offset;
                        if (direction == MultiBlockPattern.Direction.VERTICAL) {
                            int maxRow = layer.size() - 1;
                            offset = new BlockVector(colIndex, maxRow - rowIndex, layerIndex);
                        } else {
                            offset = new BlockVector(colIndex, layerIndex, rowIndex);
                        }
                        offsetCharMap.put(offset, c);
                        if (c == triggerChar) {
                            triggerOffset = offset;
                        }
                    }
                }
            }
        }

        return new SimpleMultiBlockPattern(
            id,
            displayName != null ? displayName : id,
            offsetCharMap,
            charMatcherMap,
            new ArrayList<>(actions),
            rotationEnabled,
            direction,
            triggerChar,
            triggerMatcher,
            triggerOffset,
            triggerType,
            internalCondition
        );
    }
}
