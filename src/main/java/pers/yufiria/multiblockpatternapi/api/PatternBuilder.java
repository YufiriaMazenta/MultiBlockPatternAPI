package pers.yufiria.multiblockpatternapi.api;

import org.bukkit.Material;
import org.bukkit.block.Block;
import pers.yufiria.multiblockpatternapi.impl.MultiBlockPatternImpl;
import pers.yufiria.multiblockpatternapi.util.BlockVector;

import crypticlib.CrypticLib;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public class PatternBuilder {

    public static final char WILDCARD = ' ';

    private final String id;
    private String displayName;
    private final List<List<String>> layers = new ArrayList<>();
    private final Map<Character, Material> materialMap = new HashMap<>();
    private final Map<Character, Predicate<Block>> predicateMap = new HashMap<>();
    private final Map<Character, String> predicateDescMap = new HashMap<>();
    private final List<PatternAction> actions = new ArrayList<>();
    private boolean rotationEnabled = false;
    private MultiBlockPattern.Direction direction = MultiBlockPattern.Direction.HORIZONTAL;

    private char triggerChar = '\0';
    private Material triggerMaterial = null;

    private PatternBuilder(String id) {
        this.id = id;
    }

    public static PatternBuilder create(String id) {
        return new PatternBuilder(id);
    }

    public PatternBuilder displayName(String displayName) {
        this.displayName = displayName;
        return this;
    }

    public PatternBuilder layer(String... rows) {
        List<String> layer = new ArrayList<>();
        for (String row : rows) {
            layer.add(row);
        }
        layers.add(layer);
        return this;
    }

    public PatternBuilder map(char c, Material material) {
        materialMap.put(c, material);
        return this;
    }

    public PatternBuilder mapPred(char c, Predicate<Block> predicate, String description) {
        predicateMap.put(c, predicate);
        predicateDescMap.put(c, description);
        return this;
    }

    public PatternBuilder action(PatternAction action) {
        actions.add(action);
        return this;
    }

    public PatternBuilder rotationEnabled(boolean enabled) {
        this.rotationEnabled = enabled;
        return this;
    }

    public PatternBuilder direction(MultiBlockPattern.Direction direction) {
        this.direction = direction;
        return this;
    }

    public PatternBuilder trigger(char c, Material material) {
        this.triggerChar = c;
        this.triggerMaterial = material;
        this.materialMap.put(c, material);
        return this;
    }

    public MultiBlockPattern build() {
        if (layers.isEmpty()) {
            throw new IllegalStateException("Pattern must have at least one layer");
        }

        Map<BlockVector, Character> offsetCharMap = new LinkedHashMap<>();
        Map<Character, Object> charMap = new HashMap<>();
        BlockVector triggerOffset = null;

        charMap.put('_', Material.AIR);
        charMap.put(WILDCARD, null);
        charMap.putAll(materialMap);
        charMap.putAll(predicateMap);

        CrypticLib.debug("[MBP] Building pattern: " + id + " direction: " + direction);

        for (int layerIndex = 0; layerIndex < layers.size(); layerIndex++) {
            List<String> layer = layers.get(layerIndex);
            for (int rowIndex = 0; rowIndex < layer.size(); rowIndex++) {
                String row = layer.get(rowIndex);
                for (int colIndex = 0; colIndex < row.length(); colIndex++) {
                    char c = row.charAt(colIndex);

                    if (!charMap.containsKey(c)) {
                        throw new IllegalStateException("Unknown character '" + c + "' in pattern.");
                    }

                    if (c != WILDCARD) {
                        BlockVector offset;
                        if (direction == MultiBlockPattern.Direction.VERTICAL) {
                            // 竖直方向: X=列, Y=行(从上到下), Z=层
                            // 但为了方便，Y从下到上，所以需要翻转
                            int maxRow = layer.size() - 1;
                            offset = new BlockVector(colIndex, maxRow - rowIndex, layerIndex);
                        } else {
                            // 水平方向: X=列, Y=层, Z=行
                            offset = new BlockVector(colIndex, layerIndex, rowIndex);
                        }
                        offsetCharMap.put(offset, c);
                        if (c == triggerChar) {
                            triggerOffset = offset;
                        }
                    }
                }
            }
        }

        CrypticLib.debug("[MBP] offsetCharMap size: " + offsetCharMap.size());
        CrypticLib.debug("[MBP] offsetCharMap: " + offsetCharMap);

        return new MultiBlockPatternImpl(
            id,
            displayName != null ? displayName : id,
            offsetCharMap,
            charMap,
            new ArrayList<>(actions),
            rotationEnabled,
            direction,
            new HashMap<>(materialMap),
            new HashMap<>(predicateMap),
            new HashMap<>(predicateDescMap),
            triggerMaterial,
            triggerOffset
        );
    }
}
