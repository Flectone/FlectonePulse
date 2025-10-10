package net.flectone.pulse.module.message.format.world.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.Event;
import net.flectone.pulse.model.event.message.MessageFormattingEvent;
import net.flectone.pulse.model.event.player.PlayerJoinEvent;
import net.flectone.pulse.model.event.player.PlayerLoadEvent;
import net.flectone.pulse.module.message.format.world.WorldModule;
import net.flectone.pulse.processing.context.MessageContext;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class WorldPulseListener implements PulseListener {

    private final WorldModule worldModule;

    @Pulse
    public void onPlayerJoinEvent(PlayerJoinEvent event) {
        FPlayer fPlayer = event.getPlayer();
        worldModule.update(fPlayer);
    }

    @Pulse
    public void onPlayerLoadEvent(PlayerLoadEvent event) {
        if (!event.isReload()) return;

        FPlayer fPlayer = event.getPlayer();
        worldModule.update(fPlayer);
    }

    @Pulse(priority = Event.Priority.HIGH)
    public void onMessageFormattingEvent(MessageFormattingEvent event) {
        MessageContext messageContext = event.getContext();

        worldModule.addTag(messageContext);
    }
}
