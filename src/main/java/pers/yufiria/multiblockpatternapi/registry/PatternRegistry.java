package pers.yufiria.multiblockpatternapi.registry;

import crypticlib.BukkitPlugin;
import crypticlib.CrypticLib;
import crypticlib.CrypticLibPlugin;
import crypticlib.config.BukkitConfigWrapper;
import crypticlib.lifecycle.Lifecycle;
import crypticlib.lifecycle.LifecycleRule;
import crypticlib.lifecycle.LifecycleTask;
import crypticlib.lifecycle.LifecycleTaskSettings;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import pers.yufiria.multiblockpatternapi.api.*;
import pers.yufiria.multiblockpatternapi.config.Languages;
import pers.yufiria.multiblockpatternapi.impl.SimplePatternConfigParser;
import pers.yufiria.multiblockpatternapi.impl.SimplePatternMatcher;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@LifecycleTaskSettings(rules = {
    @LifecycleRule(lifeCycle = Lifecycle.ENABLE),
    @LifecycleRule(lifeCycle = Lifecycle.RELOAD)
})
public enum PatternRegistry implements LifecycleTask {

    INSTANCE;

    private PatternMatcher matcher = SimplePatternMatcher.INSTANCE;
    private PatternConfigParser configParser = new SimplePatternConfigParser();
    private BukkitConfigWrapper patternsConfig;

    public void register(MultiBlockPattern pattern) {
        matcher.registerPattern(pattern);
    }

    public void unregister(String patternId) {
        matcher.unregisterPattern(patternId);
    }

    public MultiBlockPattern get(String patternId) {
        return matcher.getPattern(patternId);
    }

    public Map<String, MultiBlockPattern> getAll() {
        return matcher.getAllPatterns();
    }

    public void clear() {
        for (String id : new ArrayList<>(matcher.getAllPatterns().keySet())) {
            matcher.unregisterPattern(id);
        }
    }

    /**
     * 获取触发方块匹配指定方块的所有模式
     */
    public List<MultiBlockPattern> getPatternsByTrigger(Block block) {
        return matcher.getAllPatterns().values().stream()
            .filter(p -> p.hasTrigger() && p.getTriggerMatcher().matches(block))
            .collect(Collectors.toList());
    }

    /**
     * 获取所有没有触发方块的模式
     */
    public List<MultiBlockPattern> getPatternsWithoutTrigger() {
        return matcher.getAllPatterns().values().stream()
            .filter(p -> !p.hasTrigger())
            .collect(Collectors.toList());
    }

    /**
     * 获取所有交互触发的模式
     */
    public List<MultiBlockPattern> getPatternsByInteraction() {
        return matcher.getAllPatterns().values().stream()
            .filter(p -> p.getTriggerType() == TriggerType.INTERACTION && p.hasTrigger())
            .collect(Collectors.toList());
    }

    public void loadPatterns() {
        if (patternsConfig == null) return;

        ConfigurationSection root = patternsConfig.config();

        for (String patternId : root.getKeys(false)) {
            ConfigurationSection patternSection = root.getConfigurationSection(patternId);
            if (patternSection == null) continue;

            // 跳过非模式节点
            if (!patternSection.contains("layer") && !patternSection.contains("layers")) {
                continue;
            }

            try {
                MultiBlockPattern pattern = configParser.parse(patternId, patternSection);
                register(pattern);
                String msg = Languages.LOG_PATTERN_LOADED.value().replace("%name%", pattern.getDisplayName());
                CrypticLib.info(msg);
            } catch (Exception e) {
                String msg = Languages.LOG_PATTERN_LOAD_ERROR.value()
                    .replace("%name%", patternId)
                    .replace("%error%", e.getMessage());
                CrypticLib.info("&e" + msg);
            }
        }

        if (CrypticLib.debug) {
            //测试方法
            MultiBlockPattern pattern = PatternBuilder.create("test_interaction")
                .displayName("测试交互触发")
                .triggerType(TriggerType.INTERACTION)
                .triggerBlock('B', BlockMatcher.ofPredicate(block -> {
                    Material type = block.getType();
                    return Tag.WOOL.isTagged(type);
                }))
                .internalCondition((block, player) -> {
                    // 手持木棍右键任意方块时触发
                    return player.getInventory().getItemInMainHand().getType() == Material.STICK;
                })
                .layer(
                    " B ",
                    "BSB",
                    " B "
                )
                .block('S', BlockMatcher.ofMaterial(Material.STONE))
                .action(result -> {
                    Block triggerBlock = result.getTriggerBlock();
                    if (triggerBlock != null)
                        triggerBlock.setType(Material.BLUE_WOOL);
                    CrypticLib.info("[MBP] 交互触发模式已匹配!");
                    if (result.getCauser() != null) {
                        result.getCauser().sendMsg("&a交互触发模式已匹配!");
                    }
                })
                .build();

            PatternRegistry.INSTANCE.register(pattern);
        }
    }

    public void reloadPatterns() {

    }

    public PatternMatcher matcher() {
        return matcher;
    }

    public void setMatcher(PatternMatcher matcher) {
        this.matcher = matcher;
    }

    public PatternConfigParser configParser() {
        return configParser;
    }

    public void setConfigParser(PatternConfigParser configParser) {
        this.configParser = configParser;
    }

    @Override
    public void lifecycle(CrypticLibPlugin crypticLibPlugin, @NotNull Lifecycle lifecycle) {
        if (lifecycle == Lifecycle.ENABLE) {
            this.patternsConfig = ((BukkitPlugin) crypticLibPlugin).getConfigWrapperOrCreate("patterns.yml");
        }
        clear();
        loadPatterns();
    }

}
