package net.flectone.pulse.module.message.format.mention.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.event.Event;
import net.flectone.pulse.model.event.message.MessageFormattingEvent;
import net.flectone.pulse.model.event.message.context.MessageContext;
import net.flectone.pulse.module.message.format.mention.MentionModule;
import net.flectone.pulse.util.constant.MessageFlag;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class MentionPulseListener implements PulseListener {

    private final MentionModule mentionModule;

    @Pulse
    public Event onMessageFormattingEvent(MessageFormattingEvent event) {
        MessageContext messageContext = event.context();
        if (!messageContext.isFlag(MessageFlag.MENTION)) return event;
        if (!messageContext.isFlag(MessageFlag.USER_MESSAGE)) return event;

        messageContext = mentionModule.format(messageContext);
        messageContext = mentionModule.addTags(messageContext);
        return event.withContext(messageContext);
    }
}
