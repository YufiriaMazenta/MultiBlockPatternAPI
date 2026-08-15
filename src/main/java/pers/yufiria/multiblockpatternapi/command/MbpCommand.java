package pers.yufiria.multiblockpatternapi.command;

import crypticlib.command.CommandTree;
import crypticlib.command.annotation.Command;
import crypticlib.perm.PermInfo;

import java.util.List;

@Command
public class MbpCommand extends CommandTree {

    @crypticlib.command.annotation.Subcommand
    private final ListSubCommand listCmd = new ListSubCommand();

    @crypticlib.command.annotation.Subcommand
    private final TestSubCommand testCmd = new TestSubCommand();

    @crypticlib.command.annotation.Subcommand
    private final ReloadSubCommand reloadCmd = new ReloadSubCommand();

    public MbpCommand() {
        super("mbp", new PermInfo("mbp.admin"), List.of("multiblockpattern"));
    }
}
