package net.flectone.pulse.module.message.format.world.listener;

import com.github.retrooper.packetevents.event.PacketListener;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerPositionAndLook;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.message.format.world.WorldModule;
import net.flectone.pulse.service.FPlayerService;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class MinecraftPacketWorldListener implements PacketListener {

    private final WorldModule worldModule;
    private final FPlayerService fPlayerService;

    @Override
    public void onPacketSend(PacketSendEvent event) {
        if (event.getPacketType() != PacketType.Play.Server.PLAYER_POSITION_AND_LOOK) return;

        WrapperPlayServerPlayerPositionAndLook wrapper = new WrapperPlayServerPlayerPositionAndLook(event);
        if (wrapper.getRelativeFlags().getFullMask() != 0) {
            FPlayer fPlayer = fPlayerService.getFPlayer(event.getUser().getUUID());
            if (fPlayer.isUnknown()) return;

            worldModule.update(fPlayer);
        }
    }
}
