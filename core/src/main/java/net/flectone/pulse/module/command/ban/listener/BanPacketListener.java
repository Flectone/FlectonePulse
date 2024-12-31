package net.flectone.pulse.module.command.ban.listener;

import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.login.client.WrapperLoginClientLoginStart;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.listener.AbstractPacketListener;
import net.flectone.pulse.module.command.ban.BanModule;

@Singleton
public class BanPacketListener extends AbstractPacketListener {

    private final BanModule banModule;

    @Inject
    public BanPacketListener(BanModule banModule) {
        this.banModule = banModule;
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (event.isCancelled()) return;
        if (event.getPacketType() != PacketType.Login.Client.LOGIN_START) return;

        WrapperLoginClientLoginStart wrapperLoginClientLoginStart = new WrapperLoginClientLoginStart(event);
        if (wrapperLoginClientLoginStart.getPlayerUUID().isEmpty()) return;

        banModule.checkJoin(wrapperLoginClientLoginStart.getPlayerUUID().get(), event.getChannel());
    }

}
