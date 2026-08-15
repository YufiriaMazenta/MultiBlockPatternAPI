package pers.yufiria.multiblockpatternapi.registry;

import crypticlib.CrypticLib;
import crypticlib.config.BukkitConfigWrapper;
import crypticlib.util.BukkitConfigHelper;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import pers.yufiria.multiblockpatternapi.api.MultiBlockPattern;
import pers.yufiria.multiblockpatternapi.api.PatternBuilder;
import pers.yufiria.multiblockpatternapi.config.Messages;
import pers.yufiria.multiblockpatternapi.impl.PatternMatcherImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PatternRegistryImpl {

    private static final PatternRegistryImpl INSTANCE = new PatternRegistryImpl();
    private final PatternMatcherImpl matcher = PatternMatcherImpl.getInstance();
    private BukkitConfigWrapper patternsWrapper;

    private PatternRegistryImpl() {}

    public static PatternRegistryImpl getInstance() {
        return INSTANCE;
    }

    public void setPatternsWrapper(BukkitConfigWrapper wrapper) {
        this.patternsWrapper = wrapper;
    }

    public void register(MultiBlockPattern pattern) {
        matcher.registerPattern(pattern);
    }

    public void unregister(String patternId) {
        matcher.unregisterPattern(patternId);
    }

    public MultiBlockPattern get(String patternId) {
        return matcher.getPattern(patternId);
    }

    public Map<String, MultiBlockPattern> getAll() {
        return matcher.getAllPatterns();
    }

    public void clear() {
        for (String id : new ArrayList<>(matcher.getAllPatterns().keySet())) {
            matcher.unregisterPattern(id);
        }
    }

    /**
     * 获取包含指定触发方块的所有模式
     */
    public List<MultiBlockPattern> getPatternsByTrigger(Material material) {
        return matcher.getAllPatterns().values().stream()
            .filter(p -> p.hasTrigger() && p.getTriggerMaterial() == material)
            .collect(Collectors.toList());
    }

    /**
     * 获取所有没有触发方块的模式
     */
    public List<MultiBlockPattern> getPatternsWithoutTrigger() {
        return matcher.getAllPatterns().values().stream()
            .filter(p -> !p.hasTrigger())
            .collect(Collectors.toList());
    }

    public void loadPatterns() {
        if (patternsWrapper == null) return;

        ConfigurationSection root = patternsWrapper.config();
        if (root == null) return;

        for (String patternId : root.getKeys(false)) {
            ConfigurationSection patternSection = root.getConfigurationSection(patternId);
            if (patternSection == null) continue;

            // 跳过非模式节点
            if (!patternSection.contains("layer") && !patternSection.contains("layers")) {
                continue;
            }

            try {
                MultiBlockPattern pattern = parsePattern(patternId, patternSection);
                register(pattern);
                String msg = Messages.PATTERN_LOADED.value().replace("%name%", pattern.getDisplayName());
                CrypticLib.info(msg);
            } catch (Exception e) {
                String msg = Messages.PATTERN_LOAD_ERROR.value()
                    .replace("%name%", patternId)
                    .replace("%error%", e.getMessage());
                CrypticLib.info("&e" + msg);
            }
        }
    }

    public void reloadPatterns() {
        if (patternsWrapper == null) return;

        clear();
        patternsWrapper.reloadConfig();
        loadPatterns();
    }

    private MultiBlockPattern parsePattern(String id, ConfigurationSection section) {
        String displayName = section.getString("display_name", id);
        boolean rotation = section.getBoolean("rotation", false);
        String directionStr = section.getString("direction", "horizontal");
        MultiBlockPattern.Direction direction = "vertical".equalsIgnoreCase(directionStr)
            ? MultiBlockPattern.Direction.VERTICAL
            : MultiBlockPattern.Direction.HORIZONTAL;

        PatternBuilder builder = PatternBuilder.create(id)
            .displayName(displayName)
            .rotationEnabled(rotation)
            .direction(direction);

        if (section.contains("layer")) {
            List<String> layer = section.getStringList("layer");
            if (!layer.isEmpty()) {
                builder.layer(layer.toArray(new String[0]));
            }
        }

        if (section.contains("layers")) {
            List<?> layers = section.getList("layers");
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

        ConfigurationSection mapSection = section.getConfigurationSection("map");
        if (mapSection != null) {
            CrypticLib.debug("[MBP] Parsing map for pattern: " + id);
            for (String key : mapSection.getKeys(false)) {
                String cleanKey = key.replace("\"", "").replace("'", "");
                CrypticLib.debug("[MBP] Map key: '" + key + "' -> cleanKey: '" + cleanKey + "'");
                if (cleanKey.length() != 1) continue;
                char c = cleanKey.charAt(0);
                String materialName = mapSection.getString(key);
                CrypticLib.debug("[MBP] Material name: " + materialName);
                if (materialName != null) {
                    Material material = Material.matchMaterial(materialName);
                    CrypticLib.debug("[MBP] Resolved material: " + material);
                    if (material != null) {
                        builder.map(c, material);
                    }
                }
            }
        }

        // 解析触发方块
        if (section.contains("trigger")) {
            String triggerStr = section.getString("trigger", "");
            if (triggerStr.length() == 1) {
                char triggerChar = triggerStr.charAt(0);
                // 从map中获取对应的Material
                Material triggerMaterial = null;
                if (mapSection != null) {
                    String materialName = mapSection.getString(triggerStr);
                    if (materialName != null) {
                        triggerMaterial = Material.matchMaterial(materialName);
                    }
                }
                if (triggerMaterial != null) {
                    builder.trigger(triggerChar, triggerMaterial);
                }
            }
        }

        // 解析 actions
        List<?> actionsList = section.getList("actions");
        if (actionsList != null) {
            for (Object actionObj : actionsList) {
                if (actionObj instanceof Map<?, ?> actionMap) {
                    ConfigurationSection config = BukkitConfigHelper.map2ConfigSection(actionMap);
                    String type = config.getString("type");
                    if ("destroy".equals(type)) {
                        boolean dropItems = config.getBoolean("drop_items", true);
                        builder.action(result -> result.destroy(dropItems));
                    } else if ("message".equals(type)) {
                        String text = config.getString("text", "");
                        builder.action(result -> {
                            if (result.getOrigin().getWorld() != null) {
                                result.getOrigin().getWorld().getPlayers().forEach(player -> {
                                    player.sendMessage(text);
                                });
                            }
                        });
                    }
                }
            }
        }

        return builder.build();
    }
}
