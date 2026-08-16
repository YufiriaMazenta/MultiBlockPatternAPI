package pers.yufiria.multiblockpatternapi.command;

import crypticlib.Invoker;
import crypticlib.command.CommandNode;
import crypticlib.command.CommandTree;
import crypticlib.command.annotation.Command;
import crypticlib.command.annotation.Subcommand;
import crypticlib.perm.PermInfo;
import org.jetbrains.annotations.NotNull;
import pers.yufiria.multiblockpatternapi.PluginMain;
import pers.yufiria.multiblockpatternapi.api.MultiBlockPattern;
import pers.yufiria.multiblockpatternapi.config.Languages;
import pers.yufiria.multiblockpatternapi.registry.PatternRegistry;

import java.util.List;
import java.util.Map;

@Command
public class MainCommand extends CommandTree {

    public static final MainCommand INSTANCE = new MainCommand();

    private MainCommand() {
        super("mbp", new PermInfo("mbp.admin"), List.of("multiblockpattern"));
    }

    @Subcommand
    private final CommandNode listCmd = new CommandNode("list") {
        @Override
        public void execute(@NotNull Invoker invoker, @NotNull List<String> args) {
            Map<String, MultiBlockPattern> patterns = PatternRegistry.INSTANCE.getAll();

            if (patterns.isEmpty()) {
                invoker.sendMsg(Languages.PREFIX.value() + Languages.COMMAND_LIST_NO_PATTERNS.value());
                return;
            }

            invoker.sendMsg(Languages.PREFIX.value() + Languages.COMMAND_LIST_PATTERN_LIST_HEADER.value());
            for (Map.Entry<String, MultiBlockPattern> entry : patterns.entrySet()) {
                MultiBlockPattern pattern = entry.getValue();
                String msg = Languages.COMMAND_LIST_PATTERN_LIST_ENTRY.value()
                    .replace("%id%", pattern.getId())
                    .replace("%name%", pattern.getDisplayName());
                invoker.sendMsg(Languages.PREFIX.value() + msg);
            }
        }
    };

    @Subcommand
    private final CommandNode reloadCmd = new CommandNode("reload") {
        @Override
        public void execute(Invoker invoker, @NotNull List<String> args) {
            PluginMain.instance().reloadPlugin();
            invoker.sendMsg(Languages.PREFIX.value() + Languages.COMMAND_RELOAD_SUCCESS.value());
        }
    };

}
