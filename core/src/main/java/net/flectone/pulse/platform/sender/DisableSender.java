package net.flectone.pulse.platform.sender;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.execution.dispatcher.MessageDispatcher;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.EventMetadata;
import net.flectone.pulse.model.event.message.context.MessageContext;
import net.flectone.pulse.service.SocialService;
import net.flectone.pulse.util.constant.ModuleName;
import net.flectone.pulse.util.constant.SettingText;
import net.flectone.pulse.util.file.FileFacade;

/**
 * Sends disable messages when chat features are disabled for players.
 *
 * <p><b>Usage example:</b>
 * <pre>{@code
 * DisableSender disableSender = flectonePulse.get(DisableSender.class);
 *
 * // Check if private messaging is disabled for receiver
 * if (disableSender.sendIfDisabled(sender, receiver, MessageType.COMMAND_ME)) {
 *     // Private messaging is disabled for receiver
 * }
 * }</pre>
 *
 * @author TheFaser
 * @since 1.6.0
 */
@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class DisableSender {

    private final FileFacade fileFacade;
    private final MessageDispatcher messageDispatcher;
    private final SocialService socialService;

    /**
     * Checks if a message type is disabled for a receiver and sends appropriate message.
     *
     * @param entity the entity sending the message
     * @param receiver the entity receiving the message
     * @param moduleName the type of message being sent
     * @return true if message type is disabled for receiver, false otherwise
     */
    public boolean sendIfDisabled(FEntity entity, FEntity receiver, ModuleName moduleName) {
        if (!(receiver instanceof FPlayer fReceiver)) return false;
        if (fReceiver.isUnknown()) return false;
        if (socialService.isSetting(fReceiver, moduleName)) return false;

        // skip message for entities
        if (!(entity instanceof FPlayer fPlayer)) return true;

        Localization.Command.Chatsetting localization = fileFacade.localization(socialService.getSetting(fReceiver, SettingText.LOCALE)).command().chatsetting();

        String disableMessage = fPlayer.equals(fReceiver)
                ? localization.disabledSelf()
                : localization.disabledOther();

        messageDispatcher.dispatch(ModuleName.ERROR, EventMetadata.builder()
                .filter(fPlayer)
                .messageContext(fResolver -> MessageContext.builder()
                        .sender(receiver)
                        .receiver(fResolver)
                        .message(disableMessage)
                        .build()
                )
                .build()
        );

        return true;
    }

}
