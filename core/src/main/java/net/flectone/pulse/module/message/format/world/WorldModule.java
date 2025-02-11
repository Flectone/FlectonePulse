package net.flectone.pulse.module.message.format.world;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.annotation.Async;
import net.flectone.pulse.database.dao.FPlayerDAO;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.manager.FPlayerManager;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.registry.ListenerRegistry;
import net.flectone.pulse.model.FEntity;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.module.AbstractModule;
import net.flectone.pulse.module.message.format.world.listener.WorldPacketListener;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.jetbrains.annotations.NotNull;

@Singleton
public class WorldModule extends AbstractModule {

    private final Message.Format.World message;
    private final Permission.Message.Format.World permission;

    private final FPlayerDAO fPlayerDAO;
    private final FPlayerManager fPlayerManager;
    private final ListenerRegistry listenerRegistry;

    @Inject
    public WorldModule(FileManager fileManager,
                       FPlayerDAO fPlayerDAO,
                       FPlayerManager fPlayerManager,
                       ListenerRegistry listenerRegistry) {
        this.fPlayerDAO = fPlayerDAO;
        this.fPlayerManager = fPlayerManager;
        this.listenerRegistry = listenerRegistry;

        message = fileManager.getMessage().getFormat().getWorld();
        permission = fileManager.getPermission().getMessage().getFormat().getWorld();
    }

    @Override
    public void reload() {
        registerModulePermission(permission);

        listenerRegistry.register(WorldPacketListener.class);
    }

    @Override
    public boolean isConfigEnable() {
        return message.isEnable();
    }

    @Async
    public void update(FPlayer fPlayer) {
        if (checkModulePredicates(fPlayer)) return;

        String worldPrefix = message.getMode() == WorldMode.TYPE
                ? message.getValues().get(fPlayerManager.getWorldEnvironment(fPlayer))
                : message.getValues().get(fPlayerManager.getWorldName(fPlayer));

        if (worldPrefix == null && fPlayer.getWorldPrefix() == null) return;
        if (worldPrefix != null && worldPrefix.equalsIgnoreCase(fPlayer.getWorldPrefix())) return;

        fPlayer.setWorldPrefix(worldPrefix);
        fPlayerDAO.updateFPlayer(fPlayer);
    }

    public TagResolver worldTag(@NotNull FEntity sender) {
        if (checkModulePredicates(sender)) return TagResolver.empty();
        if (!(sender instanceof FPlayer fPlayer)) return TagResolver.empty();

        return TagResolver.resolver("world_prefix", (argumentQueue, context) -> {

            String worldPrefix = fPlayer.getWorldPrefix();
            if (worldPrefix == null) return Tag.selfClosingInserting(Component.empty());

            return Tag.preProcessParsed(worldPrefix);
        });
    }
}
