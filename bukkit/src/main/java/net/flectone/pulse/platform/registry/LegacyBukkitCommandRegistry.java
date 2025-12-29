package net.flectone.pulse.platform.registry;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.config.Config;
import net.flectone.pulse.execution.scheduler.TaskScheduler;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.platform.handler.CommandExceptionHandler;
import net.flectone.pulse.processing.mapper.FPlayerMapper;
import net.flectone.pulse.processing.resolver.ReflectionResolver;
import net.flectone.pulse.util.file.FileFacade;
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
    private final TaskScheduler taskScheduler;
    protected final LegacyPaperCommandManager<FPlayer> manager;

    @Inject
    public LegacyBukkitCommandRegistry(FileFacade fileFacade,
                                       CommandExceptionHandler commandExceptionHandler,
                                       Plugin plugin,
                                       ReflectionResolver reflectionResolver,
                                       TaskScheduler taskScheduler,
                                       FPlayerMapper fPlayerMapper) {
        this.config = fileFacade.config();
        this.plugin = plugin;
        this.taskScheduler = taskScheduler;
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
                || config.command().unregisterOnReload() && isCloudCommand;

        if (needUnregister) {
            unregisterCommand(commandName);
        } else if (isCloudCommand) {
            return;
        }

        // register new command
        if (reflectionResolver.isPaper()) {
            registerCommand(command);
        } else {
            taskScheduler.runSync(() -> registerCommand(command));
        }
    }

    @Override
    public void unregisterCommand(String name) {
        if (reflectionResolver.isPaper()) {
            deleteRootCommand(name);
        } else {
            taskScheduler.runSync(() -> deleteRootCommand(name));
        }
    }

    @Override
    public void reload() {
        if (!config.command().unregisterOnReload()) return;

        if (reflectionResolver.isPaper()) {
            unregisterCommands();
        } else {
            taskScheduler.runSync(this::unregisterCommands);
        }
    }

    public void deleteRootCommand(String name) {
        manager.deleteRootCommand(name);
    }

    public void registerCommand(Command<FPlayer> command) {
        manager.command(command);
    }

    public void unregisterCommands() {
        manager.commands().stream()
                .map(command -> command.rootComponent().name())
                .toList() // fix concurrent modification
                .forEach(this::unregisterCommand);
    }

}
