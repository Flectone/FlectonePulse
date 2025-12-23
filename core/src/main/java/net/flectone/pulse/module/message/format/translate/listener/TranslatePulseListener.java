package net.flectone.pulse.module.message.format.translate.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.event.message.MessageFormattingEvent;
import net.flectone.pulse.module.message.format.translate.TranslateModule;
import net.flectone.pulse.processing.context.MessageContext;
import net.flectone.pulse.util.constant.MessageFlag;

import java.util.UUID;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class TranslatePulseListener implements PulseListener {

    private final TranslateModule translateModule;

    @Pulse
    public void onMessageFormattingEvent(MessageFormattingEvent event) {
        MessageContext messageContext = event.context();
        if (!messageContext.isFlag(MessageFlag.TRANSLATE)) return;

        String messageToTranslate = messageContext.getUserMessage();
        UUID key = translateModule.saveMessage(messageToTranslate);

        translateModule.addTag(messageContext, key);
    }

}
