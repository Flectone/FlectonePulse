package net.flectone.pulse.platform.registry.cloud;

import com.mojang.brigadier.tree.LiteralCommandNode;
import com.mojang.brigadier.tree.RootCommandNode;
import io.papermc.paper.command.brigadier.CommandRegistrationFlag;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import net.flectone.pulse.model.entity.FPlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.incendo.cloud.Command;
import org.incendo.cloud.brigadier.CloudBrigadierCommand;
import org.incendo.cloud.brigadier.CloudBrigadierManager;
import org.incendo.cloud.component.CommandComponent;
import org.incendo.cloud.internal.CommandNode;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings("UnstableApiUsage")
public class PaperRegistrationHandler extends PlatformRegistrationHandler<CommandSender> {

    private final Map<String, Set<String>> registeredLabels = new ConcurrentHashMap<>();
    private final Plugin plugin;

    private CloudBrigadierManager<FPlayer, CommandSourceStack> brigadierManager;

    private volatile Commands commands;

    private Field invalidField;
    private Method removeCommandMethod;

    public PaperRegistrationHandler(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void initialize(PlatformCommandManager<CommandSender> manager) {
        super.initialize(manager);

        this.brigadierManager = new CloudBrigadierManager<>(manager, new PaperBrigadierSenderMapper<>(CommandSourceStack::getSender, manager.senderMapper()));

        plugin.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, event -> {
            this.commands = event.registrar();

            registeredLabels.clear();
            manager.commandTree().rootNodes().forEach(rootNode -> register(commands, rootNode));
        });
    }

    public CloudBrigadierManager<FPlayer, ?> getBrigadierManager() {
        return brigadierManager;
    }

    @Override
    public boolean registerCommand(@NonNull Command<FPlayer> command) {
        String label = command.rootComponent().name();
        if (registeredLabels.containsKey(label)) return false;

        Commands commands = this.commands;
        if (commands == null) return true;

        CommandNode<FPlayer> rootNode = manager.commandTree().getNamedNode(label);
        if (rootNode == null) return false;

        runUnsafe(commands, () -> register(commands, rootNode));
        updateCommands();
        return true;
    }

    @Override
    public void unregisterRootCommand(@NonNull CommandComponent<FPlayer> rootCommand) {
        Commands commands = this.commands;
        if (commands == null) return;

        Set<String> labels = registeredLabels.remove(rootCommand.name());
        if (labels == null) return;

        runUnsafe(commands, () -> {
            RootCommandNode<CommandSourceStack> root = commands.getDispatcher().getRoot();
            for (String label : labels) {
                removeCommand(root, label);
            }
        });
        updateCommands();
    }

    private void register(Commands commands, CommandNode<FPlayer> rootNode) {
        CommandComponent<FPlayer> component = rootNode.component();
        if (component == null) return;

        String name = component.name();
        LiteralCommandNode<CommandSourceStack> node = brigadierManager.literalBrigadierNodeFactory().createNode(
                name,
                rootNode,
                new CloudBrigadierCommand<>(manager, brigadierManager, this::stripNamespace),
                (fPlayer, permission) -> manager.commandTree().getNamedNode(name) != null && manager.testPermission(fPlayer, permission).allowed()
        );

        Set<String> labels = commands.registerWithFlags(
                plugin.getPluginMeta(),
                node,
                getDescription(rootNode),
                new ArrayList<>(component.alternativeAliases()),
                Set.of(CommandRegistrationFlag.FLATTEN_ALIASES)
        );

        registeredLabels.put(name, labels);
    }

    private @Nullable String getDescription(CommandNode<FPlayer> node) {
        Command<FPlayer> command = node.command();
        if (command != null) {
            return command.commandDescription().description().textDescription();
        }

        for (CommandNode<FPlayer> child : node.children()) {
            String description = getDescription(child);
            if (description != null) {
                return description;
            }
        }

        return null;
    }

    private String stripNamespace(String input) {
        String namespace = plugin.getName().toLowerCase(Locale.ROOT) + ":";
        return input.startsWith(namespace) ? input.substring(namespace.length()) : input;
    }

    private void removeCommand(RootCommandNode<CommandSourceStack> root, String label) {
        try {
            if (removeCommandMethod == null) {
                removeCommandMethod = com.mojang.brigadier.tree.CommandNode.class.getMethod("removeCommand", String.class);
            }

            removeCommandMethod.invoke(root, label);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Failed to remove command " + label, e);
        }
    }

    private void runUnsafe(Commands commands, Runnable runnable) {
        try {
            if (invalidField == null) {
                invalidField = commands.getClass().getDeclaredField("invalid");
                invalidField.setAccessible(true);
            }

            boolean invalid = invalidField.getBoolean(commands);
            try {
                invalidField.setBoolean(commands, false);
                runnable.run();
            } finally {
                invalidField.setBoolean(commands, invalid);
            }
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Failed to modify paper commands", e);
        }
    }

    private void updateCommands() {
        plugin.getServer().getOnlinePlayers().forEach(Player::updateCommands);
    }

}
