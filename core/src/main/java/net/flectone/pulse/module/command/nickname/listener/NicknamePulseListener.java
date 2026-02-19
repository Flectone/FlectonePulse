package net.flectone.pulse.module.command.nickname.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.event.Event;
import net.flectone.pulse.model.event.message.MessageFormattingEvent;
import net.flectone.pulse.model.event.message.context.MessageContext;
import net.flectone.pulse.module.command.nickname.NicknameModule;
import net.flectone.pulse.module.message.format.names.NamesModule;
import net.flectone.pulse.util.constant.MessageFlag;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class NicknamePulseListener implements PulseListener {

    private final NicknameModule nickModule;
    private final NamesModule namesModule;

    @Pulse(priority = Event.Priority.HIGH)
    public Event onMessageFormattingEvent(MessageFormattingEvent event) {
        MessageContext messageContext = event.context();
        if (!messageContext.isFlag(MessageFlag.NICKNAME)) return event;
        if (messageContext.isFlag(MessageFlag.INVISIBLE_NAME) && namesModule.isInvisible(event.context().sender())) return event;
        if (messageContext.isFlag(MessageFlag.USER_MESSAGE)) return event;

        return event.withContext(nickModule.addTag(messageContext));
    }

}
