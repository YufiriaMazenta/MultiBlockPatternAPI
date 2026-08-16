package pers.yufiria.multiblockpatternapi.impl;

import crypticlib.CrypticLib;
import crypticlib.util.BukkitConfigHelper;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import pers.yufiria.multiblockpatternapi.api.BlockMatcher;
import pers.yufiria.multiblockpatternapi.api.MultiBlockPattern;
import pers.yufiria.multiblockpatternapi.api.PatternBuilder;
import pers.yufiria.multiblockpatternapi.api.ActionType;
import pers.yufiria.multiblockpatternapi.api.PatternConfigParser;
import pers.yufiria.multiblockpatternapi.api.TriggerType;
import pers.yufiria.multiblockpatternapi.impl.action.DestroyStructure;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public enum SimplePatternConfigParser implements PatternConfigParser {

    INSTANCE;

    private final Map<String, ActionType> actionTypeMap = new ConcurrentHashMap<>();

    SimplePatternConfigParser() {
        registerActionType(DestroyStructure.INSTANCE);
    }

    @Override
    public MultiBlockPattern parse(String patternId, ConfigurationSection config) {
        String displayName = config.getString("display_name", patternId);
        boolean rotation = config.getBoolean("rotation", false);
        String directionStr = config.getString("direction", "horizontal");
        MultiBlockPattern.Direction direction = "vertical".equalsIgnoreCase(directionStr)
            ? MultiBlockPattern.Direction.VERTICAL
            : MultiBlockPattern.Direction.HORIZONTAL;

        // 解析触发模式
        String triggerModeStr = config.getString("trigger_type", "block_change");
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

        ConfigurationSection blocksConfig = config.getConfigurationSection("blocks");
        if (blocksConfig != null) {
            for (String key : blocksConfig.getKeys(false)) {
                String cleanKey = key.replace("\"", "").replace("'", "");
                if (cleanKey.length() != 1) continue;
                char c = cleanKey.charAt(0);
                String materialName = blocksConfig.getString(key);
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
                if (blocksConfig != null) {
                    String materialName = blocksConfig.getString(triggerStr);
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
                    ActionType actionType = actionTypeMap.get(type);
                    if (actionType != null) {
                        builder.action(actionType.createAction(actionConfig));
                    } else {
                        CrypticLib.info("&cUnknown action type: " + type);
                    }
                }
            }
        }

        return builder.build();
    }

    public void registerActionType(@NotNull ActionType actionType) {
        Objects.requireNonNull(actionType);
        actionTypeMap.put(actionType.typeId(), actionType);
    }

    public Optional<ActionType> getRegisteredActionType(String typeId) {
        return Optional.ofNullable(actionTypeMap.get(typeId));
    }

    public void clearRegisteredActionTypes() {
        actionTypeMap.clear();
    }

}
