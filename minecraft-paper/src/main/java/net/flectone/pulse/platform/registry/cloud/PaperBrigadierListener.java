package net.flectone.pulse.platform.registry.cloud;

import com.destroystokyo.paper.brigadier.BukkitBrigadierCommandSource;
import com.destroystokyo.paper.event.brigadier.CommandRegisteredEvent;
import net.flectone.pulse.model.entity.FPlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginIdentifiableCommand;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.incendo.cloud.brigadier.CloudBrigadierCommand;
import org.incendo.cloud.brigadier.CloudBrigadierManager;
import org.incendo.cloud.internal.CommandNode;

import java.util.Locale;

@SuppressWarnings({"deprecation", "removal", "UnstableApiUsage"})
public class PaperBrigadierListener implements Listener {

    private final Plugin plugin;
    private final PlatformCommandManager<CommandSender> manager;

    private final CloudBrigadierManager<FPlayer, BukkitBrigadierCommandSource> brigadierManager;

    public PaperBrigadierListener(Plugin plugin, PlatformCommandManager<CommandSender> manager) {
        this.plugin = plugin;
        this.manager = manager;
        this.brigadierManager = new CloudBrigadierManager<>(manager, new PaperBrigadierSenderMapper<>(BukkitBrigadierCommandSource::getBukkitSender, manager.senderMapper()));
    }

    public CloudBrigadierManager<FPlayer, ?> getBrigadierManager() {
        return brigadierManager;
    }

    @EventHandler
    public void onCommandRegisteredEvent(CommandRegisteredEvent<BukkitBrigadierCommandSource> event) {
        if (!(event.getCommand() instanceof PluginIdentifiableCommand pluginCommand)) return;
        if (!pluginCommand.getPlugin().equals(plugin)) return;

        String label = event.getCommandLabel();
        CommandNode<FPlayer> node = manager.commandTree().getNamedNode(label.substring(label.indexOf(':') + 1));
        if (node == null || node.component() == null) return;

        String name = node.component().name();
        event.setLiteral(brigadierManager.literalBrigadierNodeFactory().createNode(
                event.getLiteral().getLiteral(),
                node,
                new CloudBrigadierCommand<>(manager, brigadierManager, this::stripNamespace),
                (fPlayer, permission) -> manager.commandTree().getNamedNode(name) != null && manager.testPermission(fPlayer, permission).allowed()
        ));
    }

    private String stripNamespace(String input) {
        String namespace = plugin.getName().toLowerCase(Locale.ROOT) + ":";
        return input.startsWith(namespace) ? input.substring(namespace.length()) : input;
    }

}
