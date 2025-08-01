package net.flectone.pulse.registry;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.annotation.Sync;
import net.flectone.pulse.configuration.Config;
import net.flectone.pulse.handler.CommandExceptionHandler;
import net.flectone.pulse.mapper.FPlayerMapper;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.resolver.FileResolver;
import org.bukkit.plugin.Plugin;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.exception.ArgumentParseException;
import org.incendo.cloud.exception.CommandExecutionException;
import org.incendo.cloud.exception.InvalidSyntaxException;
import org.incendo.cloud.exception.NoPermissionException;
import org.incendo.cloud.execution.ExecutionCoordinator;
import org.incendo.cloud.paper.LegacyPaperCommandManager;
import org.incendo.cloud.setting.ManagerSetting;

import java.util.function.Function;

@Singleton
public class LegacyBukkitCommandRegistry extends CommandRegistry {

    private final Config config;
    private final Plugin plugin;
    protected final LegacyPaperCommandManager<FPlayer> manager;

    @Inject
    public LegacyBukkitCommandRegistry(FileResolver fileResolver,
                                       CommandExceptionHandler commandExceptionHandler,
                                       Plugin plugin,
                                       FPlayerMapper fPlayerMapper) {
        this.config = fileResolver.getConfig();
        this.plugin = plugin;
        this.manager = new LegacyPaperCommandManager<>(plugin, ExecutionCoordinator.asyncCoordinator(), fPlayerMapper);

        manager.settings().set(ManagerSetting.ALLOW_UNSAFE_REGISTRATION, true);

        manager.exceptionController().registerHandler(ArgumentParseException.class, commandExceptionHandler::handleArgumentParseException);
        manager.exceptionController().registerHandler(InvalidSyntaxException.class, commandExceptionHandler::handleInvalidSyntaxException);
        manager.exceptionController().registerHandler(NoPermissionException.class, commandExceptionHandler::handleNoPermissionException);
        manager.exceptionController().registerHandler(CommandExecutionException.class, commandExceptionHandler::handleCommandExecutionException);
    }

    @Override
    public void registerCommand(Function<CommandManager<FPlayer>, Command.Builder<FPlayer>> builder) {
        Command<FPlayer> command = builder.apply(manager).build();

        // root name
        String commandName = command.rootComponent().name();

        boolean isCloudCommand = manager.commands().stream()
                .anyMatch(fPlayerCommand -> fPlayerCommand.rootComponent().name().equals(commandName));

        boolean needUnregister = plugin.getServer().getPluginCommand(commandName) != null
                || config.isUnregisterOwnCommands() && isCloudCommand;

        if (needUnregister) {
            unregisterCommand(commandName);
        } else if (isCloudCommand) {
            return;
        }

        // register new command
        manager.command(command);
    }

    @Override
    public void unregisterCommand(String name) {
        manager.deleteRootCommand(name);
    }

    @Override
    public void reload() {
        if (!config.isUnregisterOwnCommands()) return;

        syncRemoveCommands();
    }

    public void removeCommands() {
        manager.commands().stream()
                .map(command -> command.rootComponent().name())
                .toList() // fix concurrent modification
                .forEach(this::unregisterCommand);
    }

    @Sync
    public void syncRemoveCommands() {
        removeCommands();
    }

}
