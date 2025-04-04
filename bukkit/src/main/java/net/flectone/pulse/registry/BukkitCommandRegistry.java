package net.flectone.pulse.registry;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.mojang.brigadier.arguments.StringArgumentType;
import io.leangen.geantyref.TypeToken;
import net.flectone.pulse.mapper.FPlayerMapper;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.parser.integer.ColorParser;
import net.flectone.pulse.parser.integer.DurationReasonParser;
import net.flectone.pulse.parser.player.PlayerParser;
import net.flectone.pulse.parser.string.MessageParser;
import org.bukkit.plugin.Plugin;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.brigadier.CloudBrigadierManager;
import org.incendo.cloud.bukkit.CloudBukkitCapabilities;
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
                                 Plugin plugin,
                                 FPlayerMapper fPlayerMapper) {
        super(parsers);

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

        } else if (manager.hasCapability(CloudBukkitCapabilities.ASYNCHRONOUS_COMPLETION)) {
            manager.registerAsynchronousCompletions();
        }

        manager.settings().set(ManagerSetting.ALLOW_UNSAFE_REGISTRATION, true);
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
        manager.deleteRootCommand(name);
    }
}
