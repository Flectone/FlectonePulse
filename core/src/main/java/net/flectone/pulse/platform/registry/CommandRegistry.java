package net.flectone.pulse.platform.registry;

import net.flectone.pulse.model.entity.FPlayer;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;

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
     * Initializes the command manager and registers exception handlers.
     */
    void init();

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

}
