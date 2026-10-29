package net.flectone.pulse.platform.registry.cloud;

import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.resolver.ReflectionResolver;
import net.flectone.pulse.scheduler.TaskScheduler;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginIdentifiableCommand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.incendo.cloud.Command;
import org.incendo.cloud.component.CommandComponent;
import org.incendo.cloud.setting.ManagerSetting;
import org.incendo.cloud.suggestion.Suggestion;
import org.incendo.cloud.suggestion.Suggestions;
import org.incendo.cloud.util.StringUtils;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class BukkitRegistrationHandler extends PlatformRegistrationHandler<CommandSender> {

    private final Map<String, BukkitCommand> registeredCommands = new ConcurrentHashMap<>();
    private final Plugin plugin;
    private final TaskScheduler taskScheduler;
    private final boolean isPaper;
    private final boolean canUpdateCommands;

    private final CommandMap commandMap;
    private final Map<String, org.bukkit.command.Command> knownCommands;

    public BukkitRegistrationHandler(Plugin plugin,
                                     CommandMap commandMap,
                                     Map<String, org.bukkit.command.Command> knownCommands,
                                     TaskScheduler taskScheduler,
                                     ReflectionResolver reflectionResolver) {
        this.plugin = plugin;
        this.commandMap = commandMap;
        this.knownCommands = knownCommands;
        this.taskScheduler = taskScheduler;
        this.isPaper = reflectionResolver.isPaper();
        this.canUpdateCommands = reflectionResolver.hasMethod(Player.class, "updateCommands");
    }

    @Override
    public boolean registerCommand(@NonNull Command<FPlayer> command) {
        CommandComponent<FPlayer> component = command.rootComponent();
        String label = component.name();
        if (registeredCommands.containsKey(label)) return false;

        List<String> aliases = new ArrayList<>(component.alternativeAliases());
        BukkitCommand bukkitCommand = new BukkitCommand(label, aliases, command);
        registeredCommands.put(label, bukkitCommand);

        runSync(() -> {
            if (manager.settings().get(ManagerSetting.OVERRIDE_EXISTING_COMMANDS)) {
                knownCommands.remove(label);
                aliases.forEach(knownCommands::remove);
            }

            commandMap.register(label, plugin.getName().toLowerCase(Locale.ROOT), bukkitCommand);
        });
        return true;
    }

    @Override
    public void unregisterRootCommand(@NonNull CommandComponent<FPlayer> rootCommand) {
        BukkitCommand bukkitCommand = registeredCommands.remove(rootCommand.name());
        if (bukkitCommand == null) return;

        runSync(() -> {
            knownCommands.values().removeIf(command -> command == bukkitCommand);
            bukkitCommand.unregister(commandMap);

            if (canUpdateCommands) {
                Bukkit.getOnlinePlayers().forEach(Player::updateCommands);
            }
        });
    }

    public boolean isRegisteredCommand(String label) {
        return knownCommands.get(label) instanceof BukkitCommand;
    }

    private void runSync(Runnable runnable) {
        if (isPaper) {
            runnable.run();
        } else {
            taskScheduler.runSync(runnable::run);
        }
    }

    private class BukkitCommand extends org.bukkit.command.Command implements PluginIdentifiableCommand {

        private final Command<FPlayer> command;

        private BukkitCommand(String label, List<String> aliases, Command<FPlayer> command) {
            super(label, command.commandDescription().description().textDescription(), "", aliases);

            this.command = command;
        }

        @Override
        public boolean execute(@NonNull CommandSender sender, @NonNull String commandLabel, @NonNull String @NonNull [] args) {
            FPlayer fPlayer = manager.senderMapper().map(sender);

            manager.commandExecutor().executeCommand(fPlayer, getInput(args));
            return true;
        }

        @Override
        public @NonNull List<String> tabComplete(@NonNull CommandSender sender, @NonNull String alias, @NonNull String @NonNull [] args) {
            FPlayer fPlayer = manager.senderMapper().map(sender);

            Suggestions<FPlayer, ? extends Suggestion> suggestions = manager.suggestionFactory().suggestImmediately(fPlayer, getInput(args));
            return suggestions.list().stream()
                    .map(suggestion -> StringUtils.trimBeforeLastSpace(suggestion.suggestion(), suggestions.commandInput()))
                    .filter(Objects::nonNull)
                    .toList();
        }

        @Override
        public boolean testPermissionSilent(@NonNull CommandSender target) {
            if (!isRegistered()) return false;

            FPlayer fPlayer = manager.senderMapper().map(target);
            return manager.testPermission(fPlayer, command.commandPermission()).allowed();
        }

        @Override
        public @NonNull Plugin getPlugin() {
            return plugin;
        }

        private String getInput(String[] args) {
            return args.length == 0 ? getName() : getName() + " " + String.join(" ", args);
        }

    }

}
