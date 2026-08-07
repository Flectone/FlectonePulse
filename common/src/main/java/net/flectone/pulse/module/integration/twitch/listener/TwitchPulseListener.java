package net.flectone.pulse.module.integration.twitch.listener;

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
import net.flectone.pulse.module.integration.twitch.TwitchModule;
import net.flectone.pulse.scheduler.TaskScheduler;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class TwitchPulseListener implements PulseListener {

    private final TaskScheduler taskScheduler;
    private final TwitchModule twitchModule;

    @Pulse(priority = Event.Priority.LOW)
    public void onMessagePrepareEvent(MessagePrepareEvent event) {
        IntegrationMessageFormat integrationMessageFormat = event.integrationMessageFormat();
        if (integrationMessageFormat == null) return;

        ModuleName moduleName = event.moduleName();
        if (moduleName == ModuleName.INTEGRATION_TWITCH) return;

        MessageContext messageContext = event.messageContext();
        taskScheduler.runAsync(() ->
                twitchModule.sendMessage(moduleName, messageContext, integrationMessageFormat)
        );
    }

}
