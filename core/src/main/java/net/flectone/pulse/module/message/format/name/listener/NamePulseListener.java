package net.flectone.pulse.module.message.format.name.listener;

import com.github.retrooper.packetevents.protocol.potion.PotionTypes;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.event.Event;
import net.flectone.pulse.model.event.message.MessageFormattingEvent;
import net.flectone.pulse.module.message.format.name.NameModule;
import net.flectone.pulse.platform.adapter.PlatformPlayerAdapter;
import net.flectone.pulse.processing.context.MessageContext;
import net.flectone.pulse.processing.resolver.FileResolver;
import net.flectone.pulse.util.constant.MessageFlag;

@Singleton
public class NamePulseListener implements PulseListener {

    private final Message.Format.Name message;
    private final NameModule nameModule;
    private final PlatformPlayerAdapter platformPlayerAdapter;

    @Inject
    public NamePulseListener(FileResolver fileResolver,
                             NameModule nameModule,
                             PlatformPlayerAdapter platformPlayerAdapter) {
        this.message = fileResolver.getMessage().getFormat().getName_();
        this.nameModule = nameModule;
        this.platformPlayerAdapter = platformPlayerAdapter;
    }

    @Pulse(priority = Event.Priority.HIGH)
    public void onMessageFormattingEvent(MessageFormattingEvent event) {
        MessageContext messageContext = event.getContext();
        FEntity sender = messageContext.getSender();
        if (messageContext.isFlag(MessageFlag.INVISIBLE_NAME) && isInvisible(sender)) {
            nameModule.addInvisibleTag(messageContext);
        } else {
            nameModule.addTags(messageContext);
        }
    }

    private boolean isInvisible(FEntity entity) {
        return message.isShouldCheckInvisibility()
                && platformPlayerAdapter.hasPotionEffect(entity, PotionTypes.INVISIBILITY);
    }
}
