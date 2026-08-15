package pers.yufiria.multiblockpatternapi.command;

import crypticlib.command.CommandNode;
import crypticlib.Invoker;
import pers.yufiria.multiblockpatternapi.config.Messages;
import pers.yufiria.multiblockpatternapi.registry.PatternRegistryImpl;

import java.util.List;

public class ReloadSubCommand extends CommandNode {

    public ReloadSubCommand() {
        super("reload");
    }

    @Override
    public void execute(Invoker invoker, List<String> args) {
        // 清空已注册的模式
        PatternRegistryImpl.getInstance().clear();

        // 重新加载配置
        PatternRegistryImpl.getInstance().reloadPatterns();

        invoker.sendMsg(Messages.PREFIX.value() + Messages.RELOAD_SUCCESS.value());
    }

    @Override
    public List<String> tabComplete(Invoker invoker, List<String> args) {
        return List.of();
    }
}
