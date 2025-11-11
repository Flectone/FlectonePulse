package net.flectone.pulse.platform.registry;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.annotation.Sync;
import net.flectone.pulse.config.Config;
import net.flectone.pulse.platform.handler.CommandExceptionHandler;
import net.flectone.pulse.processing.mapper.FPlayerMapper;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.processing.resolver.FileResolver;
import net.flectone.pulse.processing.resolver.ReflectionResolver;
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
    private final ReflectionResolver reflectionResolver;
    protected final LegacyPaperCommandManager<FPlayer> manager;

    @Inject
    public LegacyBukkitCommandRegistry(FileResolver fileResolver,
                                       CommandExceptionHandler commandExceptionHandler,
                                       Plugin plugin,
                                       ReflectionResolver reflectionResolver,
                                       FPlayerMapper fPlayerMapper) {
        this.config = fileResolver.getConfig();
        this.plugin = plugin;
        this.reflectionResolver = reflectionResolver;
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
                || config.getCommand().isUnregisterOnReload() && isCloudCommand;

        if (needUnregister) {
            unregisterCommand(commandName);
        } else if (isCloudCommand) {
            return;
        }

        // register new command
        if (reflectionResolver.isPaper()) {
            registerCommand(command);
        } else {
            syncRegisterCommand(command);
        }
    }

    @Override
    public void unregisterCommand(String name) {
        if (reflectionResolver.isPaper()) {
            deleteRootCommand(name);
        } else {
            syncDeleteRootCommand(name);
        }
    }

    @Override
    public void reload() {
        if (!config.getCommand().isUnregisterOnReload()) return;

        if (reflectionResolver.isPaper()) {
            unregisterCommands();
        } else {
            syncUnregisterCommands();
        }
    }

    public void deleteRootCommand(String name) {
        manager.deleteRootCommand(name);
    }

    @Sync
    public void syncDeleteRootCommand(String name) {
        deleteRootCommand(name);
    }

    public void registerCommand(Command<FPlayer> command) {
        manager.command(command);
    }

    @Sync
    public void syncRegisterCommand(Command<FPlayer> command) {
        registerCommand(command);
    }

    public void unregisterCommands() {
        manager.commands().stream()
                .map(command -> command.rootComponent().name())
                .toList() // fix concurrent modification
                .forEach(this::unregisterCommand);
    }

    @Sync
    public void syncUnregisterCommands() {
        unregisterCommands();
    }

}
