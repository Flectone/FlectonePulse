package net.flectone.pulse.module.message.sleep;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.annotation.Async;
import net.flectone.pulse.configuration.Localization;
import net.flectone.pulse.configuration.Message;
import net.flectone.pulse.configuration.Permission;
import net.flectone.pulse.constant.MinecraftTranslationKey;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.module.AbstractModuleLocalization;
import net.flectone.pulse.module.message.sleep.listener.SleepPulseListener;
import net.flectone.pulse.registry.ListenerRegistry;
import net.flectone.pulse.resolver.FileResolver;
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
        if (checkModulePredicates(fPlayer)) return;

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
