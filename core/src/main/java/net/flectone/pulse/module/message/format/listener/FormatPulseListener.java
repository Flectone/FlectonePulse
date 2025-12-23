package net.flectone.pulse.module.message.format.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.event.message.MessageFormattingEvent;
import net.flectone.pulse.module.message.format.FormatModule;
import net.flectone.pulse.processing.context.MessageContext;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class FormatPulseListener implements PulseListener {

    private final FormatModule formatModule;

    @Pulse
    public void onMessageFormattingEvent(MessageFormattingEvent event) {
        MessageContext messageContext = event.context();

        formatModule.addTags(messageContext);
    }
}
