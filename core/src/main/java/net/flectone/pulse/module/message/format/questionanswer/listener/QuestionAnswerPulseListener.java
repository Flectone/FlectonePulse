package net.flectone.pulse.module.message.format.questionanswer.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.event.message.MessageFormattingEvent;
import net.flectone.pulse.module.message.format.questionanswer.QuestionAnswerModule;
import net.flectone.pulse.processing.context.MessageContext;
import net.flectone.pulse.util.constant.MessageFlag;

@Singleton
public class QuestionAnswerPulseListener implements PulseListener {

    private final QuestionAnswerModule questionAnswerModule;

    @Inject
    public QuestionAnswerPulseListener(QuestionAnswerModule questionAnswerModule) {
        this.questionAnswerModule = questionAnswerModule;
    }

    @Pulse
    public void onMessageFormattingEvent(MessageFormattingEvent event) {
        MessageContext messageContext = event.getContext();
        if (!messageContext.isFlag(MessageFlag.QUESTION)) return;

        questionAnswerModule.format(messageContext);
        questionAnswerModule.addTag(messageContext);
    }

}
