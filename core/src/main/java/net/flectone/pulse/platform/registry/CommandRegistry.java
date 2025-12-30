package net.flectone.pulse.platform.registry;

import com.mojang.brigadier.arguments.StringArgumentType;
import io.leangen.geantyref.TypeToken;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.processing.parser.integer.ColorParser;
import net.flectone.pulse.processing.parser.integer.DurationReasonParser;
import net.flectone.pulse.processing.parser.player.PlayerParser;
import net.flectone.pulse.processing.parser.string.MessageParser;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.brigadier.BrigadierSetting;
import org.incendo.cloud.brigadier.CloudBrigadierManager;
import org.incendo.cloud.parser.standard.StringParser;

import java.util.function.Function;

/**
 * Registry for managing command registration and Brigadier integration.
 * Provides methods to register, unregister, and configure commands with Brigadier support.
 *
 * <p><b>Command registration example:</b>
 * <pre>{@code
 * CommandRegistry registry = flectonePulse.get(CommandRegistry.class);
 *
 * registry.registerCommand(manager ->
 *     manager.commandBuilder("mycommand")
 *         .permission("myplugin.command")
 *         .handler(context -> {
 *             FPlayer player = context.sender();
 *             player.sendMessage("Command executed!");
 *         })
 * );
 * }</pre>
 *
 * @author TheFaser
 * @since 0.8.0
 */
public interface CommandRegistry extends Registry {

    /**
     * Registers a new command using the provided builder function.
     *
     * @param builder function that creates a command builder using the CommandManager
     */
    void registerCommand(Function<CommandManager<FPlayer> , Command.Builder<FPlayer>> builder);

    /**
     * Unregisters a command by its name.
     *
     * @param name the name of the command to unregister
     */
    void unregisterCommand(String name);

    /**
     * Configures Brigadier mappings for custom argument parsers.
     * This method sets up mappings between FlectonePulse parsers and Brigadier argument types.
     *
     * @param brigadierManager the CloudBrigadierManager to configure
     */
    default void setupBrigadierManager(CloudBrigadierManager<FPlayer, ?> brigadierManager) {
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
    }

}
