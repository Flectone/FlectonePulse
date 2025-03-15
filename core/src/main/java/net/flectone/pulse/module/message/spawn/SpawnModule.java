package net.flectone.pulse.module.message.spawn;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.annotation.Async;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.manager.FPlayerManager;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.module.AbstractModuleMessage;
import net.flectone.pulse.module.message.spawn.listener.ChangeGameStatePacketListener;
import net.flectone.pulse.module.message.spawn.listener.SetSpawnPacketListener;
import net.flectone.pulse.module.message.spawn.listener.SpawnpointPacketListener;
import net.flectone.pulse.registry.ListenerRegistry;
import net.flectone.pulse.util.MinecraftTranslationKeys;

import java.util.UUID;

@Singleton
public class SpawnModule extends AbstractModuleMessage<Localization.Message.Spawn> {

    private final Message.Spawn message;
    private final Permission.Message.Spawn permission;

    private final FPlayerManager fPlayerManager;
    private final ListenerRegistry listenerRegistry;

    @Inject
    public SpawnModule(FileManager fileManager,
                       FPlayerManager fPlayerManager,
                       ListenerRegistry listenerRegistry) {
        super(localization -> localization.getMessage().getSpawn());

        this.fPlayerManager = fPlayerManager;
        this.listenerRegistry = listenerRegistry;

        message = fileManager.getMessage().getSpawn();
        permission = fileManager.getPermission().getMessage().getSpawn();
    }

    @Override
    public void reload() {
        registerModulePermission(permission);

        createSound(message.getSound(), permission.getSound());

        listenerRegistry.register(ChangeGameStatePacketListener.class);
        listenerRegistry.register(SetSpawnPacketListener.class);
        listenerRegistry.register(SpawnpointPacketListener.class);
    }

    @Override
    public boolean isConfigEnable() {
        return message.isEnable();
    }

    @Async
    public void send(UUID receiver, MinecraftTranslationKeys key) {
        FPlayer fPlayer = fPlayerManager.get(receiver);
        if (checkModulePredicates(fPlayer)) return;

        builder(fPlayer)
                .destination(message.getDestination())
                .receiver(fPlayer)
                .format(spawn -> key == MinecraftTranslationKeys.BLOCK_MINECRAFT_SET_SPAWN
                        ? spawn.getSet() : spawn.getNotValid())
                .sound(getSound())
                .sendBuilt();
    }

    @Async
    public void send(UUID receiver, MinecraftTranslationKeys key, String x, String y, String z, String angle, String world, String value) {
        FPlayer fPlayer = fPlayerManager.get(receiver);
        if (checkModulePredicates(fPlayer)) return;

        FPlayer fTarget = fPlayer;

        if (key == MinecraftTranslationKeys.COMMANDS_SPAWNPOINT_SUCCESS_SINGLE) {
            fTarget = fPlayerManager.getOnline(value);
            if (fTarget.isUnknown()) return;
        }

        builder(fTarget)
                .destination(message.getDestination())
                .receiver(fPlayer)
                .format(s -> (key == MinecraftTranslationKeys.COMMANDS_SPAWNPOINT_SUCCESS_SINGLE
                        ? s.getSingle() : s.getMultiple().replace("<count>", value))
                        .replace("<x>", x)
                        .replace("<y>", y)
                        .replace("<z>", z)
                        .replace("<angle>", angle)
                        .replace("<world>", world)
                )
                .sound(getSound())
                .sendBuilt();
    }
}
