package net.flectone.pulse.module.message.format.style.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.util.constant.MessageFlag;
import net.flectone.pulse.processing.context.MessageContext;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.Event;
import net.flectone.pulse.model.event.message.MessageFormattingEvent;

@Singleton
public class StylePulseListener implements PulseListener {

    @Inject
    public StylePulseListener() {
    }

    @Pulse(priority = Event.Priority.HIGH)
    public void onMessageProcessingEvent(MessageFormattingEvent event) {
        MessageContext messageContext = event.getContext();
        if (messageContext.isFlag(MessageFlag.USER_MESSAGE)) return;
        if (!(messageContext.getSender() instanceof FPlayer sender)) return;

        String style = sender.getSettingValue(FPlayer.Setting.STYLE);

        // bad practice, but only it works
        String processedMessage = messageContext.getMessage()
                .replace("<style>", style == null ? "" : style)
                .replace("</style>", "");

        messageContext.setMessage(processedMessage);
    }
}
