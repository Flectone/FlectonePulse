package net.flectone.pulse.module.integration.twitch.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.execution.scheduler.TaskScheduler;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.event.Event;
import net.flectone.pulse.model.event.EventMetadata;
import net.flectone.pulse.model.event.message.MessagePrepareEvent;
import net.flectone.pulse.module.integration.twitch.TwitchModule;
import net.flectone.pulse.util.constant.ModuleName;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class TwitchPulseListener implements PulseListener {

    private final TaskScheduler taskScheduler;
    private final TwitchModule twitchModule;

    @Pulse(priority = Event.Priority.LOW)
    public void onMessagePrepareEvent(MessagePrepareEvent event) {
        if (!event.isForIntegration()) return;

        ModuleName moduleName = event.moduleName();
        if (moduleName == ModuleName.INTEGRATION_TWITCH) return;

        EventMetadata<?> eventMetadata = event.eventMetadata();
        String format = event.rawFormat();
        taskScheduler.runAsync(() ->
                twitchModule.sendMessage(eventMetadata, moduleName, format)
        );
    }

}
