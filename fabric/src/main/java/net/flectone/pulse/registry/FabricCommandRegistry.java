package net.flectone.pulse.registry;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.FabricFlectonePulse;
import net.flectone.pulse.checker.PermissionChecker;
import net.flectone.pulse.handler.CommandExceptionHandler;
import net.flectone.pulse.mapper.FPlayerMapper;
import net.flectone.pulse.model.FPlayer;
import org.incendo.cloud.CloudCapability;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.exception.ArgumentParseException;
import org.incendo.cloud.exception.CommandExecutionException;
import org.incendo.cloud.exception.InvalidSyntaxException;
import org.incendo.cloud.exception.NoPermissionException;
import org.incendo.cloud.execution.ExecutionCoordinator;
import org.incendo.cloud.fabric.FabricServerCommandManager;
import org.incendo.cloud.setting.ManagerSetting;

import java.util.function.Function;

@Singleton
public class FabricCommandRegistry extends CommandRegistry {

    private final FabricFlectonePulse fabricFlectonePulse;
    private final CommandManager<FPlayer> manager;

    @Inject
    public FabricCommandRegistry(FabricFlectonePulse fabricFlectonePulse,
                                 CommandParserRegistry parsers,
                                 PermissionChecker permissionChecker,
                                 CommandExceptionHandler commandExceptionHandler,
                                 FPlayerMapper fPlayerMapper) {
        super(parsers, permissionChecker);

        this.fabricFlectonePulse = fabricFlectonePulse;
        this.manager = new FabricServerCommandManager<>(ExecutionCoordinator.asyncCoordinator(), fPlayerMapper);

        manager.settings().set(ManagerSetting.ALLOW_UNSAFE_REGISTRATION, true);
        manager.settings().set(ManagerSetting.OVERRIDE_EXISTING_COMMANDS, true);

        manager.exceptionController().registerHandler(ArgumentParseException.class, commandExceptionHandler::handleArgumentParseException);
        manager.exceptionController().registerHandler(InvalidSyntaxException.class, commandExceptionHandler::handleInvalidSyntaxException);
        manager.exceptionController().registerHandler(NoPermissionException.class, commandExceptionHandler::handleNoPermissionException);
        manager.exceptionController().registerHandler(CommandExecutionException.class, commandExceptionHandler::handleCommandExecutionException);
    }

    @Override
    public final void registerCommand(Function<CommandManager<FPlayer>, Command.Builder<FPlayer>> builder) {
        Command<FPlayer> command = builder.apply(manager).build();

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
