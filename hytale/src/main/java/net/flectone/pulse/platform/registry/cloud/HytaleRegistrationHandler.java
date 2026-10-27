package net.flectone.pulse.platform.registry.cloud;

import com.hypixel.hytale.server.core.command.system.AbstractCommand;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.CommandSender;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import net.flectone.pulse.model.entity.FPlayer;
import org.incendo.cloud.Command;
import org.jspecify.annotations.NonNull;

import java.util.concurrent.CompletableFuture;

public class HytaleRegistrationHandler extends PlatformRegistrationHandler<CommandSender> {

    private final JavaPlugin javaPlugin;

    public HytaleRegistrationHandler(JavaPlugin javaPlugin) {
        this.javaPlugin = javaPlugin;
    }

    @Override
    public boolean registerCommand(@NonNull Command<FPlayer> command) {
        javaPlugin.getCommandRegistry().registerCommand(new HytaleCommand(command, manager));
        return true;
    }

    private static class HytaleCommand extends AbstractCommand {

        private final PlatformCommandManager<CommandSender> manager;

        private HytaleCommand(Command<FPlayer> command, PlatformCommandManager<CommandSender> manager) {
            super(command.rootComponent().name(), command.commandDescription().description().textDescription());

            this.manager = manager;

            String commandName = command.rootComponent().name();
            addAliases(command.rootComponent().alternativeAliases().stream()
                    .filter(alias -> !alias.equalsIgnoreCase(commandName))
                    .toArray(String[]::new)
            );

            setAllowsExtraArguments(true);
            requireNoPermission();
        }

        @Override
        protected CompletableFuture<Void> execute(@NonNull CommandContext commandContext) {
            FPlayer fPlayer = manager.senderMapper().map(commandContext.sender());

            manager.commandExecutor().executeCommand(fPlayer, commandContext.getInputString());

            return CompletableFuture.completedFuture(null);
        }

    }

}
