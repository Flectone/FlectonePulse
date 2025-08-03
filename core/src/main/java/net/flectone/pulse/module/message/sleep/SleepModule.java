package net.flectone.pulse.module.message.sleep;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.annotation.Async;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.util.constant.MinecraftTranslationKey;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.AbstractModuleLocalization;
import net.flectone.pulse.module.message.sleep.listener.SleepPulseListener;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.processing.resolver.FileResolver;
import net.flectone.pulse.service.FPlayerService;

@Singleton
public class SleepModule extends AbstractModuleLocalization<Localization.Message.Sleep> {

    private final Message.Sleep message;
    private final Permission.Message.Sleep permission;
    private final FPlayerService fPlayerService;
    private final ListenerRegistry listenerRegistry;

    @Inject
    public SleepModule(FileResolver fileResolver,
                       FPlayerService fPlayerService,
                       ListenerRegistry listenerRegistry) {
        super(localization -> localization.getMessage().getSleep());

        this.message = fileResolver.getMessage().getSleep();
        this.permission = fileResolver.getPermission().getMessage().getSleep();
        this.fPlayerService = fPlayerService;
        this.listenerRegistry = listenerRegistry;
    }

    @Override
    public void onEnable() {
        registerModulePermission(permission);

        createSound(message.getSound(), permission.getSound());

        listenerRegistry.register(SleepPulseListener.class);
    }

    @Override
    protected boolean isConfigEnable() {
        return message.isEnable();
    }

    @Async
    public void send(FPlayer fPlayer, MinecraftTranslationKey key, String sleepCount, String allCount) {
        if (isModuleDisabledFor(fPlayer)) return;

        builder(fPlayer)
                .destination(message.getDestination())
                .receiver(fPlayer)
                .format(bed -> switch (key) {
                    case SLEEP_NOT_POSSIBLE -> bed.getNotPossible();
                    case SLEEP_PLAYERS_SLEEPING -> bed.getPlayersSleeping()
                            .replace("<sleep_count>", sleepCount)
                            .replace("<all_count>", allCount);
                    case SLEEP_SKIPPING_NIGHT -> bed.getSkippingNight();
                    default -> "";
                })
                .sound(getSound())
                .sendBuilt();
    }
}
