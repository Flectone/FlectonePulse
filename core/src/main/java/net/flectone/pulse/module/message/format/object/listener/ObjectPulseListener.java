package net.flectone.pulse.module.message.format.object.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.event.message.MessageFormattingEvent;
import net.flectone.pulse.module.message.format.object.ObjectModule;
import net.flectone.pulse.processing.context.MessageContext;

@Singleton
public class ObjectPulseListener implements PulseListener {

    private final ObjectModule objectModule;

    @Inject
    public ObjectPulseListener(ObjectModule objectModule) {
        this.objectModule = objectModule;
    }

    @Pulse
    public void onMessageFormattingEvent(MessageFormattingEvent event) {
        MessageContext messageContext = event.getContext();

        objectModule.addPlayerHeadTag(messageContext);
        objectModule.addSpriteTag(messageContext);
    }

}
