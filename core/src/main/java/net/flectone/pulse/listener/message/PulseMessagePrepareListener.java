package net.flectone.pulse.listener.message;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.event.Event;
import net.flectone.pulse.model.event.message.MessagePrepareEvent;
import net.flectone.pulse.platform.sender.ProxySender;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class PulseMessagePrepareListener implements PulseListener {

    private final ProxySender proxySender;

    @Pulse(priority = Event.Priority.HIGH)
    public Event onMessagePrepareEvent(MessagePrepareEvent event) {
        if (event.isForProxy() && proxySender.send(event.moduleName(), event.eventMetadata())) {
            return event.withCancelled(true);
        }

        return event;
    }

}
