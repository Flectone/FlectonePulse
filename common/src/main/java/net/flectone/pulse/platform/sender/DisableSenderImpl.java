package net.flectone.pulse.platform.sender;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.constant.ModuleName;
import net.flectone.pulse.constant.SettingText;
import net.flectone.pulse.dispatcher.MessageDispatcher;
import net.flectone.pulse.file.FileFacade;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.EventMetadata;
import net.flectone.pulse.model.event.message.context.MessageContext;
import net.flectone.pulse.service.SocialService;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class DisableSenderImpl implements DisableSender {

    private final FileFacade fileFacade;
    private final MessageDispatcher messageDispatcher;
    private final SocialService socialService;

    @Override
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
