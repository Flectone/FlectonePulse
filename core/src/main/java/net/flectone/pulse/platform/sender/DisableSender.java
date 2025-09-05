package net.flectone.pulse.platform.sender;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.execution.dispatcher.EventDispatcher;
import net.flectone.pulse.execution.pipeline.MessagePipeline;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.message.MessageSendEvent;
import net.flectone.pulse.processing.resolver.FileResolver;
import net.flectone.pulse.util.constant.MessageType;
import net.kyori.adventure.text.Component;

@Singleton
public class DisableSender {

    private final MessagePipeline messagePipeline;
    private final EventDispatcher eventDispatcher;
    private final FileResolver fileResolver;


    @Inject
    public DisableSender(MessagePipeline messagePipeline,
                         EventDispatcher eventDispatcher,
                         FileResolver fileResolver) {
        this.messagePipeline = messagePipeline;
        this.eventDispatcher = eventDispatcher;
        this.fileResolver = fileResolver;
    }

    public boolean sendIfDisabled(FEntity entity, FEntity receiver, MessageType messageType) {
        if (!(receiver instanceof FPlayer fReceiver)) return false;
        if (fReceiver.isUnknown()) return false;
        if (fReceiver.isSetting(messageType)) return false;

        // skip message for entities
        if (!(entity instanceof FPlayer fPlayer)) return true;

        Localization.Command.Chatsetting localization = fileResolver.getLocalization(fReceiver).getCommand().getChatsetting();

        String disableMessage = fPlayer.equals(fReceiver)
                ? localization.getDisabledSelf()
                : localization.getDisabledOther();

        Component component = messagePipeline.builder(receiver, fPlayer, disableMessage).build();

        eventDispatcher.dispatch(new MessageSendEvent(MessageType.ERROR, fPlayer, component));

        return true;
    }

}
