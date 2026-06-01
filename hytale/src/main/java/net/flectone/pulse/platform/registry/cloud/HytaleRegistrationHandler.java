package net.flectone.pulse.platform.registry.cloud;

import com.hypixel.hytale.server.core.command.system.AbstractCommand;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import net.flectone.pulse.model.entity.FPlayer;
import org.incendo.cloud.Command;
import org.incendo.cloud.internal.CommandRegistrationHandler;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class HytaleRegistrationHandler implements CommandRegistrationHandler<FPlayer> {

    private HytaleCommandManager manager;
    private JavaPlugin javaPlugin;

    public void initialize(HytaleCommandManager hytaleCommandManager, JavaPlugin javaPlugin) {
        this.manager = hytaleCommandManager;
        this.javaPlugin = javaPlugin;
    }

    @Override
    public boolean registerCommand(@NonNull Command<FPlayer> command) {
        AbstractCommand abstractCommand = new HytaleCommand(command, manager);

        javaPlugin.getCommandRegistry().registerCommand(abstractCommand);
        return true;
    }

    public static class HytaleCommand extends AbstractCommand {

        private final HytaleCommandManager manager;

        protected HytaleCommand(Command<FPlayer> command, HytaleCommandManager manager) {
            super(command.rootComponent().name(), command.commandDescription().description().textDescription());

            this.manager = manager;

            String commandName = command.rootComponent().name();
            addAliases(command.rootComponent().alternativeAliases().stream()
                    .filter(string -> !string.equalsIgnoreCase(commandName))
                    .toArray(String[]::new)
            );

            setAllowsExtraArguments(true);
        }

        @Override
        protected @Nullable CompletableFuture<Void> execute(@NonNull CommandContext commandContext) {
            FPlayer fPlayer = manager.getSenderMapper().map(commandContext.sender());

            manager.commandExecutor().executeCommand(fPlayer, commandContext.getInputString());

            return null;
        }

        @Override
        protected boolean canGeneratePermission() {
            return false;
        }

    }

}
