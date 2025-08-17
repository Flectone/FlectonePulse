package net.flectone.pulse.module.message.format.moderation.swear.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.event.message.MessageFormattingEvent;
import net.flectone.pulse.module.message.format.moderation.swear.SwearModule;
import net.flectone.pulse.processing.context.MessageContext;
import net.flectone.pulse.util.constant.MessageFlag;

@Singleton
public class SwearPulseListener implements PulseListener {

    private final SwearModule swearModule;

    @Inject
    public SwearPulseListener(SwearModule swearModule) {
        this.swearModule = swearModule;
    }

    @Pulse
    public void onMessageFormattingEvent(MessageFormattingEvent event) {
        MessageContext messageContext = event.getContext();
        if (!messageContext.isFlag(MessageFlag.SWEAR)) return;

        swearModule.format(messageContext);
        swearModule.addTag(messageContext);
    }
}
