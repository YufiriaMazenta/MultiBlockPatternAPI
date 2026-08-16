package pers.yufiria.multiblockpatternapi.api;

import org.bukkit.configuration.ConfigurationSection;

public interface ActionType {

    String typeId();

    Action createAction(ConfigurationSection config);

}
