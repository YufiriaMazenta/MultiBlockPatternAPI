package pers.yufiria.multiblockpatternapi;

import crypticlib.BukkitPlugin;
import crypticlib.CrypticLib;
import crypticlib.script.ScriptEngine;
import pers.yufiria.multiblockpatternapi.config.Languages;
import pers.yufiria.multiblockpatternapi.config.PluginConfigs;
import pers.yufiria.multiblockpatternapi.script.ActionModule;

public final class PluginMain extends BukkitPlugin {

    private static PluginMain instance;

    public PluginMain() {
        instance = this;
    }

    @Override
    public void whenEnable() {
        CrypticLib.debug = PluginConfigs.DEBUG.value();
        ScriptEngine.INSTANCE.registerModule(ActionModule.INSTANCE);
        CrypticLib.info(Languages.LOG_PLUGIN_ENABLED.value());
    }

    @Override
    public void whenDisable() {
        CrypticLib.info(Languages.LOG_PLUGIN_DISABLED.value());
    }

    @Override
    public void whenReload() {
        CrypticLib.debug = PluginConfigs.DEBUG.value();
    }

    public static PluginMain instance() {
        return instance;
    }

}
