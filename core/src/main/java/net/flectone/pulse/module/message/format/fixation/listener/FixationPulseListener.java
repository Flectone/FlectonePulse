package net.flectone.pulse.module.message.format.fixation.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.event.message.MessageFormattingEvent;
import net.flectone.pulse.module.message.format.fixation.FixationModule;
import net.flectone.pulse.processing.context.MessageContext;
import net.flectone.pulse.util.constant.MessageFlag;

@Singleton
public class FixationPulseListener implements PulseListener {

    private final FixationModule fixationModule;

    @Inject
    public FixationPulseListener(FixationModule fixationModule) {
        this.fixationModule = fixationModule;
    }

    @Pulse
    public void onMessageFormattingEvent(MessageFormattingEvent event) {
        MessageContext messageContext = event.getContext();
        if (!messageContext.isFlag(MessageFlag.FIXATION)) return;

        fixationModule.format(messageContext);
    }
}
