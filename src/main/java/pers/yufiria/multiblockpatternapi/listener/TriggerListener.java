package pers.yufiria.multiblockpatternapi.listener;

import crypticlib.CrypticLib;
import crypticlib.listener.EventListener;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import pers.yufiria.multiblockpatternapi.api.MatchResult;
import pers.yufiria.multiblockpatternapi.api.MultiBlockPattern;
import pers.yufiria.multiblockpatternapi.api.RotationSupport;
import pers.yufiria.multiblockpatternapi.event.MultiblockMatchEvent;
import pers.yufiria.multiblockpatternapi.registry.PatternRegistryImpl;
import pers.yufiria.multiblockpatternapi.util.BlockVector;

import java.util.List;
import java.util.Map;

@EventListener
public class TriggerListener implements Listener {

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();

        CrypticLib.debug("[MBP] Block placed: " + block.getType() + " at " + block.getX() + "," + block.getY() + "," + block.getZ());

        // 1. 检查有触发方块的模式
        List<MultiBlockPattern> triggerPatterns = PatternRegistryImpl.getInstance().getPatternsByTrigger(block.getType());
        CrypticLib.debug("[MBP] Trigger patterns found: " + triggerPatterns.size());
        for (MultiBlockPattern pattern : triggerPatterns) {
            BlockVector triggerOffset = pattern.getTriggerOffset();
            Location origin = block.getLocation().subtract(
                triggerOffset.getX(),
                triggerOffset.getY(),
                triggerOffset.getZ()
            );

            CrypticLib.debug("[MBP] Checking trigger pattern: " + pattern.getId() + " at origin " + origin.getX() + "," + origin.getY() + "," + origin.getZ());
            if (checkAndTrigger(pattern, origin, player)) {
                return;
            }
        }

        // 2. 检查没有触发方块的模式
        List<MultiBlockPattern> autoPatterns = PatternRegistryImpl.getInstance().getPatternsWithoutTrigger();
        CrypticLib.debug("[MBP] Auto patterns found: " + autoPatterns.size());
        for (MultiBlockPattern pattern : autoPatterns) {
            // 检查放置的方块是否在模式中
            if (!isBlockInPattern(block.getType(), pattern)) {
                continue;
            }

            CrypticLib.debug("[MBP] Checking auto pattern: " + pattern.getId());
            // 尝试所有可能的偏移位置来计算原点
            for (Map.Entry<BlockVector, Character> entry : pattern.getOffsetCharMap().entrySet()) {
                BlockVector offset = entry.getKey();
                Location origin = block.getLocation().subtract(
                    offset.getX(),
                    offset.getY(),
                    offset.getZ()
                );

                if (checkAndTrigger(pattern, origin, player)) {
                    return;
                }
            }
        }
    }

    private boolean isBlockInPattern(Material material, MultiBlockPattern pattern) {
        for (Object obj : pattern.getCharMap().values()) {
            if (obj instanceof Material m && m == material) {
                return true;
            }
        }
        return false;
    }

    private boolean checkAndTrigger(MultiBlockPattern pattern, Location origin, Player player) {
        if (pattern.isRotationEnabled()) {
            for (RotationSupport.Rotation rotation : RotationSupport.Rotation.values()) {
                MatchResult result = pattern.checkMatch(origin, rotation, RotationSupport.Mirror.NONE);
                if (result.isMatch()) {
                    CrypticLib.debug("[MBP] Pattern matched: " + pattern.getId() + " with rotation " + rotation);
                    triggerMatch(result, player);
                    return true;
                }
            }
        } else {
            MatchResult result = pattern.checkMatch(origin);
            if (result.isMatch()) {
                CrypticLib.debug("[MBP] Pattern matched: " + pattern.getId());
                triggerMatch(result, player);
                return true;
            }
        }
        return false;
    }

    private void triggerMatch(MatchResult result, Player player) {
        result.execute();
        MultiblockMatchEvent matchEvent = new MultiblockMatchEvent(result, player);
        org.bukkit.Bukkit.getPluginManager().callEvent(matchEvent);
    }
}
