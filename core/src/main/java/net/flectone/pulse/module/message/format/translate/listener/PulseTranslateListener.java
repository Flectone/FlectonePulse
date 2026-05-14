package net.flectone.pulse.module.message.format.translate.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.event.Event;
import net.flectone.pulse.model.event.message.MessageFormattingEvent;
import net.flectone.pulse.model.event.message.context.MessageContext;
import net.flectone.pulse.module.message.format.translate.TranslateModule;
import net.flectone.pulse.util.constant.MessageFlag;
import net.flectone.pulse.util.logging.FLogger;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class PulseTranslateListener implements PulseListener {

    private final TranslateModule translateModule;
    private final FLogger fLogger;

    @Pulse
    public Event onMessageFormattingEvent(MessageFormattingEvent event) {
        MessageContext messageContext = event.context();
        boolean hasTranslate = messageContext.isFlag(MessageFlag.TRANSLATE_MODULE);
        boolean isPlayerMessage = messageContext.isFlag(MessageFlag.PLAYER_MESSAGE);

        if (!hasTranslate) {
            fLogger.info("[Translate] FormattingEvent: skip uuid=%s — flag TRANSLATE_MODULE is not set",
                    messageContext.messageUUID());
            return event;
        }
        if (isPlayerMessage) {
            fLogger.info("[Translate] FormattingEvent: skip uuid=%s — flag PLAYER_MESSAGE is set (inner build)",
                    messageContext.messageUUID());
            return event;
        }

        fLogger.info("[Translate] FormattingEvent: uuid=%s — passing to addTag", messageContext.messageUUID());
        return event.withContext(translateModule.addTag(messageContext));
    }

}
