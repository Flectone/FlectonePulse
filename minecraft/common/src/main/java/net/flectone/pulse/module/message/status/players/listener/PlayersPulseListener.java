package net.flectone.pulse.module.message.status.players.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.execution.pipeline.MessagePipeline;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.Event;
import net.flectone.pulse.model.event.player.PlayerPreLoginEvent;
import net.flectone.pulse.module.message.status.players.PlayersModule;
import net.flectone.pulse.processing.context.MessageContext;
import net.flectone.pulse.service.FPlayerService;
import net.kyori.adventure.text.Component;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class PlayersPulseListener implements PulseListener {

    private final PlayersModule playersModule;
    private final FPlayerService fPlayerService;
    private final MessagePipeline messagePipeline;

    @Pulse
    public Event onPlayerPreLoginEvent(PlayerPreLoginEvent event) {
        FPlayer fPlayer = event.player();
        if (playersModule.isAllowed(fPlayer)) return event;

        fPlayerService.loadColors(fPlayer);

        String reasonMessage = playersModule.localization(fPlayer).full();
        MessageContext reasonContext = messagePipeline.createContext(fPlayer, reasonMessage);
        Component reason = messagePipeline.build(reasonContext);

        return event.withAllowed(false).withKickReason(reason);
    }

}
