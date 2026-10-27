package net.flectone.pulse.platform.registry.cloud;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.RootCommandNode;
import lombok.Getter;
import net.flectone.pulse.model.entity.FPlayer;
import org.incendo.cloud.Command;
import org.incendo.cloud.brigadier.CloudBrigadierCommand;
import org.incendo.cloud.brigadier.CloudBrigadierManager;
import org.incendo.cloud.component.CommandComponent;
import org.jspecify.annotations.NonNull;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class BrigadierRegistrationHandler<S> extends PlatformRegistrationHandler<S> {

    private final Set<Command<FPlayer>> registeredCommands = ConcurrentHashMap.newKeySet();
    private final Consumer<Consumer<CommandDispatcher<S>>> commandRegistrar;

    @Getter
    private CloudBrigadierManager<FPlayer, S> brigadierManager;

    public BrigadierRegistrationHandler(Consumer<Consumer<CommandDispatcher<S>>> commandRegistrar) {
        this.commandRegistrar = commandRegistrar;
    }

    @Override
    public void initialize(PlatformCommandManager<S> manager) {
        super.initialize(manager);

        this.brigadierManager = new CloudBrigadierManager<>(manager, manager.senderMapper());

        commandRegistrar.accept(this::registerCommands);
    }

    @Override
    public boolean registerCommand(@NonNull Command<FPlayer> command) {
        return registeredCommands.add(command);
    }

    private void registerCommands(CommandDispatcher<S> dispatcher) {
        RootCommandNode<S> root = dispatcher.getRoot();
        for (Command<FPlayer> command : registeredCommands) {
            CommandComponent<FPlayer> component = command.rootComponent();
            CommandNode<S> node = brigadierManager.literalBrigadierNodeFactory()
                    .createNode(component.name(), command, new CloudBrigadierCommand<>(manager, brigadierManager));

            root.addChild(node);

            for (String alias : component.alternativeAliases()) {
                root.addChild(buildRedirect(alias, node));
            }
        }
    }

    private CommandNode<S> buildRedirect(String alias, CommandNode<S> destination) {
        LiteralArgumentBuilder<S> builder = LiteralArgumentBuilder.<S>literal(alias)
                .requires(destination.getRequirement())
                .forward(destination.getRedirect(), destination.getRedirectModifier(), destination.isFork())
                .executes(destination.getCommand());

        for (CommandNode<S> child : destination.getChildren()) {
            builder.then(child);
        }

        return builder.build();
    }

}
