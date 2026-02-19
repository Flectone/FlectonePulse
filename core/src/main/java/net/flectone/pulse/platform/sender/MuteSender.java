package net.flectone.pulse.platform.sender;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.execution.dispatcher.EventDispatcher;
import net.flectone.pulse.execution.pipeline.MessagePipeline;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.message.MessageSendEvent;
import net.flectone.pulse.model.event.message.context.MessageContext;
import net.flectone.pulse.platform.formatter.ModerationMessageFormatter;
import net.flectone.pulse.util.checker.MuteChecker;
import net.flectone.pulse.util.constant.MessageType;
import net.kyori.adventure.text.Component;

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
 * @since 1.6.0
 * @author TheFaser
 */
@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class MuteSender {

    private final MuteChecker muteChecker;
    private final MessagePipeline messagePipeline;
    private final ModerationMessageFormatter moderationMessageFormatter;
    private final EventDispatcher eventDispatcher;

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

        String muteMessage = moderationMessageFormatter.buildMuteMessage(fPlayer, status);
        MessageContext muteContext = messagePipeline.createContext(fPlayer, muteMessage);
        Component component = messagePipeline.build(muteContext);

        eventDispatcher.dispatch(new MessageSendEvent(MessageType.ERROR, fPlayer, component));

        return true;
    }

}
