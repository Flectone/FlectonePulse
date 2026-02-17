package net.flectone.pulse.module.message.format.moderation.swear.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.event.Event;
import net.flectone.pulse.model.event.message.MessageFormattingEvent;
import net.flectone.pulse.module.message.format.moderation.swear.SwearModule;
import net.flectone.pulse.model.event.message.context.MessageContext;
import net.flectone.pulse.util.constant.MessageFlag;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class SwearPulseListener implements PulseListener {

    private final SwearModule swearModule;

    @Pulse
    public Event onMessageFormattingEvent(MessageFormattingEvent event) {
        MessageContext messageContext = event.context();
        if (!messageContext.isFlag(MessageFlag.SWEAR)) return event;
        if (!messageContext.isFlag(MessageFlag.USER_MESSAGE)) return event;

        messageContext = swearModule.format(messageContext);
        messageContext = swearModule.addTag(messageContext);
        return event.withContext(messageContext);
    }
}
