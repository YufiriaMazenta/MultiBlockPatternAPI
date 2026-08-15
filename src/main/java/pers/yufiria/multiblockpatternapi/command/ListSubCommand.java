package pers.yufiria.multiblockpatternapi.command;

import crypticlib.Invoker;
import crypticlib.command.CommandNode;
import pers.yufiria.multiblockpatternapi.api.MultiBlockPattern;
import pers.yufiria.multiblockpatternapi.config.Messages;
import pers.yufiria.multiblockpatternapi.registry.PatternRegistryImpl;

import java.util.List;
import java.util.Map;

public class ListSubCommand extends CommandNode {

    public ListSubCommand() {
        super("list");
    }

    @Override
    public void execute(Invoker invoker, List<String> args) {
        Map<String, MultiBlockPattern> patterns = PatternRegistryImpl.getInstance().getAll();

        if (patterns.isEmpty()) {
            invoker.sendMsg(Messages.PREFIX.value() + Messages.NO_PATTERNS.value());
            return;
        }

        invoker.sendMsg(Messages.PREFIX.value() + Messages.PATTERN_LIST_HEADER.value());
        for (Map.Entry<String, MultiBlockPattern> entry : patterns.entrySet()) {
            MultiBlockPattern pattern = entry.getValue();
            String msg = Messages.PATTERN_LIST_ENTRY.value()
                .replace("%id%", pattern.getId())
                .replace("%name%", pattern.getDisplayName());
            invoker.sendMsg(Messages.PREFIX.value() + msg);
        }
    }

    @Override
    public List<String> tabComplete(Invoker invoker, List<String> args) {
        return List.of();
    }
}
