package net.flectone.pulse.platform.sender;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.execution.dispatcher.EventDispatcher;
import net.flectone.pulse.execution.pipeline.MessagePipeline;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.message.MessageSendEvent;
import net.flectone.pulse.platform.formatter.ModerationMessageFormatter;
import net.flectone.pulse.util.checker.MuteChecker;
import net.flectone.pulse.util.constant.MessageType;
import net.kyori.adventure.text.Component;

@Singleton
public class MuteSender {

    private final MuteChecker muteChecker;
    private final MessagePipeline messagePipeline;
    private final ModerationMessageFormatter moderationMessageFormatter;
    private final EventDispatcher eventDispatcher;

    @Inject
    public MuteSender(MuteChecker muteChecker,
                      MessagePipeline messagePipeline,
                      ModerationMessageFormatter moderationMessageFormatter,
                      EventDispatcher eventDispatcher) {
        this.muteChecker = muteChecker;
        this.messagePipeline = messagePipeline;
        this.moderationMessageFormatter = moderationMessageFormatter;
        this.eventDispatcher = eventDispatcher;
    }

    public boolean sendIfMuted(FEntity entity) {
        // skip message for entity
        if (!(entity instanceof FPlayer fPlayer)) return false;

        MuteChecker.Status status = muteChecker.check(fPlayer);
        if (status == MuteChecker.Status.NONE) return false;

        String muteMessage = moderationMessageFormatter.buildMuteMessage(fPlayer, status);
        Component component = messagePipeline.builder(fPlayer, muteMessage)
                .build();

        eventDispatcher.dispatch(new MessageSendEvent(MessageType.ERROR, fPlayer, component));

        return true;
    }

}
