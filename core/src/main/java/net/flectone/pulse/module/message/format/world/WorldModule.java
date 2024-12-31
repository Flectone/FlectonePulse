package net.flectone.pulse.module.message.format.world;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.annotation.Async;
import net.flectone.pulse.file.Message;
import net.flectone.pulse.file.Permission;
import net.flectone.pulse.manager.FPlayerManager;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.manager.ListenerManager;
import net.flectone.pulse.manager.ThreadManager;
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

    private final ThreadManager threadManager;
    private final FPlayerManager fPlayerManager;
    private final ListenerManager listenerManager;

    @Inject
    public WorldModule(FileManager fileManager,
                       ThreadManager threadManager,
                       FPlayerManager fPlayerManager,
                       ListenerManager listenerManager) {
        this.threadManager = threadManager;
        this.fPlayerManager = fPlayerManager;
        this.listenerManager = listenerManager;

        message = fileManager.getMessage().getFormat().getWorld();
        permission = fileManager.getPermission().getMessage().getFormat().getWorld();
    }

    @Override
    public void reload() {
        registerModulePermission(permission);

        listenerManager.register(WorldPacketListener.class);
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

        if (worldPrefix.equalsIgnoreCase(fPlayer.getWorldPrefix())) return;

        fPlayer.setWorldPrefix(worldPrefix);
        threadManager.runDatabase(database -> database.updateFPlayer(fPlayer));
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
