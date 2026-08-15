package pers.yufiria.multiblockpatternapi.command;

import crypticlib.command.CommandNode;
import crypticlib.Invoker;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import pers.yufiria.multiblockpatternapi.api.MatchResult;
import pers.yufiria.multiblockpatternapi.api.MultiBlockPattern;
import pers.yufiria.multiblockpatternapi.config.Messages;
import pers.yufiria.multiblockpatternapi.registry.PatternRegistryImpl;

import java.util.List;
import java.util.Map;

public class TestSubCommand extends CommandNode {

    public TestSubCommand() {
        super("test");
    }

    @Override
    public void execute(Invoker invoker, List<String> args) {
        if (!invoker.isPlayer()) {
            invoker.sendMsg(Messages.PREFIX.value() + Messages.ONLY_PLAYER.value());
            return;
        }

        Player player = invoker.asPlayer().getPlatformPlayer(Bukkit::getPlayer).orElse(null);
        if (player == null) {
            throw new NullPointerException("Can not get player: " + invoker.name());
        }
        Map<String, MultiBlockPattern> patterns = PatternRegistryImpl.getInstance().getAll();

        if (patterns.isEmpty()) {
            invoker.sendMsg(Messages.PREFIX.value() + Messages.NO_PATTERNS.value());
            return;
        }

        int matchCount = 0;
        for (MultiBlockPattern pattern : patterns.values()) {
            MatchResult result = pattern.checkMatch(player.getLocation());
            if (result.isMatch()) {
                matchCount++;
                String msg = Messages.MATCH_FOUND.value()
                    .replace("%name%", pattern.getDisplayName());
                invoker.sendMsg(Messages.PREFIX.value() + msg);
                result.execute();
            }
        }

        if (matchCount == 0) {
            invoker.sendMsg(Messages.PREFIX.value() + Messages.NO_MATCH.value());
        } else {
            String msg = Messages.MATCH_SUMMARY.value()
                .replace("%count%", String.valueOf(matchCount));
            invoker.sendMsg(Messages.PREFIX.value() + msg);
        }
    }

    @Override
    public List<String> tabComplete(Invoker invoker, List<String> args) {
        return List.of();
    }
}
