package net.flectone.pulse.module.message.bed;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.annotation.Async;
import net.flectone.pulse.configuration.Localization;
import net.flectone.pulse.configuration.Message;
import net.flectone.pulse.configuration.Permission;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.model.event.message.TranslatableMessageEvent;
import net.flectone.pulse.module.AbstractModuleMessage;
import net.flectone.pulse.registry.EventProcessRegistry;
import net.flectone.pulse.resolver.FileResolver;
import net.flectone.pulse.service.FPlayerService;

@Singleton
public class BedModule extends AbstractModuleMessage<Localization.Message.Bed> {

    private final Message.Bed message;
    private final Permission.Message.Bed permission;
    private final FPlayerService fPlayerService;
    private final EventProcessRegistry eventProcessRegistry;

    @Inject
    public BedModule(FileResolver fileResolver,
                     FPlayerService fPlayerService,
                     EventProcessRegistry eventProcessRegistry) {
        super(localization -> localization.getMessage().getBed());

        this.message = fileResolver.getMessage().getBed();
        this.permission = fileResolver.getPermission().getMessage().getBed();
        this.fPlayerService = fPlayerService;
        this.eventProcessRegistry = eventProcessRegistry;
    }

    @Override
    public void onEnable() {
        registerModulePermission(permission);

        createSound(message.getSound(), permission.getSound());

        listenerRegistry.register(BedPacketListener.class);
    }

    @Override
    protected boolean isConfigEnable() {
        return message.isEnable();
    }

    @Async
    public void send(UUID receiver, MinecraftTranslationKeys key) {
        FPlayer fPlayer = fPlayerService.getFPlayer(receiver);
        if (checkModulePredicates(fPlayer)) return;

        builder(fPlayer)
                .destination(message.getDestination())
                .receiver(fPlayer)
                .format(bed -> switch (key) {
                    case BLOCK_MINECRAFT_BED_NO_SLEEP -> bed.getNoSleep();
                    case BLOCK_MINECRAFT_BED_NOT_SAFE -> bed.getNotSafe();
                    case BLOCK_MINECRAFT_BED_OBSTRUCTED -> bed.getObstructed();
                    case BLOCK_MINECRAFT_BED_OCCUPIED -> bed.getOccupied();
                    case BLOCK_MINECRAFT_BED_TOO_FAR_AWAY -> bed.getTooFarAway();
                    default -> "";
                })
                .sound(getSound())
                .sendBuilt();
    }
}

