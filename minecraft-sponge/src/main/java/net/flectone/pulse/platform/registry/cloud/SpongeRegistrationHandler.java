package net.flectone.pulse.platform.registry.cloud;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.ParsedCommandNode;
import com.mojang.brigadier.context.StringRange;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import lombok.Getter;
import net.flectone.pulse.model.entity.FPlayer;
import net.minecraft.commands.CommandSourceStack;
import org.incendo.cloud.Command;
import org.incendo.cloud.brigadier.CloudBrigadierCommand;
import org.incendo.cloud.brigadier.CloudBrigadierManager;
import org.jspecify.annotations.NonNull;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.manager.CommandFailedRegistrationException;
import org.spongepowered.api.command.manager.CommandManager;
import org.spongepowered.api.command.registrar.CommandRegistrar;
import org.spongepowered.api.event.GenericEvent;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.lifecycle.RegisterCommandEvent;
import org.spongepowered.plugin.PluginContainer;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class SpongeRegistrationHandler extends PlatformRegistrationHandler<CommandSourceStack> {

    private final Set<Command<FPlayer>> registeredCommands = ConcurrentHashMap.newKeySet();
    private final PluginContainer pluginContainer;

    @Getter
    private CloudBrigadierManager<FPlayer, CommandSourceStack> brigadierManager;

    public SpongeRegistrationHandler(PluginContainer pluginContainer) {
        this.pluginContainer = pluginContainer;
    }

    @Override
    public void initialize(PlatformCommandManager<CommandSourceStack> manager) {
        super.initialize(manager);

        this.brigadierManager = new CloudBrigadierManager<>(manager, manager.senderMapper());

        Sponge.eventManager().registerListeners(pluginContainer, this, MethodHandles.lookup());
    }

    @Override
    public boolean registerCommand(@NonNull Command<FPlayer> command) {
        if (!registeredCommands.add(command)) return false;

        getRegistrar().ifPresent(registrar -> {
            try {
                registrar.register(pluginContainer, createBuilder(command), command.rootComponent().name(), getAliases(command));
            } catch (CommandFailedRegistrationException _) {
                return;
            }

            CommandManager commandManager = Sponge.server().commandManager();
            Sponge.server().onlinePlayers().forEach(commandManager::updateCommandTreeForPlayer);
        });

        return true;
    }

    @Listener
    @SuppressWarnings("unchecked")
    public void onRegisterCommandEvent(RegisterCommandEvent<?> event) {
        if (!isBrigadierEvent(event)) return;

        RegisterCommandEvent<LiteralArgumentBuilder<CommandSourceStack>> brigadierEvent = (RegisterCommandEvent<LiteralArgumentBuilder<CommandSourceStack>>) event;

        for (Command<FPlayer> command : registeredCommands) {
            try {
                brigadierEvent.register(pluginContainer, createBuilder(command), command.rootComponent().name(), getAliases(command));
            } catch (CommandFailedRegistrationException _) {
                // ignore
            }
        }
    }

    @SuppressWarnings("unchecked")
    private LiteralArgumentBuilder<CommandSourceStack> createBuilder(Command<FPlayer> command) {
        LiteralCommandNode<CommandSourceStack> node = brigadierManager.literalBrigadierNodeFactory()
                .createNode(command.rootComponent().name(), command, new CloudBrigadierCommand<>(manager, brigadierManager, this::stripNamespace));

        return (LiteralArgumentBuilder<CommandSourceStack>) wrapBuilder(node);
    }

    @SuppressWarnings("unchecked")
    private ArgumentBuilder<CommandSourceStack, ?> wrapBuilder(CommandNode<CommandSourceStack> node) {
        ArgumentBuilder<CommandSourceStack, ?> builder = node.createBuilder();

        com.mojang.brigadier.Command<CommandSourceStack> command = node.getCommand();
        if (command != null) {
            builder.executes(context -> command.run(toVanilla(context)));
        }

        if (node instanceof ArgumentCommandNode<CommandSourceStack, ?> argumentNode && argumentNode.getCustomSuggestions() != null) {
            SuggestionProvider<CommandSourceStack> suggestionProvider = argumentNode.getCustomSuggestions();
            ((RequiredArgumentBuilder<CommandSourceStack, ?>) builder).suggests((context, suggestionsBuilder) ->
                    suggestionProvider.getSuggestions(toVanilla(context), suggestionsBuilder)
            );
        }

        node.getChildren().forEach(child -> builder.then(wrapBuilder(child)));

        return builder;
    }

    private CommandContext<CommandSourceStack> toVanilla(CommandContext<CommandSourceStack> context) {
        List<ParsedCommandNode<CommandSourceStack>> nodes = new ArrayList<>();
        for (CommandContext<CommandSourceStack> child = context; child != null; child = child.getChild()) {
            nodes.addAll(child.getNodes());
        }

        if (context.getRootNode() instanceof LiteralCommandNode<CommandSourceStack> rootNode) {
            int end = nodes.isEmpty() ? context.getInput().length() : nodes.getFirst().getRange().getStart() - 1;
            int start = context.getInput().lastIndexOf(' ', end - 1) + 1;

            nodes.addFirst(new ParsedCommandNode<>(rootNode, StringRange.between(start, end)));
        }

        CommandContext<CommandSourceStack> lastChild = context.getLastChild();

        return new CommandContext<>(
                lastChild.getSource(),
                context.getInput(),
                Map.of(),
                lastChild.getCommand(),
                context.getRootNode(),
                nodes,
                StringRange.between(context.getRange().getStart(), lastChild.getRange().getEnd()),
                null,
                null,
                false
        );
    }

    private String[] getAliases(Command<FPlayer> command) {
        return command.rootComponent().alternativeAliases().toArray(String[]::new);
    }

    @SuppressWarnings("unchecked")
    private Optional<CommandRegistrar<LiteralArgumentBuilder<CommandSourceStack>>> getRegistrar() {
        if (!Sponge.isServerAvailable()) return Optional.empty();

        return Sponge.server().commandManager().registrar(LiteralArgumentBuilder.class)
                .map(registrar -> (CommandRegistrar<LiteralArgumentBuilder<CommandSourceStack>>) (CommandRegistrar<?>) registrar);
    }

    private boolean isBrigadierEvent(RegisterCommandEvent<?> event) {
        try {
            Object paramType = GenericEvent.class.getMethod("paramType").invoke(event);
            Type type = (Type) paramType.getClass().getMethod("getType").invoke(paramType);

            return type instanceof ParameterizedType parameterizedType && parameterizedType.getRawType() == LiteralArgumentBuilder.class;
        } catch (ReflectiveOperationException _) {
            return false;
        }
    }

    private String stripNamespace(String input) {
        String namespace = pluginContainer.metadata().id().toLowerCase(Locale.ROOT) + ":";
        return input.startsWith(namespace) ? input.substring(namespace.length()) : input;
    }

}
