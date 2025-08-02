package net.flectone.pulse.module.message.format.world.listener;

import com.github.retrooper.packetevents.event.PacketListener;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.message.format.world.WorldModule;
import net.flectone.pulse.service.FPlayerService;

@Singleton
public class WorldPacketListener implements PacketListener {

    private final WorldModule worldModule;
    private final FPlayerService fPlayerService;

    @Inject
    public WorldPacketListener(WorldModule worldModule,
                               FPlayerService fPlayerService) {
        this.worldModule = worldModule;
        this.fPlayerService = fPlayerService;
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (event.getPacketType() != PacketType.Play.Client.TELEPORT_CONFIRM) return;

        FPlayer fPlayer = fPlayerService.getFPlayer(event.getUser().getUUID());

        worldModule.update(fPlayer);
    }
}
