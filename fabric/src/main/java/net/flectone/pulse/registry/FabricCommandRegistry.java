package net.flectone.pulse.registry;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.handler.CommandExceptionHandler;
import net.flectone.pulse.mapper.FPlayerMapper;
import net.flectone.pulse.model.FPlayer;
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

    private final CommandManager<FPlayer> manager;

    @Inject
    public FabricCommandRegistry(CommandParserRegistry parsers,
                                 CommandExceptionHandler commandExceptionHandler,
                                 FPlayerMapper fPlayerMapper) {
        super(parsers);

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

        // root name
        String commandName = command.rootComponent().name();

        // unregister minecraft and other plugins command
        if (!containsCommand(commandName)) {
            unregisterCommand(commandName);
        }

        // save to cache
        addCommand(commandName);

        // register new command
        manager.command(command);
    }

    @Override
    public void unregisterCommand(String name) {
//        manager.deleteRootCommand(name);
    }
}
