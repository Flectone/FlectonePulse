package net.flectone.pulse.module.message.status.listener;

import com.github.retrooper.packetevents.event.PacketListener;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.module.command.maintenance.MaintenanceModule;
import net.flectone.pulse.module.message.status.MinecraftStatusModule;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class MinecraftPacketStatusListener implements PacketListener {

    private final MaintenanceModule maintenanceModule;
    private final MinecraftStatusModule statusModule;

    @Override
    public void onPacketSend(PacketSendEvent event) {
        if (event.isCancelled()) return;
        if (event.getPacketType() != PacketType.Status.Server.RESPONSE) return;
        if (maintenanceModule.isTurnedOn()) return;

        statusModule.update(event);
    }
}
