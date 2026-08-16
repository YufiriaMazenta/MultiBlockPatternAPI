package pers.yufiria.multiblockpatternapi.impl.action;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import pers.yufiria.multiblockpatternapi.api.ActionType;
import pers.yufiria.multiblockpatternapi.api.Action;

public enum DestroyStructure implements ActionType {

    INSTANCE;

    @Override
    public String typeId() {
        return "destroy_structure";
    }

    @Override
    public Action createAction(ConfigurationSection config) {
        boolean dropItems = config.getBoolean("drop_items", false);
        return matchResult -> {
            if (dropItems) {
                for (Block matchedBlock : matchResult.getMatchedBlocks()) {
                    matchedBlock.breakNaturally();
                }
            } else {
                for (Block matchedBlock : matchResult.getMatchedBlocks()) {
                    matchedBlock.setType(Material.AIR);
                }
            }
        };
    }

}
