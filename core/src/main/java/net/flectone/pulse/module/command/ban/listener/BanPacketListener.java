package net.flectone.pulse.module.command.ban.listener;

import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.login.server.WrapperLoginServerLoginSuccess;
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
    public void onPacketSend(PacketSendEvent event) {
        if (event.isCancelled()) return;
        if (event.getPacketType() != PacketType.Login.Server.LOGIN_SUCCESS) return;
        if (!banModule.isEnable()) return;

        event.setCancelled(true);

        WrapperLoginServerLoginSuccess wrapperLoginServerLoginSuccess = new WrapperLoginServerLoginSuccess(event);
        banModule.checkJoin(wrapperLoginServerLoginSuccess.getUserProfile());
    }
}
