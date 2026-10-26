package net.flectone.pulse.module.message.format.moderation.spam.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.constant.MessageFlag;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.event.Event;
import net.flectone.pulse.model.event.message.MessageFormattingEvent;
import net.flectone.pulse.model.event.message.context.MessageContext;
import net.flectone.pulse.model.event.player.PlayerQuitEvent;
import net.flectone.pulse.module.message.format.moderation.spam.SpamModule;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class PulseSpamListener implements PulseListener {

    private final SpamModule spamModule;

    @Pulse
    public Event onMessageFormattingEvent(MessageFormattingEvent event) {
        MessageContext messageContext = event.context();
        if (!messageContext.isFlag(MessageFlag.SPAM_MODULE)) return event;
        if (!messageContext.isFlag(MessageFlag.PLAYER_MESSAGE)) return event;

        return event.withContext(spamModule.format(messageContext));
    }

    @Pulse
    public void onPlayerQuit(PlayerQuitEvent event) {
        spamModule.removeMessage(event.player());
    }

}
