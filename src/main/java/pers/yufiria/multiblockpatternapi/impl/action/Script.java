package pers.yufiria.multiblockpatternapi.impl.action;

import crypticlib.CrypticLib;
import crypticlib.script.ScriptContext;
import crypticlib.script.ScriptEngine;
import crypticlib.script.ScriptValue;
import crypticlib.script.compile.CompiledScript;
import crypticlib.script.object.ReflectPropertyResolver;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import pers.yufiria.multiblockpatternapi.api.Action;
import pers.yufiria.multiblockpatternapi.api.ActionType;

import java.util.List;

public enum Script implements ActionType {

    INSTANCE;

    @Override
    public String typeId() {
        return "script";
    }

    @Override
    public Action createAction(ConfigurationSection config) {
        if (!config.isList("script")) {
            return result -> {};
        } else {
            List<String> scriptLines = config.getStringList("script");
            CompiledScript compiledScript = ScriptEngine.INSTANCE.compile("action_script_" + System.nanoTime(), String.join("\n", scriptLines));
            return result -> {
                if (result.getCauser() == null) {
                    CrypticLib.info("&e[MBP] Script action skipped: no causer (triggered by non-player event)");
                    return;
                }
                ScriptContext context = new ScriptContext(result.getCauser());
                Location origin = result.getOrigin();
                context.setVariable("world", ScriptValue.of(origin.getWorld().getName()));
                context.setVariable("x", ScriptValue.of(origin.getX()));
                context.setVariable("y", ScriptValue.of(origin.getY()));
                context.setVariable("z", ScriptValue.of(origin.getZ()));
                context.setVariable("location", ScriptValue.of(origin.clone(), ReflectPropertyResolver.INSTANCE));
                context.setVariable("result", ScriptValue.of(result, ReflectPropertyResolver.INSTANCE));
                compiledScript.execute(context);
            };
        }
    }

}
