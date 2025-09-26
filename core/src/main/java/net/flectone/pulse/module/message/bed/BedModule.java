package net.flectone.pulse.module.message.bed;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.annotation.Async;
import net.flectone.pulse.config.localization.Localization;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.AbstractModuleLocalization;
import net.flectone.pulse.module.message.bed.listener.BedPulseListener;
import net.flectone.pulse.module.message.bed.model.BedMetadata;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.processing.resolver.FileResolver;
import net.flectone.pulse.util.constant.MessageType;
import net.flectone.pulse.util.constant.MinecraftTranslationKey;

@Singleton
public class BedModule extends AbstractModuleLocalization<Localization.Message.Bed> {

    private final FileResolver fileResolver;
    private final ListenerRegistry listenerRegistry;

    @Inject
    public BedModule(FileResolver fileResolver,
                     ListenerRegistry listenerRegistry) {
        super(MessageType.BED);

        this.fileResolver = fileResolver;
        this.listenerRegistry = listenerRegistry;
    }

    @Override
    public void onEnable() {
        registerModulePermission(permission());

        createSound(config().getSound(), permission().getSound());

        listenerRegistry.register(BedPulseListener.class);
    }

    @Override
    public Message.Bed config() {
        return fileResolver.getMessage().getBed();
    }

    @Override
    public Permission.Message.Bed permission() {
        return fileResolver.getPermission().getMessage().getBed();
    }

    @Override
    public Localization.Message.Bed localization(FEntity sender) {
        return fileResolver.getLocalization(sender).getMessage().getBed();
    }

    @Async
    public void send(FPlayer fPlayer, MinecraftTranslationKey minecraftTranslationKey) {
        if (isModuleDisabledFor(fPlayer)) return;

        sendMessage(BedMetadata.<Localization.Message.Bed>builder()
                .sender(fPlayer)
                .range(config().getRange())
                .format(localization -> switch (minecraftTranslationKey) {
                    case BLOCK_MINECRAFT_BED_NO_SLEEP, TILE_BED_NO_SLEEP -> localization.getNoSleep();
                    case BLOCK_MINECRAFT_BED_NOT_SAFE, TILE_BED_NOT_SAFE -> localization.getNotSafe();
                    case BLOCK_MINECRAFT_BED_OBSTRUCTED, BLOCK_MINECRAFT_SPAWN_NOT_VALID -> localization.getObstructed();
                    case BLOCK_MINECRAFT_BED_OCCUPIED, TILE_BED_OCCUPIED -> localization.getOccupied();
                    case BLOCK_MINECRAFT_BED_TOO_FAR_AWAY -> localization.getTooFarAway();
                    default -> "";
                })
                .translationKey(minecraftTranslationKey)
                .destination(config().getDestination())
                .sound(getModuleSound())
                .build()
        );
    }
}

