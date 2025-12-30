package net.flectone.pulse.platform.sender;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.execution.dispatcher.EventDispatcher;
import net.flectone.pulse.execution.pipeline.MessagePipeline;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.message.MessageSendEvent;
import net.flectone.pulse.processing.context.MessageContext;
import net.flectone.pulse.util.file.FileFacade;
import net.flectone.pulse.util.constant.MessageType;
import net.kyori.adventure.text.Component;

/**
 * Sends ignore-related messages when players ignore each other.
 *
 * <p><b>Usage example:</b>
 * <pre>{@code
 * IgnoreSender ignoreSender = flectonePulse.get(IgnoreSender.class);
 *
 * // Check if players ignore each other
 * if (ignoreSender.sendIfIgnored(sender, receiver)) {
 *     // One player is ignoring the other
 * }
 * }</pre>
 *
 * @since 1.6.0
 * @author TheFaser
 */
@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class IgnoreSender {

    private final MessagePipeline messagePipeline;
    private final EventDispatcher eventDispatcher;
    private final FileFacade fileFacade;

    /**
     * Checks if two players ignore each other and sends notification to sender.
     *
     * @param sender the player attempting to send a message (receives notification)
     * @param receiver the target player
     * @return true if either player ignores the other, false otherwise
     */
    public boolean sendIfIgnored(FPlayer sender, FPlayer receiver) {
        Localization.Command.Ignore localization = fileFacade.localization(sender).command().ignore();

        if (sender.isIgnored(receiver)) {
            sendMessage(sender, receiver, localization.you());
            return true;
        }

        if (receiver.isIgnored(sender)) {
            sendMessage(sender, receiver, localization.he());
            return true;
        }

        return false;
    }

    private void sendMessage(FPlayer sender, FPlayer receiver, String ignoreMessage) {
        MessageContext messageContext = messagePipeline.createContext(receiver, sender, ignoreMessage);
        Component component = messagePipeline.build(messageContext);

        eventDispatcher.dispatch(new MessageSendEvent(MessageType.ERROR, sender, component));
    }
}
