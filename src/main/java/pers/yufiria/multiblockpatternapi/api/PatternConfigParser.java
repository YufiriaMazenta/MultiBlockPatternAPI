package pers.yufiria.multiblockpatternapi.api;

import org.bukkit.configuration.ConfigurationSection;

/**
 * 多方块模式配置解析器接口。
 * <p>
 * 用于从 YAML 配置文件解析模式定义。
 */
public interface PatternConfigParser {

    /**
     * 从配置节点解析模式
     *
     * @param patternId 模式ID
     * @param config 配置节点
     * @return 解析后的模式
     * @throws Exception 解析失败时抛出
     */
    MultiBlockPattern parse(String patternId, ConfigurationSection config) throws Exception;

}
