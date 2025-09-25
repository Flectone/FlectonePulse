package net.flectone.pulse.module.command.maintenance.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.execution.pipeline.MessagePipeline;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.player.PlayerPreLoginEvent;
import net.flectone.pulse.module.command.maintenance.MaintenanceModule;
import net.flectone.pulse.service.FPlayerService;
import net.kyori.adventure.text.Component;

@Singleton
public class MaintenancePulseListener implements PulseListener {

    private final MaintenanceModule maintenanceModule;
    private final FPlayerService fPlayerService;
    private final MessagePipeline messagePipeline;

    @Inject
    public MaintenancePulseListener(MaintenanceModule maintenanceModule,
                                    FPlayerService fPlayerService,
                                    MessagePipeline messagePipeline) {
        this.maintenanceModule = maintenanceModule;
        this.fPlayerService = fPlayerService;
        this.messagePipeline = messagePipeline;
    }

    @Pulse
    public void onPlayerPreLoginEvent(PlayerPreLoginEvent event) {
        FPlayer fPlayer = event.getPlayer();
        if (maintenanceModule.isAllowed(fPlayer)) return;

        event.setAllowed(false);

        fPlayerService.loadColors(fPlayer);

        String reasonMessage = maintenanceModule.localization(fPlayer).getKick();
        Component reason = messagePipeline.builder(fPlayer, reasonMessage).build();

        event.setKickReason(reason);
    }

}
