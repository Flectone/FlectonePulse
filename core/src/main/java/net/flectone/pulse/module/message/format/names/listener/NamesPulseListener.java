package net.flectone.pulse.module.message.format.names.listener;

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
import net.flectone.pulse.util.constant.PotionUtil;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class NamesPulseListener implements PulseListener {

    private final NamesModule namesModule;
    private final PlatformPlayerAdapter platformPlayerAdapter;

    @Pulse(priority = Event.Priority.HIGH)
    public Event onMessageFormattingEvent(MessageFormattingEvent event) {
        MessageContext messageContext = event.context();
        FEntity sender = messageContext.sender();
        if (messageContext.isFlag(MessageFlag.INVISIBLE_NAME) && isInvisible(sender)) {
            return event.withContext(namesModule.addInvisibleTag(messageContext));
        } else {
            return event.withContext(namesModule.addTags(messageContext));
        }
    }

    private boolean isInvisible(FEntity entity) {
        return namesModule.config().shouldCheckInvisibility()
                && platformPlayerAdapter.hasPotionEffect(entity, PotionUtil.INVISIBILITY_POTION_NAME);
    }
}
