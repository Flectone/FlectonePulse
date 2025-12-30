package net.flectone.pulse.platform.registry;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.FabricFlectonePulse;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.platform.handler.CommandExceptionHandler;
import net.flectone.pulse.processing.mapper.FPlayerMapper;
import net.flectone.pulse.util.checker.PermissionChecker;
import org.incendo.cloud.CloudCapability;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.exception.ArgumentParseException;
import org.incendo.cloud.exception.CommandExecutionException;
import org.incendo.cloud.exception.InvalidSyntaxException;
import org.incendo.cloud.exception.NoPermissionException;
import org.incendo.cloud.execution.ExecutionCoordinator;
import org.incendo.cloud.fabric.FabricServerCommandManager;
import org.incendo.cloud.permission.Permission;
import org.incendo.cloud.permission.PermissionResult;
import org.incendo.cloud.setting.ManagerSetting;

import java.util.function.Function;

@Singleton
public class FabricCommandRegistry extends CommandRegistry {

    private final FabricFlectonePulse fabricFlectonePulse;
    private final PermissionChecker permissionChecker;
    private final CommandManager<FPlayer> manager;

    @Inject
    public FabricCommandRegistry(FabricFlectonePulse fabricFlectonePulse,
                                 PermissionChecker permissionChecker,
                                 CommandExceptionHandler commandExceptionHandler,
                                 FPlayerMapper fPlayerMapper) {
        this.fabricFlectonePulse = fabricFlectonePulse;
        this.permissionChecker = permissionChecker;

        this.manager = new FabricServerCommandManager<>(ExecutionCoordinator.asyncCoordinator(), fPlayerMapper);

        manager.settings().set(ManagerSetting.ALLOW_UNSAFE_REGISTRATION, true);
        manager.settings().set(ManagerSetting.OVERRIDE_EXISTING_COMMANDS, true);

        manager.exceptionController().registerHandler(ArgumentParseException.class, commandExceptionHandler::handleArgumentParseException);
        manager.exceptionController().registerHandler(InvalidSyntaxException.class, commandExceptionHandler::handleInvalidSyntaxException);
        manager.exceptionController().registerHandler(NoPermissionException.class, commandExceptionHandler::handleNoPermissionException);
        manager.exceptionController().registerHandler(CommandExecutionException.class, commandExceptionHandler::handleCommandExecutionException);

        setupBrigadierManager(((FabricServerCommandManager<FPlayer>) manager).brigadierManager());
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
        if (fabricFlectonePulse.getMinecraftServer() != null) return;
        if (!manager.hasCapability(CloudCapability.StandardCapabilities.ROOT_COMMAND_DELETION)) return;

        manager.deleteRootCommand(name);
    }

    @Override
    public void reload() {
    }
}
