package net.flectone.pulse.module.message.spawn;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.annotation.Async;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.AbstractModuleLocalization;
import net.flectone.pulse.module.message.spawn.listener.SpawnPacketListener;
import net.flectone.pulse.module.message.spawn.listener.SpawnPulseListener;
import net.flectone.pulse.module.message.spawn.model.Spawn;
import net.flectone.pulse.module.message.spawn.model.SpawnMetadata;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.processing.resolver.FileResolver;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.util.constant.MessageType;
import net.flectone.pulse.util.constant.MinecraftTranslationKey;
import org.apache.commons.lang3.StringUtils;

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
        super(localization -> localization.getMessage().getSpawn(), MessageType.SPAWN);

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

        sendMessage(SpawnMetadata.<Localization.Message.Spawn>builder()
                .sender(fPlayer)
                .filterPlayer(fPlayer)
                .format(spawn -> key == MinecraftTranslationKey.BLOCK_MINECRAFT_SET_SPAWN
                        ? spawn.getSet() : spawn.getNotValid()
                )
                .destination(message.getDestination())
                .translationKey(key)
                .sound(getModuleSound())
                .build()
        );
    }

    @Async
    public void send(FPlayer fPlayer, MinecraftTranslationKey key, Spawn spawn) {
        if (isModuleDisabledFor(fPlayer)) return;

        FPlayer fTarget = fPlayer;

        boolean isSingle = key == MinecraftTranslationKey.COMMANDS_SPAWNPOINT_SUCCESS_SINGLE
                || key == MinecraftTranslationKey.COMMANDS_SPAWNPOINT_SUCCESS;

        if (isSingle) {
            fTarget = fPlayerService.getFPlayer(spawn.value());
            if (fTarget.isUnknown()) return;
        }

        sendMessage(SpawnMetadata.<Localization.Message.Spawn>builder()
                .sender(fTarget)
                .filterPlayer(fPlayer)
                .format(s -> StringUtils.replaceEach(
                        isSingle ? s.getSingle() : s.getMultiple(),
                        new String[]{"<count>", "<x>", "<y>", "<z>", "<angle>", "<world>"},
                        new String[]{spawn.value(), spawn.x(), spawn.y(), spawn.z(), spawn.angle(), spawn.world()}
                ))
                .spawn(spawn)
                .translationKey(key)
                .destination(message.getDestination())
                .sound(getModuleSound())
                .build()
        );
    }
}
