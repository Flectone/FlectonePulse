package net.flectone.pulse.module.message.sleep;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.annotation.Async;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.AbstractModuleLocalization;
import net.flectone.pulse.module.message.sleep.listener.SleepPulseListener;
import net.flectone.pulse.module.message.sleep.model.Sleep;
import net.flectone.pulse.module.message.sleep.model.SleepMetadata;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.processing.resolver.FileResolver;
import net.flectone.pulse.util.constant.MessageType;
import net.flectone.pulse.util.constant.MinecraftTranslationKey;
import org.apache.commons.lang3.StringUtils;

@Singleton
public class SleepModule extends AbstractModuleLocalization<Localization.Message.Sleep> {

    private final Message.Sleep message;
    private final Permission.Message.Sleep permission;
    private final ListenerRegistry listenerRegistry;

    @Inject
    public SleepModule(FileResolver fileResolver,
                       ListenerRegistry listenerRegistry) {
        super(localization -> localization.getMessage().getSleep(), MessageType.SLEEP);

        this.message = fileResolver.getMessage().getSleep();
        this.permission = fileResolver.getPermission().getMessage().getSleep();
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
    public void send(FPlayer fPlayer, MinecraftTranslationKey key, Sleep sleep) {
        if (isModuleDisabledFor(fPlayer)) return;

        sendMessage(SleepMetadata.<Localization.Message.Sleep>builder()
                .sender(fPlayer)
                .filterPlayer(fPlayer)
                .format(bed -> switch (key) {
                    case SLEEP_NOT_POSSIBLE -> bed.getNotPossible();
                    case SLEEP_PLAYERS_SLEEPING -> StringUtils.replaceEach(
                            bed.getPlayersSleeping(),
                            new String[]{"<sleep_count>", "<all_count>"},
                            new String[]{String.valueOf(sleep.sleepCount()), String.valueOf(sleep.allCount())}
                    );
                    case SLEEP_SKIPPING_NIGHT -> bed.getSkippingNight();
                    default -> "";
                })
                .destination(message.getDestination())
                .sleep(sleep)
                .sound(getModuleSound())
                .build()
        );
    }
}
