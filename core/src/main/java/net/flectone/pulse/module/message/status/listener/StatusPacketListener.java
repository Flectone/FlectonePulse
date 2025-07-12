package net.flectone.pulse.module.message.status.listener;

import com.github.retrooper.packetevents.event.PacketListener;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.configuration.Command;
import net.flectone.pulse.resolver.FileResolver;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.module.message.status.StatusModule;

@Singleton
public class StatusPacketListener implements PacketListener {

    private final Command.Maintenance maintenance;
    private final StatusModule statusModule;

    @Inject
    public StatusPacketListener(FileResolver fileResolver,
                                StatusModule statusModule) {
        this.maintenance = fileResolver.getCommand().getMaintenance();
        this.statusModule = statusModule;
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (event.isCancelled()) return;
        if (event.getPacketType() != PacketType.Status.Client.REQUEST) return;
        if (maintenance.isTurnedOn()) return;
        if (statusModule.checkModulePredicates(FPlayer.UNKNOWN)) return;

        event.setCancelled(true);
        statusModule.send(event.getUser());
    }
}
