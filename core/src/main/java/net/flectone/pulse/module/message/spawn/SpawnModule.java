package net.flectone.pulse.module.message.spawn;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.annotation.Async;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.util.constant.MinecraftTranslationKey;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.AbstractModuleLocalization;
import net.flectone.pulse.module.message.spawn.listener.SpawnPacketListener;
import net.flectone.pulse.module.message.spawn.listener.SpawnPulseListener;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.processing.resolver.FileResolver;
import net.flectone.pulse.service.FPlayerService;

@Singleton
public class SpawnModule extends AbstractModuleLocalization<Localization.Message.Spawn> {

    private final Message.Spawn message;
    private final Permission.Message.Spawn permission;
    private final FPlayerService fPlayerService;
    private final ListenerRegistry listenerRegistry;

    @Inject
    public SpawnModule(FileResolver fileResolver,
                       FPlayerService fPlayerService,
                       ListenerRegistry listenerRegistry) {
        super(localization -> localization.getMessage().getSpawn());

        this.message = fileResolver.getMessage().getSpawn();
        this.permission = fileResolver.getPermission().getMessage().getSpawn();
        this.fPlayerService = fPlayerService;
        this.listenerRegistry = listenerRegistry;
    }

    @Override
    public void onEnable() {
        registerModulePermission(permission);

        createSound(message.getSound(), permission.getSound());

        listenerRegistry.register(SpawnPacketListener.class);
        listenerRegistry.register(SpawnPulseListener.class);
    }

    @Override
    protected boolean isConfigEnable() {
        return message.isEnable();
    }

    @Async
    public void send(FPlayer fPlayer, MinecraftTranslationKey key) {
        if (isModuleDisabledFor(fPlayer)) return;

        builder(fPlayer)
                .destination(message.getDestination())
                .receiver(fPlayer)
                .format(spawn -> key == MinecraftTranslationKey.BLOCK_MINECRAFT_SET_SPAWN
                        ? spawn.getSet() : spawn.getNotValid())
                .sound(getSound())
                .sendBuilt();
    }

    @Async
    public void send(FPlayer fPlayer, MinecraftTranslationKey key, String x, String y, String z, String angle, String world, String value) {
        if (isModuleDisabledFor(fPlayer)) return;

        FPlayer fTarget = fPlayer;

        boolean isSingle = key == MinecraftTranslationKey.COMMANDS_SPAWNPOINT_SUCCESS_SINGLE
                || key == MinecraftTranslationKey.COMMANDS_SPAWNPOINT_SUCCESS;

        if (isSingle) {
            fTarget = fPlayerService.getFPlayer(value);
            if (fTarget.isUnknown()) return;
        }

        builder(fTarget)
                .destination(message.getDestination())
                .receiver(fPlayer)
                .format(s -> (isSingle ? s.getSingle() : s.getMultiple().replace("<count>", value))
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
