package net.flectone.pulse.module.message.format.translate.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.event.Event;
import net.flectone.pulse.model.event.message.MessageFormattingEvent;
import net.flectone.pulse.module.message.format.translate.TranslateModule;
import net.flectone.pulse.model.event.message.context.MessageContext;
import net.flectone.pulse.util.constant.MessageFlag;

import java.util.UUID;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class TranslatePulseListener implements PulseListener {

    private final TranslateModule translateModule;

    @Pulse
    public Event onMessageFormattingEvent(MessageFormattingEvent event) {
        MessageContext messageContext = event.context();
        if (!messageContext.isFlag(MessageFlag.TRANSLATE)) return event;

        String messageToTranslate = messageContext.userMessage();
        UUID key = translateModule.saveMessage(messageToTranslate);

        return event.withContext(translateModule.addTag(messageContext, key));
    }

}
