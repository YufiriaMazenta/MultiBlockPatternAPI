package pers.yufiria.multiblockpatternapi.config;

import crypticlib.config.ConfigHandler;
import crypticlib.config.node.impl.bukkit.BooleanConfig;

@ConfigHandler(path = "config.yml")
public class PluginConfigs {

    public static final BooleanConfig DEBUG = new BooleanConfig("debug", false);

}
