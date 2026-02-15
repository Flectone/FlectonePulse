package net.flectone.pulse.module.command.maintenance.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.execution.pipeline.MessagePipeline;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.player.PlayerPreLoginEvent;
import net.flectone.pulse.module.command.maintenance.MaintenanceModule;
import net.flectone.pulse.model.event.message.context.MessageContext;
import net.flectone.pulse.service.FPlayerService;
import net.kyori.adventure.text.Component;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class MaintenancePulseListener implements PulseListener {

    private final MaintenanceModule maintenanceModule;
    private final FPlayerService fPlayerService;
    private final MessagePipeline messagePipeline;

    @Pulse
    public PlayerPreLoginEvent onPlayerPreLoginEvent(PlayerPreLoginEvent event) {
        FPlayer fPlayer = event.player();
        if (maintenanceModule.isAllowed(fPlayer)) return event;

        fPlayer = fPlayerService.loadColors(fPlayer);

        String reasonMessage = maintenanceModule.localization(fPlayer).kick();
        MessageContext messageContext = messagePipeline.createContext(fPlayer, reasonMessage);
        Component reason = messagePipeline.build(messageContext);

        return event.withPlayer(fPlayer).withAllowed(false).withKickReason(reason);
    }

}
