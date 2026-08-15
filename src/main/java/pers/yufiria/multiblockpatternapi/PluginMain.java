package pers.yufiria.multiblockpatternapi;

import crypticlib.BukkitPlugin;
import crypticlib.CrypticLib;
import crypticlib.config.BukkitConfigWrapper;
import pers.yufiria.multiblockpatternapi.config.Messages;
import pers.yufiria.multiblockpatternapi.registry.PatternRegistryImpl;

public final class PluginMain extends BukkitPlugin {

    @Override
    public void whenEnable() {
        CrypticLib.debug = true;
        BukkitConfigWrapper patternsWrapper = getConfigWrapperOrCreate("patterns.yml");
        PatternRegistryImpl.getInstance().setPatternsWrapper(patternsWrapper);
        PatternRegistryImpl.getInstance().loadPatterns();

        CrypticLib.info(Messages.PLUGIN_ENABLED.value());
    }

    @Override
    public void whenDisable() {
        CrypticLib.info(Messages.PLUGIN_DISABLED.value());
    }
}
