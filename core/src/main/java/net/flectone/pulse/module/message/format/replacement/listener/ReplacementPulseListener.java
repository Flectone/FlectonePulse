package net.flectone.pulse.module.message.format.replacement.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.event.message.MessageFormattingEvent;
import net.flectone.pulse.module.message.format.replacement.ReplacementModule;
import net.flectone.pulse.processing.context.MessageContext;
import net.flectone.pulse.util.constant.MessageFlag;

@Singleton
public class ReplacementPulseListener implements PulseListener {

    private final ReplacementModule replacementModule;

    @Inject
    public ReplacementPulseListener(ReplacementModule replacementModule) {
        this.replacementModule = replacementModule;
    }

    @Pulse
    public void onMessageFormattingEvent(MessageFormattingEvent event) {
        MessageContext messageContext = event.getContext();
        if (!messageContext.isFlag(MessageFlag.REPLACEMENT)) return;

        replacementModule.format(messageContext);
        replacementModule.addTags(messageContext);
    }

}
