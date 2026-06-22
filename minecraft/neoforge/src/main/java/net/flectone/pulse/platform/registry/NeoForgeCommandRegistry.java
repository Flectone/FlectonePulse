package net.flectone.pulse.platform.registry;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.NeoForgeFlectonePulse;
import net.flectone.pulse.execution.scheduler.TaskScheduler;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.platform.handler.CommandExceptionHandler;
import net.flectone.pulse.processing.mapper.NeoForgeFPlayerMapper;
import net.flectone.pulse.util.checker.PermissionChecker;
import org.incendo.cloud.CloudCapability;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.exception.ArgumentParseException;
import org.incendo.cloud.exception.CommandExecutionException;
import org.incendo.cloud.exception.InvalidSyntaxException;
import org.incendo.cloud.exception.NoPermissionException;
import org.incendo.cloud.execution.ExecutionCoordinator;
import org.incendo.cloud.neoforge.NeoForgeServerCommandManager;
import org.incendo.cloud.permission.Permission;
import org.incendo.cloud.permission.PermissionResult;
import org.incendo.cloud.setting.ManagerSetting;

import java.util.function.Function;

@Singleton
public class NeoForgeCommandRegistry implements BrigadierCommandRegistry {

    private final NeoForgeFlectonePulse neoForgeFlectonePulse;
    private final PermissionChecker permissionChecker;
    private final NeoForgeFPlayerMapper fPlayerMapper;
    private final CommandExceptionHandler commandExceptionHandler;
    private final TaskScheduler taskScheduler;

    private CommandManager<FPlayer> manager;

    @Inject
    public NeoForgeCommandRegistry(NeoForgeFlectonePulse neoForgeFlectonePulse,
                                   PermissionChecker permissionChecker,
                                   CommandExceptionHandler commandExceptionHandler,
                                   NeoForgeFPlayerMapper fPlayerMapper,
                                   TaskScheduler taskScheduler) {
        this.neoForgeFlectonePulse = neoForgeFlectonePulse;
        this.permissionChecker = permissionChecker;
        this.fPlayerMapper = fPlayerMapper;
        this.commandExceptionHandler = commandExceptionHandler;
        this.taskScheduler = taskScheduler;
    }

    @Override
    public void init() {
        if (this.manager != null) return;

        this.manager = new NeoForgeServerCommandManager<>(ExecutionCoordinator.<FPlayer>builder().executor(taskScheduler.getExecutorService()).build(), fPlayerMapper);

        manager.settings().set(ManagerSetting.ALLOW_UNSAFE_REGISTRATION, true);
        manager.settings().set(ManagerSetting.OVERRIDE_EXISTING_COMMANDS, true);

        manager.exceptionController().registerHandler(ArgumentParseException.class, commandExceptionHandler::handleArgumentParseException);
        manager.exceptionController().registerHandler(InvalidSyntaxException.class, commandExceptionHandler::handleInvalidSyntaxException);
        manager.exceptionController().registerHandler(NoPermissionException.class, commandExceptionHandler::handleNoPermissionException);
        manager.exceptionController().registerHandler(CommandExecutionException.class, commandExceptionHandler::handleCommandExecutionException);

        setupBrigadierManager(((NeoForgeServerCommandManager<FPlayer>) manager).brigadierManager());
    }

    @Override
    public final void registerCommand(Function<CommandManager<FPlayer>, Command.Builder<FPlayer>> builder) {
        Command.Builder<FPlayer> commandBuilder = builder.apply(manager);

        Permission permission = commandBuilder.commandPermission();
        String permissionString = permission.permissionString();

        Command<FPlayer> command = commandBuilder.permission(fPlayer -> {
            boolean value = permissionChecker.check(fPlayer, permissionString);

            return PermissionResult.of(value, permission);
        }).build();

        String commandName = command.rootComponent().name();

        boolean isCloudCommand = manager.commands().stream()
                .anyMatch(fPlayerCommand -> fPlayerCommand.rootComponent().name().equals(commandName));
        if (isCloudCommand) return;

        unregisterCommand(commandName);
        manager.command(command);
    }

    @Override
    public void unregisterCommand(String name) {
        if (neoForgeFlectonePulse.getMinecraftServer() != null) return;
        if (!manager.hasCapability(CloudCapability.StandardCapabilities.ROOT_COMMAND_DELETION)) return;

        manager.deleteRootCommand(name);
    }
}
