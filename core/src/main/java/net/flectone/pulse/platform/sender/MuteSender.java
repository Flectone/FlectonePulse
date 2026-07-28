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

/**
 * Sends mute notifications to players when they attempt to chat while muted.
 *
 * <p><b>Usage example:</b>
 * <pre>{@code
 * MuteSender muteSender = flectonePulse.get(MuteSender.class);
 *
 * // Check if player is muted and send notification
 * if (muteSender.sendIfMuted(player)) {
 *     // Player is muted, message sent
 * }
 * }</pre>
 *
 * @author TheFaser
 * @since 1.6.0
 */
@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class MuteSender {

    private final MuteChecker muteChecker;
    private final ModerationMessageFormatter moderationMessageFormatter;
    private final MessageDispatcher messageDispatcher;

    /**
     * Checks if a player is muted and sends a mute notification.
     * Only sends messages to players, not other entities.
     *
     * @param entity the entity to check
     * @return true if player is muted and notification was sent, false otherwise
     */
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
