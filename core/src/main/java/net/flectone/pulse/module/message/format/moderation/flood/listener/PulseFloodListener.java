package net.flectone.pulse.module.message.format.moderation.flood.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.event.Event;
import net.flectone.pulse.model.event.message.MessageFormattingEvent;
import net.flectone.pulse.model.event.message.context.MessageContext;
import net.flectone.pulse.module.message.format.moderation.flood.FloodModule;
import net.flectone.pulse.util.constant.MessageFlag;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class PulseFloodListener implements PulseListener {

    private final FloodModule floodModule;

    @Pulse
    public Event onMessageFormattingEvent(MessageFormattingEvent event) {
        MessageContext messageContext = event.context();
        if (!messageContext.isFlag(MessageFlag.FLOOD_MODULE)) return event;
        if (!messageContext.isFlag(MessageFlag.PLAYER_MESSAGE)) return event;

        return event.withContext(floodModule.format(messageContext));
    }
}
