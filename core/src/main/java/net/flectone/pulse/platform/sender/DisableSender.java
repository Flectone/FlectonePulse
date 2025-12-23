package net.flectone.pulse.platform.sender;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.execution.dispatcher.EventDispatcher;
import net.flectone.pulse.execution.pipeline.MessagePipeline;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.message.MessageSendEvent;
import net.flectone.pulse.processing.context.MessageContext;
import net.flectone.pulse.util.file.FileFacade;
import net.flectone.pulse.util.constant.MessageType;
import net.kyori.adventure.text.Component;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class DisableSender {

    private final MessagePipeline messagePipeline;
    private final EventDispatcher eventDispatcher;
    private final FileFacade fileFacade;

    public boolean sendIfDisabled(FEntity entity, FEntity receiver, MessageType messageType) {
        if (!(receiver instanceof FPlayer fReceiver)) return false;
        if (fReceiver.isUnknown()) return false;
        if (fReceiver.isSetting(messageType)) return false;

        // skip message for entities
        if (!(entity instanceof FPlayer fPlayer)) return true;

        Localization.Command.Chatsetting localization = fileFacade.localization(fReceiver).command().chatsetting();

        String disableMessage = fPlayer.equals(fReceiver)
                ? localization.disabledSelf()
                : localization.disabledOther();

        MessageContext messageContext = messagePipeline.createContext(receiver, fPlayer, disableMessage);
        Component component = messagePipeline.build(messageContext);

        eventDispatcher.dispatch(new MessageSendEvent(MessageType.ERROR, fPlayer, component));

        return true;
    }

}
