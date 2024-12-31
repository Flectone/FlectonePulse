package net.flectone.pulse.module.message.status.listener;

import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.file.Command;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.listener.AbstractPacketListener;
import net.flectone.pulse.module.message.status.StatusModule;

@Singleton
public class StatusPacketListener extends AbstractPacketListener {

    private final Command.Maintenance maintenance;

    private final StatusModule statusModule;

    @Inject
    public StatusPacketListener(FileManager fileManager,
                                StatusModule statusModule) {
        this.statusModule = statusModule;

        maintenance = fileManager.getCommand().getMaintenance();
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (event.isCancelled()) return;
        if (event.getPacketType() != PacketType.Status.Client.REQUEST) return;
        if (maintenance.isTurnedOn()) return;

        event.setCancelled(true);
        statusModule.send(event.getUser());
    }
}
