package net.flectone.pulse.module.message.format.questionanswer.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.event.Event;
import net.flectone.pulse.model.event.message.MessageFormattingEvent;
import net.flectone.pulse.module.message.format.questionanswer.QuestionAnswerModule;
import net.flectone.pulse.model.event.message.context.MessageContext;
import net.flectone.pulse.util.constant.MessageFlag;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class QuestionAnswerPulseListener implements PulseListener {

    private final QuestionAnswerModule questionAnswerModule;

    @Pulse
    public Event onMessageFormattingEvent(MessageFormattingEvent event) {
        MessageContext messageContext = event.context();
        if (!messageContext.isFlag(MessageFlag.QUESTION)) return event;

        messageContext = questionAnswerModule.format(messageContext);
        messageContext = questionAnswerModule.addTag(messageContext);
        return event.withContext(messageContext);
    }

}
