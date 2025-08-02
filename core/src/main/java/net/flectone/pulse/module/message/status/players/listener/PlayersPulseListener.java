package net.flectone.pulse.module.message.status.players.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.player.PlayerPreLoginEvent;
import net.flectone.pulse.module.message.status.players.PlayersModule;
import net.flectone.pulse.execution.pipeline.MessagePipeline;
import net.flectone.pulse.service.FPlayerService;
import net.kyori.adventure.text.Component;

@Singleton
public class PlayersPulseListener implements PulseListener {

    private final PlayersModule playersModule;
    private final FPlayerService fPlayerService;
    private final MessagePipeline messagePipeline;

    @Inject
    public PlayersPulseListener(PlayersModule playersModule,
                                FPlayerService fPlayerService,
                                MessagePipeline messagePipeline) {
        this.playersModule = playersModule;
        this.fPlayerService = fPlayerService;
        this.messagePipeline = messagePipeline;
    }

    @Pulse
    public void onPlayerPreLoginEvent(PlayerPreLoginEvent event) {
        FPlayer fPlayer = event.getPlayer();
        if (playersModule.isAllowed(fPlayer)) return;

        event.setAllowed(false);

        fPlayerService.loadSettings(fPlayer);
        fPlayerService.loadColors(fPlayer);

        String reasonMessage = playersModule.resolveLocalization(fPlayer).getFull();
        Component reason = messagePipeline.builder(fPlayer, reasonMessage).build();

        event.setKickReason(reason);
    }

}
