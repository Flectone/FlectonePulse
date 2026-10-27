package net.flectone.pulse.platform.registry;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.RootCommandNode;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.platform.adapter.FabricVersionAdapter;
import net.minecraft.commands.CommandSourceStack;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.SenderMapper;
import org.incendo.cloud.SenderMapperHolder;
import org.incendo.cloud.brigadier.BrigadierManagerHolder;
import org.incendo.cloud.brigadier.CloudBrigadierCommand;
import org.incendo.cloud.brigadier.CloudBrigadierManager;
import org.incendo.cloud.component.CommandComponent;
import org.incendo.cloud.execution.ExecutionCoordinator;
import org.incendo.cloud.internal.CommandRegistrationHandler;
import org.jspecify.annotations.NonNull;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class FabricBrigadierCommandManager extends CommandManager<FPlayer> implements
        BrigadierManagerHolder<FPlayer, CommandSourceStack>, SenderMapperHolder<CommandSourceStack, FPlayer> {

    private final SenderMapper<CommandSourceStack, FPlayer> senderMapper;
    private final CloudBrigadierManager<FPlayer, CommandSourceStack> brigadierManager;

    public FabricBrigadierCommandManager(ExecutionCoordinator<FPlayer> executionCoordinator,
                                         SenderMapper<CommandSourceStack, FPlayer> senderMapper,
                                         FabricVersionAdapter fabricVersionAdapter) {
        super(executionCoordinator, new RegistrationHandler());

        this.senderMapper = senderMapper;
        this.brigadierManager = new CloudBrigadierManager<>(this, senderMapper);

        ((RegistrationHandler) commandRegistrationHandler()).initialize(this, fabricVersionAdapter);
    }

    @Override
    public boolean hasPermission(@NonNull FPlayer sender, @NonNull String permission) {
        return true;
    }

    @Override
    public boolean hasBrigadierManager() {
        return true;
    }

    @Override
    public @NonNull CloudBrigadierManager<FPlayer, CommandSourceStack> brigadierManager() {
        return brigadierManager;
    }

    @Override
    public @NonNull SenderMapper<CommandSourceStack, FPlayer> senderMapper() {
        return senderMapper;
    }

    private static class RegistrationHandler implements CommandRegistrationHandler<FPlayer> {

        private final Set<Command<FPlayer>> registeredCommands = ConcurrentHashMap.newKeySet();

        private FabricBrigadierCommandManager commandManager;

        private void initialize(FabricBrigadierCommandManager commandManager, FabricVersionAdapter fabricVersionAdapter) {
            this.commandManager = commandManager;

            fabricVersionAdapter.registerCommands(this::registerCommands);
        }

        @Override
        public boolean registerCommand(@NonNull Command<FPlayer> command) {
            return registeredCommands.add(command);
        }

        private void registerCommands(CommandDispatcher<CommandSourceStack> dispatcher) {
            commandManager.lockRegistration();

            RootCommandNode<CommandSourceStack> root = dispatcher.getRoot();
            for (Command<FPlayer> command : registeredCommands) {
                CommandComponent<FPlayer> component = command.rootComponent();
                CommandNode<CommandSourceStack> node = commandManager.brigadierManager()
                        .literalBrigadierNodeFactory()
                        .createNode(component.name(), command, new CloudBrigadierCommand<>(commandManager, commandManager.brigadierManager()));

                root.addChild(node);

                for (String alias : component.alternativeAliases()) {
                    root.addChild(buildRedirect(alias, node));
                }
            }
        }

        private CommandNode<CommandSourceStack> buildRedirect(String alias, CommandNode<CommandSourceStack> destination) {
            LiteralArgumentBuilder<CommandSourceStack> builder = LiteralArgumentBuilder.<CommandSourceStack>literal(alias)
                    .requires(destination.getRequirement())
                    .forward(destination.getRedirect(), destination.getRedirectModifier(), destination.isFork())
                    .executes(destination.getCommand());

            for (CommandNode<CommandSourceStack> child : destination.getChildren()) {
                builder.then(child);
            }

            return builder.build();
        }

    }

}
