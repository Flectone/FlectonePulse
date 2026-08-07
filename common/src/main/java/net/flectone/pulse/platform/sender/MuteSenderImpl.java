package net.flectone.pulse.platform.sender;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.execution.dispatcher.MessageDispatcher;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.EventMetadata;
import net.flectone.pulse.model.event.message.context.MessageContext;
import net.flectone.pulse.platform.formatter.ModerationMessageFormatter;
import net.flectone.pulse.util.checker.MuteChecker;
import net.flectone.pulse.util.constant.ModuleName;

import java.util.Optional;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class MuteSenderImpl implements MuteSender {

    private final MuteChecker muteChecker;
    private final ModerationMessageFormatter moderationMessageFormatter;
    private final MessageDispatcher messageDispatcher;

    @Override
    public boolean sendIfMuted(FEntity entity) {
        // skip message for entity
        if (!(entity instanceof FPlayer fPlayer)) return false;

        MuteChecker.Status status = muteChecker.check(fPlayer);
        if (status == MuteChecker.Status.NONE) return false;

        Optional<MessageContext> muteContext = moderationMessageFormatter.createMuteContext(fPlayer, status);
        if (muteContext.isEmpty()) return false;

        messageDispatcher.dispatch(ModuleName.ERROR, EventMetadata.builder()
                .messageContext(_ -> muteContext.get())
                .build()
        );

        return true;
    }

}
