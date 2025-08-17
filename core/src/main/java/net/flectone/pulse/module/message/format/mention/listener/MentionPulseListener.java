package net.flectone.pulse.module.message.format.mention.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.event.message.MessageFormattingEvent;
import net.flectone.pulse.module.message.format.mention.MentionModule;
import net.flectone.pulse.processing.context.MessageContext;
import net.flectone.pulse.util.constant.MessageFlag;

@Singleton
public class MentionPulseListener implements PulseListener {

    private final MentionModule mentionModule;

    @Inject
    public MentionPulseListener(MentionModule mentionModule) {
        this.mentionModule = mentionModule;
    }

    @Pulse
    public void onMessageFormattingEvent(MessageFormattingEvent event) {
        MessageContext messageContext = event.getContext();
        if (!messageContext.isFlag(MessageFlag.MENTION)) return;

        mentionModule.format(messageContext);
        mentionModule.addTags(messageContext);
    }
}
