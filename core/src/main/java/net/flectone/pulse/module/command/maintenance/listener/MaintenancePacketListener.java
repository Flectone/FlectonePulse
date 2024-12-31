package net.flectone.pulse.module.command.maintenance.listener;

import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.login.client.WrapperLoginClientLoginStart;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.listener.AbstractPacketListener;
import net.flectone.pulse.module.command.maintenance.MaintenanceModule;

@Singleton
public class MaintenancePacketListener extends AbstractPacketListener {

    private final MaintenanceModule maintenanceModule;

    @Inject
    public MaintenancePacketListener(MaintenanceModule maintenanceModule) {
        this.maintenanceModule = maintenanceModule;
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (event.isCancelled()) return;
        if (event.getPacketType() == PacketType.Status.Client.REQUEST) {
            if (!maintenanceModule.getCommand().isTurnedOn()) return;

            event.setCancelled(true);
            maintenanceModule.sendStatus(event.getUser());
            return;
        }

        if (event.getPacketType() == PacketType.Login.Client.LOGIN_START) {
            WrapperLoginClientLoginStart wrapperLoginClientLoginStart = new WrapperLoginClientLoginStart(event);
            if (wrapperLoginClientLoginStart.getPlayerUUID().isEmpty()) return;

            maintenanceModule.checkJoin(wrapperLoginClientLoginStart.getPlayerUUID().get(), event.getChannel());
        }
    }

}
