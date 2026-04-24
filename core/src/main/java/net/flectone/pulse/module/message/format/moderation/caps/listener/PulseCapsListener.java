package net.flectone.pulse.module.message.format.moderation.caps.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.event.Event;
import net.flectone.pulse.model.event.message.MessageFormattingEvent;
import net.flectone.pulse.model.event.message.context.MessageContext;
import net.flectone.pulse.module.message.format.moderation.caps.CapsModule;
import net.flectone.pulse.util.constant.MessageFlag;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class PulseCapsListener implements PulseListener {

    private final CapsModule capsModule;

    @Pulse
    public Event onMessageFormattingEvent(MessageFormattingEvent event) {
        MessageContext messageContext = event.context();
        if (!messageContext.isFlag(MessageFlag.CAPS_MODULE)) return event;
        if (!messageContext.isFlag(MessageFlag.PLAYER_MESSAGE)) return event;

        return event.withContext(capsModule.format(messageContext));
    }
}
