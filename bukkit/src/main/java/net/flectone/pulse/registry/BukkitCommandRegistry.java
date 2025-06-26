package net.flectone.pulse.registry;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.mojang.brigadier.arguments.StringArgumentType;
import io.leangen.geantyref.TypeToken;
import net.flectone.pulse.adapter.BukkitServerAdapter;
import net.flectone.pulse.annotation.Sync;
import net.flectone.pulse.checker.PermissionChecker;
import net.flectone.pulse.handler.CommandExceptionHandler;
import net.flectone.pulse.mapper.FPlayerMapper;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.parser.integer.ColorParser;
import net.flectone.pulse.parser.integer.DurationReasonParser;
import net.flectone.pulse.parser.player.PlayerParser;
import net.flectone.pulse.parser.string.MessageParser;
import org.bukkit.plugin.Plugin;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.brigadier.BrigadierSetting;
import org.incendo.cloud.brigadier.CloudBrigadierManager;
import org.incendo.cloud.bukkit.CloudBukkitCapabilities;
import org.incendo.cloud.exception.ArgumentParseException;
import org.incendo.cloud.exception.CommandExecutionException;
import org.incendo.cloud.exception.InvalidSyntaxException;
import org.incendo.cloud.exception.NoPermissionException;
import org.incendo.cloud.execution.ExecutionCoordinator;
import org.incendo.cloud.paper.LegacyPaperCommandManager;
import org.incendo.cloud.parser.standard.StringParser;
import org.incendo.cloud.setting.ManagerSetting;

import java.util.function.Function;

@Singleton
public class BukkitCommandRegistry extends CommandRegistry {

    private final LegacyPaperCommandManager<FPlayer> manager;

    @Inject
    public BukkitCommandRegistry(CommandParserRegistry parsers,
                                 CommandExceptionHandler commandExceptionHandler,
                                 PermissionChecker permissionChecker,
                                 Plugin plugin,
                                 FPlayerMapper fPlayerMapper) {
        super(parsers, permissionChecker);

        this.manager = new LegacyPaperCommandManager<>(plugin, ExecutionCoordinator.asyncCoordinator(), fPlayerMapper);

        if (manager.hasCapability(CloudBukkitCapabilities.NATIVE_BRIGADIER)) {
            manager.registerBrigadier();
            CloudBrigadierManager<FPlayer, ?> brigadierManager = manager.brigadierManager();
            brigadierManager.setNativeSuggestions(new TypeToken<StringParser<FPlayer>>() {}, true);

            brigadierManager.registerMapping(new TypeToken<PlayerParser>() {},
                    builder -> builder.cloudSuggestions()
                            .to(argument -> StringArgumentType.string())
            );

            brigadierManager.registerMapping(new TypeToken<DurationReasonParser>() {},
                    builder -> builder.cloudSuggestions()
                            .to(argument -> StringArgumentType.greedyString())
            );

            brigadierManager.registerMapping(new TypeToken<ColorParser>() {},
                    builder -> builder.cloudSuggestions()
                            .to(argument -> StringArgumentType.greedyString())
            );

            brigadierManager.registerMapping(new TypeToken<MessageParser>() {},
                    builder -> builder.cloudSuggestions()
                            .to(argument -> StringArgumentType.greedyString())
            );

            brigadierManager.settings().set(BrigadierSetting.FORCE_EXECUTABLE, true);

        } else if (manager.hasCapability(CloudBukkitCapabilities.ASYNCHRONOUS_COMPLETION)) {
            manager.registerAsynchronousCompletions();
        }

        manager.settings().set(ManagerSetting.ALLOW_UNSAFE_REGISTRATION, true);

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

        // spigot issue
        if (!BukkitServerAdapter.IS_PAPER && containsCommand(commandName)) return;

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
        manager.deleteRootCommand(name);
    }

    @Override
    public void reload() {
        if (BukkitServerAdapter.IS_PAPER) {
            super.reload();
        } else {
            // only for spigot
            syncReload();
        }
    }

    @Sync
    public void syncReload() {
        super.reload();
    }
}
