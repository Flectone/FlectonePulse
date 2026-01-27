package net.flectone.pulse.platform.registry;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.platform.handler.CommandExceptionHandler;
import net.flectone.pulse.platform.registry.cloud.HytaleCommandManager;
import net.flectone.pulse.platform.registry.cloud.HytaleRegistrationHandler;
import net.flectone.pulse.processing.mapper.FPlayerMapper;
import net.flectone.pulse.util.checker.PermissionChecker;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.exception.ArgumentParseException;
import org.incendo.cloud.exception.CommandExecutionException;
import org.incendo.cloud.exception.InvalidSyntaxException;
import org.incendo.cloud.exception.NoPermissionException;
import org.incendo.cloud.execution.ExecutionCoordinator;
import org.incendo.cloud.permission.Permission;
import org.incendo.cloud.permission.PermissionResult;
import org.incendo.cloud.setting.ManagerSetting;

import java.util.function.Function;

@Singleton
public class HytaleCommandRegistry implements CommandRegistry {

    private final PermissionChecker permissionChecker;
    private final CommandManager<FPlayer> manager;

    @Inject
    public HytaleCommandRegistry(PermissionChecker permissionChecker,
                                 FPlayerMapper fPlayerMapper,
                                 CommandExceptionHandler commandExceptionHandler,
                                 JavaPlugin javaPlugin) {
        this.permissionChecker = permissionChecker;

        this.manager = new HytaleCommandManager(ExecutionCoordinator.asyncCoordinator(), new HytaleRegistrationHandler(), fPlayerMapper, javaPlugin);

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

    @Override
    public void reload() {
        // not supported
    }

}
