package net.flectone.pulse.module.message.format.moderation.flood.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.event.message.MessageFormattingEvent;
import net.flectone.pulse.module.message.format.moderation.flood.FloodModule;
import net.flectone.pulse.processing.context.MessageContext;
import net.flectone.pulse.util.constant.MessageFlag;

@Singleton
public class FloodPulseListener implements PulseListener {

    private final FloodModule floodModule;

    @Inject
    public FloodPulseListener(FloodModule floodModule) {
        this.floodModule = floodModule;
    }

    @Pulse
    public void onMessageFormattingEvent(MessageFormattingEvent event) {
        MessageContext messageContext = event.getContext();
        if (!messageContext.isFlag(MessageFlag.FLOOD)) return;

        floodModule.format(messageContext);
    }
}
