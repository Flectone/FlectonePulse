package net.flectone.pulse.module.message.quit.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.Event;
import net.flectone.pulse.model.event.message.MessageReceiveEvent;
import net.flectone.pulse.model.event.player.PlayerQuitEvent;
import net.flectone.pulse.module.message.quit.QuitModule;
import net.kyori.adventure.text.TranslatableComponent;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class QuitPulseListener implements PulseListener {

    private final QuitModule quitModule;

    @Pulse
    public void onPlayerQuitEvent(PlayerQuitEvent event) {
        FPlayer fPlayer = event.player();
        quitModule.send(fPlayer, false);
    }

    @Pulse
    public Event onTranslatableMessageReceive(MessageReceiveEvent event) {
        TranslatableComponent translatableComponent = event.getTranslatableComponent();
        if (translatableComponent == null) return event;

        String translationKey = translatableComponent.key();
        if (!translationKey.equals("multiplayer.player.left")) return event;

        return event.withCancelled(true);
    }

}
