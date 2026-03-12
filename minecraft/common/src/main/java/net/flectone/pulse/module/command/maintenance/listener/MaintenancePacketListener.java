package net.flectone.pulse.module.command.maintenance.listener;

import com.github.retrooper.packetevents.event.PacketListener;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.module.command.maintenance.MinecraftMaintenanceModule;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class MaintenancePacketListener implements PacketListener {

    private final MinecraftMaintenanceModule maintenanceModule;

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (event.isCancelled()) return;
        if (event.getPacketType() != PacketType.Status.Client.REQUEST) return;
        if (!maintenanceModule.config().turnedOn()) return;

        event.setCancelled(true);
        maintenanceModule.sendStatus(event.getUser());
    }

    @Override
    public void onPacketSend(PacketSendEvent event) {
        if (event.isCancelled()) return;
        if (event.getPacketType() != PacketType.Play.Server.SERVER_DATA) return;

        maintenanceModule.updateServerData(event);
    }
}
