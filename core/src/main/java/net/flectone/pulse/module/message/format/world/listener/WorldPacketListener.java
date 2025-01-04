package net.flectone.pulse.module.message.format.world.listener;

import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.listener.AbstractPacketListener;
import net.flectone.pulse.manager.FPlayerManager;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.module.message.format.world.WorldModule;

@Singleton
public class WorldPacketListener extends AbstractPacketListener {

    private final WorldModule worldModule;
    private final FPlayerManager fPlayerManager;

    @Inject
    public WorldPacketListener(WorldModule worldModule,
                               FPlayerManager fPlayerManager) {
        this.worldModule = worldModule;
        this.fPlayerManager = fPlayerManager;
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (event.getPacketType() != PacketType.Play.Client.TELEPORT_CONFIRM) return;

        FPlayer fPlayer = fPlayerManager.get(event.getUser().getUUID());

        worldModule.update(fPlayer);
    }
}
