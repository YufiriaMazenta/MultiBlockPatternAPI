package pers.yufiria.multiblockpatternapi.impl;

import crypticlib.CrypticLib;
import org.bukkit.Material;
import org.bukkit.block.Block;
import pers.yufiria.multiblockpatternapi.api.MatchResult;

import java.util.Map;

public class StructureHandlerImpl {

    private static final StructureHandlerImpl INSTANCE = new StructureHandlerImpl();

    private StructureHandlerImpl() {}

    public static StructureHandlerImpl getInstance() {
        return INSTANCE;
    }

    public void destroy(MatchResult result, boolean dropItems) {
        if (!result.isMatch()) return;

        CrypticLib.debug("[MBP] Destroying " + result.getMatchedBlocks().size() + " blocks, dropItems: " + dropItems);
        int destroyed = 0;
        for (Block block : result.getMatchedBlocks()) {
            if (block.getType() != Material.AIR) {
                if (dropItems) {
                    block.breakNaturally();
                } else {
                    block.setType(Material.AIR);
                }
                destroyed++;
            }
        }
        CrypticLib.debug("[MBP] Destroyed " + destroyed + " non-air blocks.");
    }

    public void transform(MatchResult result, Map<Material, Material> materialMap) {
        if (!result.isMatch()) return;

        for (Block block : result.getMatchedBlocks()) {
            Material newType = materialMap.get(block.getType());
            if (newType != null) {
                block.setType(newType);
            }
        }
    }
}
