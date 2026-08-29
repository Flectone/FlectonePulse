package net.flectone.pulse.module.integration.telegram.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.constant.ModuleName;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.event.Event;
import net.flectone.pulse.model.event.IntegrationMessageFormat;
import net.flectone.pulse.model.event.message.MessagePrepareEvent;
import net.flectone.pulse.model.event.message.context.MessageContext;
import net.flectone.pulse.module.integration.telegram.TelegramModule;
import net.flectone.pulse.scheduler.TaskScheduler;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class TelegramPulseListener implements PulseListener {

    private final TaskScheduler taskScheduler;
    private final TelegramModule telegramModule;

    @Pulse(priority = Event.Priority.LOW)
    public void onMessagePrepareEvent(MessagePrepareEvent event) {
        IntegrationMessageFormat integrationMessageFormat = event.integrationMessageFormat();
        if (integrationMessageFormat == null) return;

        ModuleName moduleName = event.moduleName();
        if (moduleName == ModuleName.INTEGRATION_TELEGRAM) return;

        MessageContext messageContext = event.messageContext();
        taskScheduler.runAsync(ModuleName.INTEGRATION_TELEGRAM, () ->
                telegramModule.sendMessage(moduleName, messageContext, integrationMessageFormat)
        );
    }

}
