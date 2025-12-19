package net.flectone.pulse.module.message.join;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.annotation.Async;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.AbstractModuleLocalization;
import net.flectone.pulse.module.integration.IntegrationModule;
import net.flectone.pulse.module.message.join.listener.JoinPulseListener;
import net.flectone.pulse.module.message.join.model.JoinMetadata;
import net.flectone.pulse.platform.adapter.PlatformPlayerAdapter;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.util.file.FileFacade;
import net.flectone.pulse.util.constant.MessageType;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class JoinModule extends AbstractModuleLocalization<Localization.Message.Join> {

    private final FileFacade fileFacade;
    private final PlatformPlayerAdapter platformPlayerAdapter;
    private final IntegrationModule integrationModule;
    private final ListenerRegistry listenerRegistry;

    @Override
    public void onEnable() {
        super.onEnable();

        createSound(config().sound(), permission().sound());

        listenerRegistry.register(JoinPulseListener.class);
    }

    @Override
    public MessageType messageType() {
        return MessageType.JOIN;
    }

    @Override
    public Message.Join config() {
        return fileFacade.message().join();
    }

    @Override
    public Permission.Message.Join permission() {
        return fileFacade.permission().message().join();
    }

    @Override
    public Localization.Message.Join localization(FEntity sender) {
        return fileFacade.localization(sender).message().join();
    }

    @Async(delay = 5)
    public void sendLater(FPlayer fPlayer) {
        send(fPlayer, false);
    }

    @Async
    public void send(FPlayer fPlayer, boolean ignoreVanish) {
        if (isModuleDisabledFor(fPlayer)) return;

        boolean hasPlayedBefore = platformPlayerAdapter.hasPlayedBefore(fPlayer);

        sendMessage(JoinMetadata.<Localization.Message.Join>builder()
                .sender(fPlayer)
                .format(s -> hasPlayedBefore || !config().first() ? s.format() : s.formatFirstTime())
                .ignoreVanish(ignoreVanish)
                .playedBefore(hasPlayedBefore)
                .destination(config().destination())
                .range(config().range())
                .sound(getModuleSound())
                .filter(fReceiver -> ignoreVanish || integrationModule.canSeeVanished(fPlayer, fReceiver))
                .proxy(dataOutputStream -> {
                    dataOutputStream.writeBoolean(hasPlayedBefore);
                    dataOutputStream.writeBoolean(ignoreVanish);
                })
                .integration()
                .build()
        );
    }
}
