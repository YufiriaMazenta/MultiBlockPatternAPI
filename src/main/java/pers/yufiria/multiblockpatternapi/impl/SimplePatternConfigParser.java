package pers.yufiria.multiblockpatternapi.impl;

import crypticlib.BukkitPlayer;
import crypticlib.util.BukkitConfigHelper;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import pers.yufiria.multiblockpatternapi.api.BlockMatcher;
import pers.yufiria.multiblockpatternapi.api.MultiBlockPattern;
import pers.yufiria.multiblockpatternapi.api.PatternBuilder;
import pers.yufiria.multiblockpatternapi.api.PatternConfigParser;
import pers.yufiria.multiblockpatternapi.api.TriggerType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SimplePatternConfigParser implements PatternConfigParser {

    @Override
    public MultiBlockPattern parse(String patternId, ConfigurationSection config) {
        String displayName = config.getString("display_name", patternId);
        boolean rotation = config.getBoolean("rotation", false);
        String directionStr = config.getString("direction", "horizontal");
        MultiBlockPattern.Direction direction = "vertical".equalsIgnoreCase(directionStr)
            ? MultiBlockPattern.Direction.VERTICAL
            : MultiBlockPattern.Direction.HORIZONTAL;

        // 解析触发模式
        String triggerModeStr = config.getString("trigger_mode", "block_change");
        TriggerType triggerType = "interaction".equalsIgnoreCase(triggerModeStr)
            ? TriggerType.INTERACTION
            : TriggerType.BLOCK_CHANGE;

        PatternBuilder builder = PatternBuilder.create(patternId)
            .displayName(displayName)
            .rotationEnabled(rotation)
            .direction(direction)
            .triggerType(triggerType);

        if (config.contains("layer")) {
            List<String> layer = config.getStringList("layer");
            if (!layer.isEmpty()) {
                builder.layer(layer.toArray(new String[0]));
            }
        }

        if (config.contains("layers")) {
            List<?> layers = config.getList("layers");
            if (layers != null) {
                for (Object layerObj : layers) {
                    if (layerObj instanceof List<?> layer) {
                        List<String> rows = new ArrayList<>();
                        for (Object row : layer) {
                            rows.add(row.toString());
                        }
                        builder.layer(rows.toArray(new String[0]));
                    }
                }
            }
        }

        ConfigurationSection mapSection = config.getConfigurationSection("map");
        if (mapSection != null) {
            for (String key : mapSection.getKeys(false)) {
                String cleanKey = key.replace("\"", "").replace("'", "");
                if (cleanKey.length() != 1) continue;
                char c = cleanKey.charAt(0);
                String materialName = mapSection.getString(key);
                if (materialName != null) {
                    Material material = Material.matchMaterial(materialName);
                    if (material != null) {
                        builder.block(c, BlockMatcher.ofMaterial(material));
                    }
                }
            }
        }

        // 解析触发方块
        if (config.contains("trigger")) {
            String triggerStr = config.getString("trigger", "");
            if (triggerStr.length() == 1) {
                char triggerChar = triggerStr.charAt(0);
                if (mapSection != null) {
                    String materialName = mapSection.getString(triggerStr);
                    if (materialName != null) {
                        Material triggerMaterial = Material.matchMaterial(materialName);
                        if (triggerMaterial != null) {
                            builder.triggerBlock(triggerChar, BlockMatcher.ofMaterial(triggerMaterial));
                        }
                    }
                }
            }
        }

        // 解析 actions
        List<?> actionsList = config.getList("actions");
        if (actionsList != null) {
            for (Object actionObj : actionsList) {
                if (actionObj instanceof Map<?, ?> actionMap) {
                    ConfigurationSection actionConfig = BukkitConfigHelper.map2ConfigSection(actionMap);
                    String type = actionConfig.getString("type");
                    if ("destroy".equals(type)) {
                        boolean dropItems = actionConfig.getBoolean("drop_items", true);
                        builder.action(result -> {
                            for (Block matchedBlock : result.getMatchedBlocks()) {
                                if (dropItems) {
                                    matchedBlock.breakNaturally();
                                } else {
                                    matchedBlock.setType(Material.AIR);
                                }
                            }
                        });
                    } else if ("message".equals(type)) {
                        String text = actionConfig.getString("text", "");
                        builder.action(result -> {
                            if (result.getOrigin().getWorld() != null) {
                                result.getOrigin().getWorld().getPlayers().forEach(player ->
                                        BukkitPlayer.byPlayer(player).sendMsg(text)
                                );
                            }
                        });
                    }
                }
            }
        }

        return builder.build();
    }
}
