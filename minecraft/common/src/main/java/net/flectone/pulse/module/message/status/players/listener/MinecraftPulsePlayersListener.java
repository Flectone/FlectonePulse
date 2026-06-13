package net.flectone.pulse.module.message.status.players.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.execution.pipeline.MessagePipeline;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.Event;
import net.flectone.pulse.model.event.message.context.MessageContext;
import net.flectone.pulse.model.event.player.PlayerPreLoginEvent;
import net.flectone.pulse.module.message.status.players.MinecraftPlayersModule;
import net.kyori.adventure.text.Component;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class MinecraftPulsePlayersListener implements PulseListener {

    private final MinecraftPlayersModule playersModule;
    private final MessagePipeline messagePipeline;

    @Pulse
    public Event onPlayerPreLoginEvent(PlayerPreLoginEvent event) {
        FPlayer fPlayer = event.player();
        if (playersModule.isAllowed(fPlayer)) return event;

        Component reason = messagePipeline.build(MessageContext.builder()
                .sender(fPlayer)
                .message( playersModule.localization(fPlayer).full())
                .build()
        );

        return event.withPlayer(fPlayer).withAllowed(false).withKickReason(reason);
    }

}
