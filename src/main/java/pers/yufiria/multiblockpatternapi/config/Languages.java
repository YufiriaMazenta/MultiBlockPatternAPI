package pers.yufiria.multiblockpatternapi.config;

import crypticlib.PlatformSide;
import crypticlib.config.ConfigHandler;
import crypticlib.config.node.impl.bukkit.StringConfig;

@ConfigHandler(path = "lang.yml", platforms = {PlatformSide.BUKKIT})
public class Languages {

    public static final StringConfig PREFIX = new StringConfig(
        "prefix",
        "&7[&eMBP&7] ",
        "消息前缀"
    );

    public static final StringConfig COMMAND_LIST_NO_PATTERNS = new StringConfig(
        "command.list.no_patterns",
        "&7没有注册的模式。",
        "没有模式时显示的消息"
    );

    public static final StringConfig COMMAND_LIST_PATTERN_LIST_HEADER = new StringConfig(
        "command.list.pattern_list_header",
        "&6已注册的模式:",
        "模式列表标题"
    );

    public static final StringConfig COMMAND_LIST_PATTERN_LIST_ENTRY = new StringConfig(
        "command.list.pattern_list_entry",
        "  &e%id% &7- %name%",
        "模式列表条目，支持 %id% 和 %name% 占位符"
    );
    public static final StringConfig COMMAND_RELOAD_SUCCESS = new StringConfig(
        "command.reload.success",
        "&a配置已重新加载。",
        "重新加载配置成功时显示的消息"
    );

    public static final StringConfig COMMAND_PLAYER_ONLY = new StringConfig(
        "command.player_only",
        "&c此命令只能由玩家执行。",
        "非玩家执行命令时显示的消息"
    );

    public static final StringConfig LOG_PLUGIN_ENABLED = new StringConfig(
        "log.plugin_enabled",
        "&aMultiBlockPatternAPI 已启用。",
        "插件启用时显示的消息"
    );

    public static final StringConfig LOG_PLUGIN_DISABLED = new StringConfig(
        "log.plugin_disabled",
        "&cMultiBlockPatternAPI 已禁用。",
        "插件禁用时显示的消息"
    );

    public static final StringConfig LOG_PATTERN_LOADED = new StringConfig(
        "log.pattern_loaded",
        "&a已加载模式: %name%",
        "从配置文件加载模式成功时显示的消息，支持 %name% 占位符"
    );

    public static final StringConfig LOG_PATTERN_LOAD_ERROR = new StringConfig(
        "log.pattern_load_error",
        "&c加载模式 '%name%' 失败: %error%",
        "从配置文件加载模式失败时显示的消息，支持 %name% 和 %error% 占位符"
    );

}
