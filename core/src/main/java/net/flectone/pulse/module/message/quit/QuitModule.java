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
import net.flectone.pulse.module.ModuleLocalization;
import net.flectone.pulse.module.integration.IntegrationModule;
import net.flectone.pulse.module.message.quit.model.QuitMetadata;
import net.flectone.pulse.platform.controller.ModuleController;
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

    public void sendLater(FPlayer fPlayer) {
        taskScheduler.runAsync(() -> privateSend(fPlayer, false, 5L));
    }

    public void send(FPlayer fPlayer, boolean ignoreVanish) {
        taskScheduler.runAsync(() -> privateSend(fPlayer, ignoreVanish, 0L));
    }

    private void privateSend(FPlayer fPlayer, boolean ignoreVanish, long delay) {
        if (moduleController.isDisabledFor(this, fPlayer)) return;

        EventMetadata<Localization.Message.Quit> eventMetadata = QuitMetadata.<Localization.Message.Quit>builder()
                .base(EventMetadata.<Localization.Message.Quit>builder()
                        .sender(fPlayer)
                        .format(Localization.Message.Quit::format)
                        .destination(config().destination())
                        .range(config().range())
                        .sound(soundOrThrow())
                        .filter(fReceiver -> ignoreVanish || integrationModule.canSeeVanished(fPlayer, fReceiver))
                        .integration()
                        .proxy(dataOutputStream -> dataOutputStream.writeBoolean(ignoreVanish))
                        .build()
                )
                .ignoreVanish(ignoreVanish)
                .build();

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
}
