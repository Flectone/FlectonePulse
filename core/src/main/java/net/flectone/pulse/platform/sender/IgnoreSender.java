package net.flectone.pulse.platform.sender;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.execution.dispatcher.MessageDispatcher;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.EventMetadata;
import net.flectone.pulse.model.event.message.context.MessageContext;
import net.flectone.pulse.service.SocialService;
import net.flectone.pulse.util.constant.ModuleName;
import net.flectone.pulse.util.constant.SettingText;
import net.flectone.pulse.util.file.FileFacade;

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
 * @author TheFaser
 * @since 1.6.0
 */
@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class IgnoreSender {

    private final SocialService socialService;
    private final MessageDispatcher messageDispatcher;
    private final FileFacade fileFacade;

    /**
     * Checks if two players ignore each other and sends notification to sender.
     *
     * @param fPlayer the player attempting to send a message (receives notification)
     * @param fTarget the target player
     * @return true if either player ignores the other, false otherwise
     */
    public boolean sendIfIgnored(FPlayer fPlayer, FPlayer fTarget) {
        Localization.Command.Ignore localization = fileFacade.localization(socialService.getSetting(fPlayer, SettingText.LOCALE)).command().ignore();

        if (socialService.isIgnored(fPlayer, fTarget)) {
            sendMessage(fPlayer, fTarget, localization.you());
            return true;
        }

        if (socialService.isIgnored(fTarget, fPlayer)) {
            sendMessage(fPlayer, fTarget, localization.he());
            return true;
        }

        return false;
    }

    private void sendMessage(FPlayer fPlayer, FPlayer fTarget, String ignoreMessage) {
        messageDispatcher.dispatch(ModuleName.ERROR, EventMetadata.builder()
                .filter(fPlayer)
                .messageContext(fResolver -> MessageContext.builder()
                        .sender(fTarget)
                        .receiver(fResolver)
                        .message(ignoreMessage)
                        .build()
                )
                .build()
        );
    }
}
