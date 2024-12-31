package net.flectone.pulse.module.message.status.players.listener;

import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.login.client.WrapperLoginClientLoginStart;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.listener.AbstractPacketListener;
import net.flectone.pulse.module.message.status.players.PlayersModule;

@Singleton
public class PlayersPacketListener extends AbstractPacketListener {

    private final PlayersModule playersModule;

    @Inject
    public PlayersPacketListener(PlayersModule playersModule) {
        this.playersModule = playersModule;
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (event.getPacketType() != PacketType.Login.Client.LOGIN_START) return;

        WrapperLoginClientLoginStart wrapperLoginClientLoginStart = new WrapperLoginClientLoginStart(event);
        if (wrapperLoginClientLoginStart.getPlayerUUID().isEmpty()) return;

        playersModule.check(wrapperLoginClientLoginStart.getPlayerUUID().get(), event.getChannel());
    }
}
