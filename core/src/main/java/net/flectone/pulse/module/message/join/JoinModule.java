package net.flectone.pulse.module.message.join;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.execution.scheduler.TaskScheduler;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.AbstractModuleLocalization;
import net.flectone.pulse.module.integration.IntegrationModule;
import net.flectone.pulse.module.message.join.model.JoinMetadata;
import net.flectone.pulse.platform.adapter.PlatformPlayerAdapter;
import net.flectone.pulse.util.constant.MessageType;
import net.flectone.pulse.util.file.FileFacade;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class JoinModule extends AbstractModuleLocalization<Localization.Message.Join> {

    private final FileFacade fileFacade;
    private final PlatformPlayerAdapter platformPlayerAdapter;
    private final IntegrationModule integrationModule;
    private final TaskScheduler taskScheduler;

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

    public void sendLater(FPlayer fPlayer) {
        taskScheduler.runRegionLater(fPlayer, () -> privateSend(fPlayer, false), 5L);
    }

    public void send(FPlayer fPlayer, boolean ignoreVanish) {
        taskScheduler.runRegion(fPlayer, () -> privateSend(fPlayer, ignoreVanish));
    }

    private void privateSend(FPlayer fPlayer, boolean ignoreVanish) {
        if (isModuleDisabledFor(fPlayer)) return;

        boolean hasPlayedBefore = platformPlayerAdapter.hasPlayedBefore(fPlayer);

        sendMessage(JoinMetadata.<Localization.Message.Join>builder()
                .sender(fPlayer)
                .format(s -> hasPlayedBefore || !config().first() ? s.format() : s.formatFirstTime())
                .ignoreVanish(ignoreVanish)
                .playedBefore(hasPlayedBefore)
                .destination(config().destination())
                .range(config().range())
                .sound(soundOrThrow())
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
