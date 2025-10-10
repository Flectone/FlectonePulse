package net.flectone.pulse.module.message.sleep;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.annotation.Async;
import net.flectone.pulse.config.localization.Localization;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FEntity;
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
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class SleepModule extends AbstractModuleLocalization<Localization.Message.Sleep> {

    private final FileResolver fileResolver;
    private final ListenerRegistry listenerRegistry;

    @Override
    public void onEnable() {
        super.onEnable();

        createSound(config().getSound(), permission().getSound());

        listenerRegistry.register(SleepPulseListener.class);
    }

    @Override
    public MessageType messageType() {
        return MessageType.SLEEP;
    }

    @Override
    public Message.Sleep config() {
        return fileResolver.getMessage().getSleep();
    }

    @Override
    public Permission.Message.Sleep permission() {
        return fileResolver.getPermission().getMessage().getSleep();
    }

    @Override
    public Localization.Message.Sleep localization(FEntity sender) {
        return fileResolver.getLocalization(sender).getMessage().getSleep();
    }

    @Async
    public void send(FPlayer fPlayer, MinecraftTranslationKey translationKey, Sleep sleep) {
        if (isModuleDisabledFor(fPlayer)) return;

        sendMessage(SleepMetadata.<Localization.Message.Sleep>builder()
                .sender(fPlayer)
                .range(config().getRange())
                .format(localization -> switch (translationKey) {
                    case SLEEP_NOT_POSSIBLE -> localization.getNotPossible();
                    case SLEEP_PLAYERS_SLEEPING -> StringUtils.replaceEach(
                            localization.getPlayersSleeping(),
                            new String[]{"<players_sleeping>", "<players>"},
                            new String[]{StringUtils.defaultString(sleep.playersSleeping()), String.valueOf(sleep.players())}
                    );
                    case SLEEP_SKIPPING_NIGHT -> localization.getSkippingNight();
                    default -> "";
                })
                .destination(config().getDestination())
                .sleep(sleep)
                .translationKey(translationKey)
                .sound(getModuleSound())
                .build()
        );
    }
}
