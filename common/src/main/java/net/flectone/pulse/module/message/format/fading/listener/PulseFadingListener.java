package net.flectone.pulse.module.message.format.fading.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.event.Event;
import net.flectone.pulse.model.event.message.MessageFormattingEvent;
import net.flectone.pulse.model.event.message.context.StringMessageContext;
import net.flectone.pulse.module.message.format.fading.FadingModule;
import net.flectone.pulse.util.constant.MessageFlag;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class PulseFadingListener implements PulseListener {

    private final FadingModule fadingModule;

    @Pulse
    public Event onMessageFormattingEvent(MessageFormattingEvent event) {
        if (!(event.context() instanceof StringMessageContext messageContext)) return event;
        if (messageContext.isFlag(MessageFlag.PLAYER_MESSAGE)) return event;

        return event.withContext(fadingModule.addTag(messageContext));
    }

}
