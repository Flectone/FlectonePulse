package net.flectone.pulse.module.message.format.object.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.event.message.MessageFormattingEvent;
import net.flectone.pulse.module.message.format.object.ObjectModule;
import net.flectone.pulse.processing.context.MessageContext;
import net.flectone.pulse.util.constant.MessageFlag;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class ObjectPulseListener implements PulseListener {

    private final ObjectModule objectModule;

    @Pulse
    public void onMessageFormattingEvent(MessageFormattingEvent event) {
        MessageContext messageContext = event.getContext();
        if (!messageContext.isFlag(MessageFlag.OBJECT_PLAYER_HEAD)) return;
        if (!messageContext.isFlag(MessageFlag.OBJECT_SPRITE)) return;

        objectModule.addPlayerHeadTag(messageContext);
        objectModule.addSpriteTag(messageContext);
    }

}
