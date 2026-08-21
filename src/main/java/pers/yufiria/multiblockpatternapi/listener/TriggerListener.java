package pers.yufiria.multiblockpatternapi.listener;

import crypticlib.*;
import crypticlib.listener.EventListener;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.util.Vector;
import pers.yufiria.multiblockpatternapi.PluginMain;
import pers.yufiria.multiblockpatternapi.api.*;
import pers.yufiria.multiblockpatternapi.api.event.MultiblockMatchEvent;
import pers.yufiria.multiblockpatternapi.impl.SimplePatternMatcher;
import pers.yufiria.multiblockpatternapi.registry.PatternRegistry;
import pers.yufiria.multiblockpatternapi.util.BlockVector;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@EventListener
public enum TriggerListener implements Listener {

    INSTANCE;

    private RotationSupport rotationSupport() {
        return SimplePatternMatcher.INSTANCE.rotationSupport();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockPlace(BlockPlaceEvent event) {
        if (event.isCancelled()) return;
        handleBlockChange(event.getBlock(), BukkitPlayer.byPlayer(event.getPlayer()));
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityBlockForm(EntityBlockFormEvent event) {
        handleBlockChange(event.getBlock(), BukkitInvoker.byCommandSender(event.getEntity()));
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPistonExtend(BlockPistonExtendEvent event) {
        if (event.isCancelled()) return;
        List<Block> blocks = event.getBlocks();
        Vector direction = event.getDirection().getDirection();
        Location pistonLoc = event.getBlock().getLocation();
        // 延迟到下一 tick，此时方块已经移动到位
        CrypticLibBukkit.scheduler().runOnLocation(pistonLoc, () -> {
            for (Block block : blocks) {
                Block movedBlock = block.getRelative((int) direction.getX(), (int) direction.getY(), (int) direction.getZ());
                handleBlockChange(movedBlock, PluginMain.instance().getConsoleInvoker());
            }
        });
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPistonRetract(BlockPistonRetractEvent event) {
        if (event.isCancelled()) return;
        if (!event.isSticky()) return;
        Vector direction = event.getDirection().getDirection();
        Location pistonLoc = event.getBlock().getLocation();
        // 延迟到下一 tick，此时方块已经移动到位
        CrypticLibBukkit.scheduler().runOnLocation(pistonLoc, () -> {
            Block block = event.getBlock().getRelative((int) direction.getX(), (int) direction.getY(), (int) direction.getZ());
            handleBlockChange(block, PluginMain.instance().getConsoleInvoker());
        });
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.useInteractedBlock() == Event.Result.DENY) return;
        if (event.getHand() != EquipmentSlot.HAND) return;

        Player player = event.getPlayer();
        Block clickedBlock = event.getClickedBlock();
        if (clickedBlock == null) return;

        if (CrypticLib.debug) {
            CrypticLib.debug("[MBP] Player interact: " + player.getName() + " right-clicked " + clickedBlock.getType() + " at " + clickedBlock.getX() + "," + clickedBlock.getY() + "," + clickedBlock.getZ());
        }

        List<MultiBlockPattern> patterns = PatternRegistry.INSTANCE.getPatternsByInteraction();
        if (CrypticLib.debug) {
            CrypticLib.debug("[MBP] Interaction patterns found: " + patterns.size());
        }

        for (MultiBlockPattern pattern : patterns) {
            // 检查触发条件
            var condition = pattern.getInternalCondition();
            if (condition != null && !condition.test(clickedBlock, player)) {
                if (CrypticLib.debug) {
                    CrypticLib.debug("[MBP] Pattern " + pattern.getId() + " condition failed");
                }
                continue;
            }

            // 检查触发方块匹配
            BlockMatcher triggerMatcher = pattern.getTriggerMatcher();
            if (triggerMatcher != null && !triggerMatcher.matches(clickedBlock)) {
                if (CrypticLib.debug) {
                    CrypticLib.debug("[MBP] Pattern " + pattern.getId() + " trigger matcher failed: " + clickedBlock.getType());
                }
                continue;
            }

            if (CrypticLib.debug) {
                CrypticLib.debug("[MBP] Pattern " + pattern.getId() + " checks passed, finding origin...");
            }

            // 收集所有触发字符的位置
            char triggerChar = pattern.getTriggerChar();
            List<BlockVector> triggerOffsets = new ArrayList<>();
            for (Map.Entry<BlockVector, Character> entry : pattern.getOffsetCharMap().entrySet()) {
                if (entry.getValue() == triggerChar) {
                    triggerOffsets.add(entry.getKey());
                }
            }

            // 尝试每个触发位置，找到能匹配的那个
            for (BlockVector offset : triggerOffsets) {
                if (checkAndTriggerWithOffset(pattern, offset, BukkitPlayer.byPlayer(player), clickedBlock)) {
                    break;
                }
            }
        }
    }

    private void handleBlockChange(Block block, Invoker trigger) {
        if (CrypticLib.debug) {
            CrypticLib.debug("[MBP] Block changed: " + block.getType() + " at " + block.getX() + "," + block.getY() + "," + block.getZ());
        }

        // 1. 检查有触发方块的模式（BLOCK_CHANGE类型）
        List<MultiBlockPattern> triggerPatterns = PatternRegistry.INSTANCE.getPatternsByTrigger(block);
        if (CrypticLib.debug) {
            CrypticLib.debug("[MBP] Trigger patterns found: " + triggerPatterns.size());
        }
        for (MultiBlockPattern pattern : triggerPatterns) {
            if (pattern.getTriggerType() != TriggerType.BLOCK_CHANGE) continue;

            if (CrypticLib.debug) {
                CrypticLib.debug("[MBP] Checking trigger pattern: " + pattern.getId());
            }
            if (checkAndTriggerWithTriggerBlock(pattern, trigger, block)) {
                return;
            }
        }

        // 2. 检查没有触发方块的模式
        List<MultiBlockPattern> autoPatterns = PatternRegistry.INSTANCE.getPatternsWithoutTrigger();
        if (CrypticLib.debug) {
            CrypticLib.debug("[MBP] Auto patterns found: " + autoPatterns.size());
        }
        for (MultiBlockPattern pattern : autoPatterns) {
            if (!isBlockInPattern(block, pattern)) continue;

            if (CrypticLib.debug) {
                CrypticLib.debug("[MBP] Checking auto pattern: " + pattern.getId());
            }
            for (Map.Entry<BlockVector, Character> entry : pattern.getOffsetCharMap().entrySet()) {
                BlockVector offset = entry.getKey();
                Location origin = block.getLocation().subtract(
                    offset.x(),
                    offset.y(),
                    offset.z()
                );

                if (checkAndTriggerAtOrigin(pattern, origin, trigger, block)) {
                    return;
                }
            }
        }
    }

    private boolean isBlockInPattern(Block block, MultiBlockPattern pattern) {
        for (BlockMatcher matcher : pattern.getCharMatcherMap().values()) {
            if (matcher.matches(block)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 基于触发方块和触发偏移量计算原点，支持旋转匹配。
     * 每个旋转方向使用不同的原点（旋转后的偏移量）。
     */
    private boolean checkAndTriggerWithTriggerBlock(MultiBlockPattern pattern, Invoker trigger, Block triggerBlock) {
        BlockVector triggerOffset = pattern.getTriggerOffset();
        if (triggerOffset == null) return false;

        if (pattern.isRotationEnabled()) {
            RotationSupport rotSupport = rotationSupport();
            for (RotationSupport.Rotation rotation : RotationSupport.Rotation.values()) {
                BlockVector rotatedOffset = rotSupport.applyTransform(triggerOffset, rotation, RotationSupport.Mirror.NONE);
                Location origin = triggerBlock.getLocation().subtract(rotatedOffset.x(), rotatedOffset.y(), rotatedOffset.z());
                MatchResult result = pattern.checkMatch(origin, rotation, RotationSupport.Mirror.NONE);
                if (result.isMatch()) {
                    if (CrypticLib.debug) {
                        CrypticLib.debug("[MBP] Pattern matched: " + pattern.getId() + " with rotation " + rotation);
                    }
                    triggerMatch(result, trigger, triggerBlock);
                    return true;
                }
            }
        } else {
            Location origin = triggerBlock.getLocation().subtract(triggerOffset.x(), triggerOffset.y(), triggerOffset.z());
            MatchResult result = pattern.checkMatch(origin);
            if (result.isMatch()) {
                if (CrypticLib.debug) {
                    CrypticLib.debug("[MBP] Pattern matched: " + pattern.getId());
                }
                triggerMatch(result, trigger, triggerBlock);
                return true;
            }
        }
        return false;
    }

    /**
     * 基于指定的触发偏移量计算原点（用于交互触发中遍历多个触发位置）。
     * 每个旋转方向使用不同的原点。
     */
    private boolean checkAndTriggerWithOffset(MultiBlockPattern pattern, BlockVector offset, Invoker trigger, Block triggerBlock) {
        if (pattern.isRotationEnabled()) {
            RotationSupport rotSupport = rotationSupport();
            for (RotationSupport.Rotation rotation : RotationSupport.Rotation.values()) {
                BlockVector rotatedOffset = rotSupport.applyTransform(offset, rotation, RotationSupport.Mirror.NONE);
                Location origin = triggerBlock.getLocation().subtract(rotatedOffset.x(), rotatedOffset.y(), rotatedOffset.z());
                if (CrypticLib.debug) {
                    CrypticLib.debug("[MBP] Trying offset: " + offset + " rotation: " + rotation + " -> origin: " + origin.getX() + "," + origin.getY() + "," + origin.getZ());
                }
                MatchResult result = pattern.checkMatch(origin, rotation, RotationSupport.Mirror.NONE);
                if (result.isMatch()) {
                    if (CrypticLib.debug) {
                        CrypticLib.debug("[MBP] Pattern matched: " + pattern.getId() + " with rotation " + rotation);
                    }
                    triggerMatch(result, trigger, triggerBlock);
                    return true;
                }
            }
        } else {
            Location origin = triggerBlock.getLocation().subtract(offset.x(), offset.y(), offset.z());
            if (CrypticLib.debug) {
                CrypticLib.debug("[MBP] Trying offset: " + offset + " -> origin: " + origin.getX() + "," + origin.getY() + "," + origin.getZ());
            }
            MatchResult result = pattern.checkMatch(origin);
            if (result.isMatch()) {
                if (CrypticLib.debug) {
                    CrypticLib.debug("[MBP] Pattern matched: " + pattern.getId());
                }
                triggerMatch(result, trigger, triggerBlock);
                return true;
            }
        }
        return false;
    }

    /**
     * 在已确定的原点处检测匹配（用于无触发方块的 auto pattern）。
     */
    private boolean checkAndTriggerAtOrigin(MultiBlockPattern pattern, Location origin, Invoker trigger, Block triggerBlock) {
        if (pattern.isRotationEnabled()) {
            for (RotationSupport.Rotation rotation : RotationSupport.Rotation.values()) {
                MatchResult result = pattern.checkMatch(origin, rotation, RotationSupport.Mirror.NONE);
                if (result.isMatch()) {
                    if (CrypticLib.debug) {
                        CrypticLib.debug("[MBP] Pattern matched: " + pattern.getId() + " with rotation " + rotation);
                    }
                    triggerMatch(result, trigger, triggerBlock);
                    return true;
                }
            }
        } else {
            MatchResult result = pattern.checkMatch(origin);
            if (result.isMatch()) {
                if (CrypticLib.debug) {
                    CrypticLib.debug("[MBP] Pattern matched: " + pattern.getId());
                }
                triggerMatch(result, trigger, triggerBlock);
                return true;
            }
        }
        return false;
    }

    private void triggerMatch(MatchResult result, Invoker trigger, Block triggerBlock) {
        MatchResult finalResult = result.withTrigger(trigger, triggerBlock);
        finalResult.execute();
        Bukkit.getPluginManager().callEvent(new MultiblockMatchEvent(finalResult));
    }
}
