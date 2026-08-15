package pers.yufiria.multiblockpatternapi.config;

import crypticlib.PlatformSide;
import crypticlib.config.ConfigHandler;
import crypticlib.config.node.impl.bukkit.StringConfig;

@ConfigHandler(path = "messages.yml", platforms = {PlatformSide.BUKKIT})
public class Messages {

    public static final StringConfig PREFIX = new StringConfig(
        "prefix",
        "&7[&eMBP&7] ",
        "消息前缀"
    );

    public static final StringConfig NO_PATTERNS = new StringConfig(
        "no_patterns",
        "&7没有注册的模式。",
        "没有模式时显示的消息"
    );

    public static final StringConfig PATTERN_LIST_HEADER = new StringConfig(
        "pattern_list_header",
        "&6已注册的模式:",
        "模式列表标题"
    );

    public static final StringConfig PATTERN_LIST_ENTRY = new StringConfig(
        "pattern_list_entry",
        "  &e%id% &7- %name%",
        "模式列表条目，支持 %id% 和 %name% 占位符"
    );

    public static final StringConfig ONLY_PLAYER = new StringConfig(
        "only_player",
        "&c此命令只能由玩家执行。",
        "非玩家执行命令时显示的消息"
    );

    public static final StringConfig NO_MATCH = new StringConfig(
        "no_match",
        "&7当前位置没有匹配的模式。",
        "没有匹配模式时显示的消息"
    );

    public static final StringConfig MATCH_FOUND = new StringConfig(
        "match_found",
        "&a检测到模式: &e%name%",
        "检测到模式时显示的消息，支持 %name% 占位符"
    );

    public static final StringConfig MATCH_SUMMARY = new StringConfig(
        "match_summary",
        "&a共检测到 &e%count% &a个匹配的模式。",
        "检测完成后的汇总消息，支持 %count% 占位符"
    );

    public static final StringConfig PLUGIN_ENABLED = new StringConfig(
        "plugin_enabled",
        "&aMultiBlockPatternAPI 已启用。",
        "插件启用时显示的消息"
    );

    public static final StringConfig PLUGIN_DISABLED = new StringConfig(
        "plugin_disabled",
        "&cMultiBlockPatternAPI 已禁用。",
        "插件禁用时显示的消息"
    );

    public static final StringConfig PATTERN_LOADED = new StringConfig(
        "pattern_loaded",
        "&a已加载模式: %name%",
        "模式加载成功时显示的消息，支持 %name% 占位符"
    );

    public static final StringConfig PATTERN_LOAD_ERROR = new StringConfig(
        "pattern_load_error",
        "&c加载模式 '%name%' 失败: %error%",
        "模式加载失败时显示的消息，支持 %name% 和 %error% 占位符"
    );

    public static final StringConfig RELOAD_SUCCESS = new StringConfig(
        "reload_success",
        "&a配置已重新加载。",
        "重新加载配置成功时显示的消息"
    );
}
