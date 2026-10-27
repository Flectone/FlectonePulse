package net.flectone.pulse.platform.registry;

import net.flectone.pulse.checker.PermissionChecker;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.platform.handler.CommandExceptionHandler;
import net.flectone.pulse.platform.registry.cloud.PlatformCommandManager;
import net.flectone.pulse.platform.registry.cloud.PlatformRegistrationHandler;
import net.flectone.pulse.scheduler.TaskScheduler;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.SenderMapper;
import org.incendo.cloud.exception.ArgumentParseException;
import org.incendo.cloud.exception.CommandExecutionException;
import org.incendo.cloud.exception.InvalidSyntaxException;
import org.incendo.cloud.exception.NoPermissionException;
import org.incendo.cloud.execution.ExecutionCoordinator;
import org.incendo.cloud.permission.Permission;
import org.incendo.cloud.permission.PermissionResult;
import org.incendo.cloud.setting.ManagerSetting;

import java.util.function.Function;

public abstract class CommandRegistryImpl<S> implements CommandRegistry {

    private final PermissionChecker permissionChecker;
    private final CommandExceptionHandler commandExceptionHandler;
    private final TaskScheduler taskScheduler;
    private final SenderMapper<S, FPlayer> senderMapper;

    protected PlatformCommandManager<S> manager;

    protected CommandRegistryImpl(PermissionChecker permissionChecker,
                                  CommandExceptionHandler commandExceptionHandler,
                                  TaskScheduler taskScheduler,
                                  SenderMapper<S, FPlayer> senderMapper) {
        this.permissionChecker = permissionChecker;
        this.commandExceptionHandler = commandExceptionHandler;
        this.taskScheduler = taskScheduler;
        this.senderMapper = senderMapper;
    }

    protected abstract PlatformRegistrationHandler<S> createRegistrationHandler();

    @Override
    public void init() {
        if (this.manager != null) return;

        this.manager = new PlatformCommandManager<>(
                ExecutionCoordinator.<FPlayer>builder().executor(taskScheduler.getExecutorService()).build(),
                createRegistrationHandler(),
                senderMapper,
                permissionChecker
        );

        manager.settings().set(ManagerSetting.ALLOW_UNSAFE_REGISTRATION, true);
        manager.settings().set(ManagerSetting.OVERRIDE_EXISTING_COMMANDS, true);

        manager.exceptionController().registerHandler(ArgumentParseException.class, commandExceptionHandler::handleArgumentParseException);
        manager.exceptionController().registerHandler(InvalidSyntaxException.class, commandExceptionHandler::handleInvalidSyntaxException);
        manager.exceptionController().registerHandler(NoPermissionException.class, commandExceptionHandler::handleNoPermissionException);
        manager.exceptionController().registerHandler(CommandExecutionException.class, commandExceptionHandler::handleCommandExecutionException);
    }

    @Override
    public void registerCommand(Function<CommandManager<FPlayer>, Command.Builder<FPlayer>> builder) {
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
        // not supported
    }

}
