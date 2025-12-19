package net.flectone.pulse.module.message.format.names.listener;

import com.github.retrooper.packetevents.protocol.potion.PotionTypes;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.event.Event;
import net.flectone.pulse.model.event.message.MessageFormattingEvent;
import net.flectone.pulse.module.message.format.names.NamesModule;
import net.flectone.pulse.platform.adapter.PlatformPlayerAdapter;
import net.flectone.pulse.processing.context.MessageContext;
import net.flectone.pulse.util.constant.MessageFlag;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class NamesPulseListener implements PulseListener {

    private final NamesModule namesModule;
    private final PlatformPlayerAdapter platformPlayerAdapter;

    @Pulse(priority = Event.Priority.HIGH)
    public void onMessageFormattingEvent(MessageFormattingEvent event) {
        MessageContext messageContext = event.getContext();
        FEntity sender = messageContext.getSender();
        if (messageContext.isFlag(MessageFlag.INVISIBLE_NAME) && isInvisible(sender)) {
            namesModule.addInvisibleTag(messageContext);
        } else {
            namesModule.addTags(messageContext);
        }
    }

    private boolean isInvisible(FEntity entity) {
        return namesModule.config().shouldCheckInvisibility()
                && platformPlayerAdapter.hasPotionEffect(entity, PotionTypes.INVISIBILITY);
    }
}
