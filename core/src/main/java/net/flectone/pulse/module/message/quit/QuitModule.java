package net.flectone.pulse.module.message.quit;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.execution.dispatcher.MessageDispatcher;
import net.flectone.pulse.execution.scheduler.TaskScheduler;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.EventMetadata;
import net.flectone.pulse.model.event.message.MessageSendEvent;
import net.flectone.pulse.model.util.Range;
import net.flectone.pulse.module.ModuleLocalization;
import net.flectone.pulse.module.integration.IntegrationModule;
import net.flectone.pulse.module.message.quit.model.QuitMetadata;
import net.flectone.pulse.platform.adapter.PlatformServerAdapter;
import net.flectone.pulse.platform.controller.ModuleController;
import net.flectone.pulse.platform.registry.ProxyRegistry;
import net.flectone.pulse.platform.sender.IntegrationSender;
import net.flectone.pulse.util.constant.ModuleName;
import net.flectone.pulse.util.file.FileFacade;

import java.util.List;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class QuitModule implements ModuleLocalization<Localization.Message.Quit> {

    private final FileFacade fileFacade;
    private final IntegrationModule integrationModule;
    private final TaskScheduler taskScheduler;
    private final MessageDispatcher messageDispatcher;
    private final ModuleController moduleController;
    private final PlatformServerAdapter platformServerAdapter;
    private final IntegrationSender integrationSender;
    private final ProxyRegistry proxyRegistry;

    @Override
    public ModuleName name() {
        return ModuleName.MESSAGE_QUIT;
    }

    @Override
    public Message.Quit config() {
        return fileFacade.message().quit();
    }

    @Override
    public Permission.Message.Quit permission() {
        return fileFacade.permission().message().quit();
    }

    @Override
    public Localization.Message.Quit localization(FEntity sender) {
        return fileFacade.localization(sender).message().quit();
    }

    public boolean isProxyMode() {
        return config().range().type() == Range.Type.PROXY && proxyRegistry.hasEnabledProxy();
    }

    public void proxySend(FPlayer fPlayer) {
        if (isProxyMode()) {
            privateSend(fPlayer, Range.get(Range.Type.SERVER), false, false, 0L);
        }
    }

    public void sendLater(FPlayer fPlayer) {
        if (isProxyMode() && platformServerAdapter.getPlatformPlayerCount() > 1) {
            sendToIntegration(fPlayer);
            return;
        }

        taskScheduler.runAsync(() -> privateSend(fPlayer, config().range(), true,false, 5L));
    }

    public void send(FPlayer fPlayer, boolean ignoreVanish) {
        if (isProxyMode() && platformServerAdapter.getPlatformPlayerCount() > 1 && !ignoreVanish) {
            sendToIntegration(fPlayer);
            return;
        }

        taskScheduler.runAsync(() -> privateSend(fPlayer, config().range(), !ignoreVanish, ignoreVanish, 0L));
    }


    private void privateSend(FPlayer fPlayer, Range range, boolean toIntegration, boolean ignoreVanish, long delay) {
        if (moduleController.isDisabledFor(this, fPlayer)) return;

        EventMetadata<Localization.Message.Quit> eventMetadata = buildEventMetadata(fPlayer, range, toIntegration, ignoreVanish);
        List<FPlayer> receivers = messageDispatcher.createReceivers(this, eventMetadata);
        if (receivers.isEmpty()) return;

        List<MessageSendEvent> messageEvents = receivers.stream()
                .map(fReceiver -> messageDispatcher.createMessageEvent(fReceiver, name(), this, eventMetadata))
                .toList();

        if (delay == 0) {
            messageEvents.forEach(messageDispatcher::dispatch);
        } else {
            taskScheduler.runAsyncLater(() -> messageEvents.forEach(messageDispatcher::dispatch), delay);
        }
    }

    private void sendToIntegration(FPlayer fPlayer) {
        EventMetadata<Localization.Message.Quit> eventMetadata = buildEventMetadata(fPlayer, config().range(),true, false);
        integrationSender.send(name(), eventMetadata.resolveFormat(FPlayer.UNKNOWN, localization()), eventMetadata);
    }

    private EventMetadata<Localization.Message.Quit> buildEventMetadata(FPlayer fPlayer, Range range, boolean toIntegration, boolean ignoreVanish) {
        EventMetadata.Builder<Localization.Message.Quit> eventMetadata = EventMetadata.<Localization.Message.Quit>builder()
                .sender(fPlayer)
                .format(Localization.Message.Quit::format)
                .destination(config().destination())
                .range(range)
                .sound(soundOrThrow())
                .filter(fReceiver -> ignoreVanish || integrationModule.canSeeVanished(fPlayer, fReceiver))
                .proxy(dataOutputStream -> dataOutputStream.writeBoolean(ignoreVanish));

        if (toIntegration) {
            eventMetadata.integration();
        }

        return QuitMetadata.<Localization.Message.Quit>builder()
                .base(eventMetadata.build())
                .ignoreVanish(ignoreVanish)
                .build();
    }
}
