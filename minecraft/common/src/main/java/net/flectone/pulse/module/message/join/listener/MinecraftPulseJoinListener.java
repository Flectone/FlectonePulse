package net.flectone.pulse.module.message.join.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.event.Event;
import net.flectone.pulse.model.event.message.MessageReceiveEvent;
import net.kyori.adventure.text.TranslatableComponent;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class MinecraftPulseJoinListener implements PulseListener {

    @Pulse
    public Event onTranslatableMessageReceiveEvent(MessageReceiveEvent event) {
        TranslatableComponent translatableComponent = event.getTranslatableComponent();
        if (translatableComponent == null) return event;

        String translationKey = translatableComponent.key();
        if (!translationKey.equals("multiplayer.player.joined") && !translationKey.equals("multiplayer.player.joined.renamed")) return event;

        return event.withCancelled(true);
    }
}
