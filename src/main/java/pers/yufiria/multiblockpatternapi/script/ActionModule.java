package pers.yufiria.multiblockpatternapi.script;

import crypticlib.*;
import crypticlib.chat.BukkitTextProcessor;
import crypticlib.script.ScriptContext;
import crypticlib.script.ScriptValue;
import crypticlib.script.func.ScriptFunctionRegistry;
import crypticlib.script.func.ScriptModule;
import crypticlib.script.vm.ScriptVM;
import crypticlib.util.IOHelper;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent;
import pers.yufiria.multiblockpatternapi.impl.action.Script;

import java.util.Objects;
import java.util.Optional;

/**
 * 内置动作函数模块
 *
 * 使用示例:
 *   command "give %player% diamond 1"
 *   console "say hello"
 *   tell "&aHello!"
 *   take_money 100
 *   give_exp 50
 *   take_level 5
 */
public enum ActionModule implements ScriptModule {

    INSTANCE;

    @Override
    public String moduleName() {
        return "actions";
    }

    @Override
    public void register(ScriptFunctionRegistry registry) {
        String moduleName = moduleName();
        registry.register(moduleName, "summon", this::summon);
        registry.register(moduleName, "command", this::command);
        registry.register(moduleName, "console", this::console);
        registry.register(moduleName, "tell", this::tell);
        registry.register(moduleName, "actionbar", this::actionbar);
        registry.register(moduleName, "title", this::title);
        registry.register(moduleName, "log", this::log);
        registry.register(moduleName, "take_level", this::takeLevel);
        registry.register(moduleName, "give_level", this::giveLevel);
        registry.register(moduleName, "give_exp", this::giveExp);
        registry.register(moduleName, "close", this::close);
        registry.register(moduleName, "discover_recipe", this::discoverRecipe);
        registry.register(moduleName, "undiscover_recipe", this::undiscoverRecipe);
        registry.register(moduleName, "sound", this::sound);
        registry.register(moduleName, "perm", this::perm);
        registry.register(moduleName, "papi", this::papi);
        registry.register(moduleName, "level", this::level);
        registry.register(moduleName, "world", this::world);
        registry.register(moduleName, "gamemode", this::gameMode);
        registry.register(moduleName, "biome", this::biome);
        registry.register(moduleName, "in_water", this::inWater);
        registry.register(moduleName, "in_rain", this::inRain);
        registry.register(moduleName, "light_level", this::lightLevel);
    }

    private ScriptValue summon(ScriptContext ctx, ScriptVM vm, ScriptValue... args) {
        if (args.length < 1) {
            return ScriptValue.of(false);
        }
        Key key = Key.key(args[0].asString());
        if (key == null) {
            return ScriptValue.of(false);
        }
        NamespacedKey entityTypeKey = new NamespacedKey(key.namespace(), key.key());
        EntityType entityType = Registry.ENTITY_TYPE.get(entityTypeKey);
        if (entityType == null) {
            CrypticLib.info("&cUnknown entity type: " + entityTypeKey.asString());
            return ScriptValue.of(false);
        }
        ScriptValue locationVar = ctx.getVariable("location");
        if (!(locationVar instanceof ScriptValue.ObjectValue objectValue)) {
            CrypticLib.info("&cScript context do not have location to summon entity");
            return ScriptValue.of(false);
        }
        Object obj = objectValue.value();
        if (!(obj instanceof Location location)) {
            CrypticLib.info("&clocation variable is not a Location object");
            return ScriptValue.of(false);
        }
        Entity entity = location.getWorld().spawnEntity(location, entityType, CreatureSpawnEvent.SpawnReason.CUSTOM);
        return ScriptValue.of(true);
    }

    private ScriptValue command(ScriptContext ctx, ScriptVM vm, ScriptValue... args) {
        if (args.length < 1) return ScriptValue.of(false);
        Invoker invoker = ctx.invoker();
        StringBuilder sb = new StringBuilder();
        for (ScriptValue arg : args) {
            sb.append(arg.asString());
        }
        String cmd = sb.toString();
        if (invoker.isPlayer()) {
            String finalCmd = cmd;
            cmd = invoker.asPlayer().getPlatformPlayer(Bukkit::getPlayer)
                .map(player -> BukkitTextProcessor.placeholder(player, finalCmd))
                .orElse(cmd);
        }

        CommandSender commandSender = (CommandSender) invoker.platformInvoker();
        if (!CrypticLibBukkit.isFolia()) {
            return ScriptValue.of(Bukkit.dispatchCommand(commandSender, cmd));
        } else {
            //垃圾folia
            String finalCmd = cmd;
            Runnable task = () -> Bukkit.dispatchCommand(commandSender, finalCmd);
            if (commandSender instanceof Entity entity) {
                CrypticLibBukkit.scheduler().runOnEntity(entity, task, task);
            } else {
                CrypticLibBukkit.scheduler().sync(task);
            }
            return ScriptValue.of(true);
        }
    }

    private ScriptValue console(ScriptContext ctx, ScriptVM vm, ScriptValue... args) {
        if (args.length < 1) return ScriptValue.of(false);
        Invoker invoker = ctx.invoker();
        StringBuilder sb = new StringBuilder();
        for (ScriptValue arg : args) {
            sb.append(arg.asString());
        }
        String cmd = sb.toString();
        if (invoker.isPlayer()) {
            String finalCmd = cmd;
            cmd = invoker.asPlayer().getPlatformPlayer(Bukkit::getPlayer)
                .map(player -> BukkitTextProcessor.placeholder(player, finalCmd))
                .orElse(cmd);
        }

        if (!CrypticLibBukkit.isFolia()) {
            return ScriptValue.of(Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd));
        } else {
            String finalCmd1 = cmd;
            CrypticLibBukkit.scheduler().sync(() -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), finalCmd1));
            return ScriptValue.of(true);
        }
    }

    private ScriptValue tell(ScriptContext ctx, ScriptVM vm, ScriptValue... args) {
        if (args.length < 1) return ScriptValue.nil();
        Invoker invoker = ctx.invoker();
        StringBuilder sb = new StringBuilder();
        for (ScriptValue arg : args) {
            sb.append(arg.asString());
        }
        String msg = sb.toString();
        if (invoker.isPlayer()) {
            String finalMsg = msg;
            msg = invoker.asPlayer().getPlatformPlayer(Bukkit::getPlayer)
                .map(player -> BukkitTextProcessor.placeholder(player, finalMsg))
                .orElse(msg);
        }
        invoker.sendMsg(msg);
        return ScriptValue.nil();
    }

    private ScriptValue actionbar(ScriptContext ctx, ScriptVM vm, ScriptValue... args) {
        if (args.length < 1) return ScriptValue.nil();
        Optional<Player> playerOpt = ctx.invoker().asPlayer().getPlatformPlayer(Bukkit::getPlayer);
        if (playerOpt.isEmpty()) {
            return ScriptValue.nil();
        }
        Player player = playerOpt.get();
        StringBuilder sb = new StringBuilder();
        for (ScriptValue arg : args) {
            sb.append(arg.asString());
        }
        String msg = BukkitTextProcessor.placeholder(player, sb.toString());
        BukkitPlayer.byPlayer(player).sendActionBar(msg);
        return ScriptValue.nil();
    }

    private ScriptValue title(ScriptContext ctx, ScriptVM vm, ScriptValue... args) {
        if (args.length < 1) return ScriptValue.nil();
        Optional<Player> playerOpt = ctx.invoker().asPlayer().getPlatformPlayer(Bukkit::getPlayer);
        if (playerOpt.isEmpty()) {
            return ScriptValue.nil();
        }
        Player player = playerOpt.get();
        String title = BukkitTextProcessor.placeholder(player, args[0].asString());
        String subtitle = args.length > 1 ? BukkitTextProcessor.placeholder(player, args[1].asString()) : "";
        BukkitPlayer.byPlayer(player).sendTitle(title, subtitle, 10, 70, 20);
        return ScriptValue.nil();
    }

    private ScriptValue log(ScriptContext ctx, ScriptVM vm, ScriptValue... args) {
        if (args.length < 1) return ScriptValue.nil();
        Optional<Player> playerOpt = ctx.invoker().asPlayer().getPlatformPlayer(Bukkit::getPlayer);
        StringBuilder sb = new StringBuilder();
        for (ScriptValue arg : args) {
            sb.append(arg.asString());
        }
        String msg = BukkitTextProcessor.placeholder(playerOpt.orElse(null), sb.toString());
        CrypticLib.info(msg);
        return ScriptValue.nil();
    }

    private ScriptValue takeLevel(ScriptContext ctx, ScriptVM vm, ScriptValue... args) {
        if (args.length < 1) {
            return ScriptValue.nil();
        }
        Optional<Player> playerOpt = ctx.invoker().asPlayer().getPlatformPlayer(Bukkit::getPlayer);
        if (playerOpt.isEmpty()) {
            return ScriptValue.nil();
        }
        Player player = playerOpt.get();
        int amount = (int) args[0].asNumber();
        player.setLevel(Math.max(0, player.getLevel() - amount));
        return ScriptValue.of(player.getLevel());
    }

    private ScriptValue giveLevel(ScriptContext ctx, ScriptVM vm, ScriptValue... args) {
        if (args.length < 1) {
            return ScriptValue.nil();
        }
        Optional<Player> playerOpt = ctx.invoker().asPlayer().getPlatformPlayer(Bukkit::getPlayer);
        if (playerOpt.isEmpty()) {
            return ScriptValue.nil();
        }
        Player player = playerOpt.get();
        int amount = (int) args[0].asNumber();
        player.setLevel(player.getLevel() + amount);
        return ScriptValue.of(player.getLevel());
    }

    private ScriptValue giveExp(ScriptContext ctx, ScriptVM vm, ScriptValue... args) {
        if (args.length < 1) {
            return ScriptValue.nil();
        }
        Optional<Player> playerOpt = ctx.invoker().asPlayer().getPlatformPlayer(Bukkit::getPlayer);
        if (playerOpt.isEmpty()) {
            return ScriptValue.nil();
        }
        Player player = playerOpt.get();
        int amount = (int) args[0].asNumber();
        player.giveExp(Math.max(0, amount));
        return ScriptValue.of(player.getLevel());
    }



    private ScriptValue close(ScriptContext ctx, ScriptVM vm, ScriptValue... args) {
        Optional<Player> playerOpt = ctx.invoker().asPlayer().getPlatformPlayer(Bukkit::getPlayer);
        if (playerOpt.isEmpty()) {
            return ScriptValue.nil();
        }
        Player player = playerOpt.get();
        player.closeInventory();
        return ScriptValue.nil();
    }

    private ScriptValue discoverRecipe(ScriptContext ctx, ScriptVM vm, ScriptValue... args) {
        if (args.length < 1) return ScriptValue.of(false);
        Optional<Player> playerOpt = ctx.invoker().asPlayer().getPlatformPlayer(Bukkit::getPlayer);
        if (playerOpt.isEmpty()) {
            return ScriptValue.nil();
        }
        Player player = playerOpt.get();
        NamespacedKey recipeKey = NamespacedKey.fromString(args[0].asString());
        return ScriptValue.of(player.discoverRecipe(Objects.requireNonNull(recipeKey)));
    }

    private ScriptValue undiscoverRecipe(ScriptContext ctx, ScriptVM vm, ScriptValue... args) {
        if (args.length < 1) return ScriptValue.of(false);
        Optional<Player> playerOpt = ctx.invoker().asPlayer().getPlatformPlayer(Bukkit::getPlayer);
        if (playerOpt.isEmpty()) {
            return ScriptValue.nil();
        }
        Player player = playerOpt.get();
        NamespacedKey recipeKey = NamespacedKey.fromString(args[0].asString());
        return ScriptValue.of(player.undiscoverRecipe(Objects.requireNonNull(recipeKey)));
    }

    /**
     * sound <sound> [volume] [pitch] → 向玩家播放音频
     * 示例:
     *   sound "entity.experience_orb.pickup"
     *   sound "entity.experience_orb.pickup" 1.0 1.0
     */
    private ScriptValue sound(ScriptContext ctx, ScriptVM vm, ScriptValue... args) {
        if (args.length < 1) return ScriptValue.nil();
        Optional<Player> playerOpt = ctx.invoker().asPlayer().getPlatformPlayer(Bukkit::getPlayer);
        if (playerOpt.isEmpty()) {
            return ScriptValue.nil();
        }
        Player player = playerOpt.get();
        String soundName = args[0].asString();
        float volume = args.length > 1 ? (float) args[1].asNumber() : 1.0f;
        float pitch = args.length > 2 ? (float) args[2].asNumber() : 1.0f;
        if (MinecraftVersion.current().afterOrEquals(MinecraftVersion.V1_21_4)) {
            NamespacedKey soundKey = NamespacedKey.fromString(soundName);
            if (soundKey == null) {
                return ScriptValue.nil();
            }
            Sound sound = Registry.SOUNDS.get(soundKey);
            if (sound == null) {
                return ScriptValue.nil();
            }
            player.playSound(player.getLocation(), sound, volume, pitch);
        } else {
            NamespacedKey soundKey = NamespacedKey.fromString(soundName);
            if (soundKey != null) {
                player.playSound(player.getLocation(), soundKey.getKey(), volume, pitch);
            }
        }
        return ScriptValue.nil();
    }

    private ScriptValue perm(ScriptContext ctx, ScriptVM vm, ScriptValue... args) {
        if (args.length < 1) return ScriptValue.of(false);
        String perm = args[0].asString();
        Optional<Player> playerOpt = ctx.invoker().asPlayer().getPlatformPlayer(Bukkit::getPlayer);
        if (playerOpt.isEmpty()) {
            return ScriptValue.nil();
        }
        Player player = playerOpt.get();
        perm = BukkitTextProcessor.placeholder(player, perm);
        return ScriptValue.of(player.hasPermission(perm));
    }

    private ScriptValue papi(ScriptContext ctx, ScriptVM vm, ScriptValue... args) {
        if (args.length < 1) return ScriptValue.nil();
        Optional<Player> playerOpt = ctx.invoker().asPlayer().getPlatformPlayer(Bukkit::getPlayer);
        if (playerOpt.isEmpty()) {
            return ScriptValue.nil();
        }
        Player player = playerOpt.get();
        String placeholder = args[0].asString();
        String resolved = BukkitTextProcessor.placeholder(player, placeholder);
        try {
            return ScriptValue.of(Double.parseDouble(resolved));
        } catch (NumberFormatException e) {
            return ScriptValue.of(resolved);
        }
    }

    private ScriptValue level(ScriptContext ctx, ScriptVM vm, ScriptValue... args) {
        Optional<Player> playerOpt = ctx.invoker().asPlayer().getPlatformPlayer(Bukkit::getPlayer);
        if (playerOpt.isEmpty()) {
            return ScriptValue.nil();
        }
        Player player = playerOpt.get();
        return ScriptValue.of(player.getLevel());
    }

    private ScriptValue world(ScriptContext ctx, ScriptVM vm, ScriptValue... args) {
        Optional<Player> playerOpt = ctx.invoker().asPlayer().getPlatformPlayer(Bukkit::getPlayer);
        if (playerOpt.isEmpty()) {
            return ScriptValue.nil();
        }
        Player player = playerOpt.get();
        if (args.length == 0) {
            return ScriptValue.of(player.getWorld().getName());
        }
        String expected = args[0].asString();
        return ScriptValue.of(player.getWorld().getName().equals(expected));
    }

    private ScriptValue gameMode(ScriptContext ctx, ScriptVM vm, ScriptValue... args) {
        Optional<Player> playerOpt = ctx.invoker().asPlayer().getPlatformPlayer(Bukkit::getPlayer);
        if (playerOpt.isEmpty()) {
            return ScriptValue.nil();
        }
        Player player = playerOpt.get();
        if (args.length == 0) {
            return ScriptValue.of(player.getGameMode().name());
        }
        String expected = args[0].asString();
        return ScriptValue.of(player.getGameMode().name().equalsIgnoreCase(expected));
    }

    private ScriptValue biome(ScriptContext ctx, ScriptVM vm, ScriptValue... args) {
        Optional<Player> playerOpt = ctx.invoker().asPlayer().getPlatformPlayer(Bukkit::getPlayer);
        if (playerOpt.isEmpty()) {
            return ScriptValue.nil();
        }
        Player player = playerOpt.get();
        String playerBiome = player.getLocation().getBlock().getBiome().getKey().toString();
        if (args.length < 1) {
            return ScriptValue.of(playerBiome);
        }
        String expected = args[0].asString();
        return ScriptValue.of(playerBiome.equalsIgnoreCase(expected));
    }

    private ScriptValue inWater(ScriptContext ctx, ScriptVM vm, ScriptValue... args) {
        Optional<Player> playerOpt = ctx.invoker().asPlayer().getPlatformPlayer(Bukkit::getPlayer);
        if (playerOpt.isEmpty()) {
            return ScriptValue.nil();
        }
        Player player = playerOpt.get();
        Block block = player.getLocation().getBlock();
        return ScriptValue.of(block.getType() == Material.WATER);
    }

    @SuppressWarnings({"all", "removal"})
    private ScriptValue inRain(ScriptContext ctx, ScriptVM vm, ScriptValue... args) {
        Optional<Player> playerOpt = ctx.invoker().asPlayer().getPlatformPlayer(Bukkit::getPlayer);
        if (playerOpt.isEmpty()) {
            return ScriptValue.nil();
        }
        Player player = playerOpt.get();
        return ScriptValue.of(player.getLocation().getBlock().getBiome().name().contains("RAIN")
            || player.getWorld().hasStorm());
    }

    private ScriptValue lightLevel(ScriptContext ctx, ScriptVM vm, ScriptValue... args) {
        Optional<Player> playerOpt = ctx.invoker().asPlayer().getPlatformPlayer(Bukkit::getPlayer);
        if (playerOpt.isEmpty()) {
            return ScriptValue.nil();
        }

        Player player = playerOpt.get();
        int level = player.getLocation().getBlock().getLightLevel();
        return ScriptValue.of(level);
    }

}
