package net.flectone.pulse.module.command.maintenance.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.util.checker.PermissionChecker;
import net.flectone.pulse.config.Command;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.player.PlayerPreLoginEvent;
import net.flectone.pulse.module.command.maintenance.MaintenanceModule;
import net.flectone.pulse.execution.pipeline.MessagePipeline;
import net.flectone.pulse.processing.resolver.FileResolver;
import net.flectone.pulse.service.FPlayerService;
import net.kyori.adventure.text.Component;

@Singleton
public class MaintenancePulseListener implements PulseListener {

    private final Command.Maintenance command;
    private final Permission.Command.Maintenance permission;
    private final MaintenanceModule maintenanceModule;
    private final PermissionChecker permissionChecker;
    private final FPlayerService fPlayerService;
    private final MessagePipeline messagePipeline;

    @Inject
    public MaintenancePulseListener(FileResolver fileResolver,
                                    MaintenanceModule maintenanceModule,
                                    PermissionChecker permissionChecker,
                                    FPlayerService fPlayerService,
                                    MessagePipeline messagePipeline) {
        this.command = fileResolver.getCommand().getMaintenance();
        this.permission = fileResolver.getPermission().getCommand().getMaintenance();
        this.maintenanceModule = maintenanceModule;
        this.permissionChecker = permissionChecker;
        this.fPlayerService = fPlayerService;
        this.messagePipeline = messagePipeline;
    }

    @Pulse
    public void onPlayerPreLoginEvent(PlayerPreLoginEvent event) {
        FPlayer fPlayer = event.getPlayer();
        if (maintenanceModule.isAllowed(fPlayer)) return;

        event.setAllowed(false);

        fPlayerService.loadSettings(fPlayer);
        fPlayerService.loadColors(fPlayer);

        String reasonMessage = maintenanceModule.resolveLocalization(fPlayer).getKick();
        Component reason = messagePipeline.builder(fPlayer, reasonMessage).build();

        event.setKickReason(reason);
    }

}
