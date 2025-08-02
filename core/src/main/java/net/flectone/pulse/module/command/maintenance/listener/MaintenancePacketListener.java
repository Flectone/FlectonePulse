package net.flectone.pulse.module.command.maintenance.listener;

import com.github.retrooper.packetevents.event.PacketListener;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.config.Command;
import net.flectone.pulse.module.command.maintenance.MaintenanceModule;
import net.flectone.pulse.processing.resolver.FileResolver;

@Singleton
public class MaintenancePacketListener implements PacketListener {

    private final Command.Maintenance command;
    private final MaintenanceModule maintenanceModule;

    @Inject
    public MaintenancePacketListener(FileResolver fileResolver,
                                     MaintenanceModule maintenanceModule) {
        this.command = fileResolver.getCommand().getMaintenance();
        this.maintenanceModule = maintenanceModule;
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (event.isCancelled()) return;
        if (event.getPacketType() == PacketType.Status.Client.REQUEST) {
            if (!command.isTurnedOn()) return;

            event.setCancelled(true);
            maintenanceModule.sendStatus(event.getUser());
        }
    }
}
