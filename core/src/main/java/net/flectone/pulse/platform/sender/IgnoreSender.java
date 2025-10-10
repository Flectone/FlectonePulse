package net.flectone.pulse.platform.sender;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.localization.Localization;
import net.flectone.pulse.execution.dispatcher.EventDispatcher;
import net.flectone.pulse.execution.pipeline.MessagePipeline;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.message.MessageSendEvent;
import net.flectone.pulse.processing.resolver.FileResolver;
import net.flectone.pulse.util.constant.MessageType;
import net.kyori.adventure.text.Component;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class IgnoreSender {

    private final MessagePipeline messagePipeline;
    private final EventDispatcher eventDispatcher;
    private final FileResolver fileResolver;

    public boolean sendIfIgnored(FPlayer sender, FPlayer receiver) {
        Localization.Command.Ignore localization = fileResolver.getLocalization(sender).getCommand().getIgnore();

        if (sender.isIgnored(receiver)) {
            sendMessage(sender, receiver, localization.getYou());
            return true;
        }

        if (receiver.isIgnored(sender)) {
            sendMessage(sender, receiver, localization.getHe());
            return true;
        }

        return false;
    }

    private void sendMessage(FPlayer sender, FPlayer receiver, String ignoreMessage) {
        Component component = messagePipeline.builder(receiver, sender, ignoreMessage).build();

        eventDispatcher.dispatch(new MessageSendEvent(MessageType.ERROR, sender, component));
    }
}
