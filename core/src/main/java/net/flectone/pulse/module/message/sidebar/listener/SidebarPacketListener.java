package net.flectone.pulse.module.message.sidebar.listener;

import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.listener.AbstractPacketListener;
import net.flectone.pulse.module.message.sidebar.SidebarModule;

import java.util.UUID;

@Singleton
public class SidebarPacketListener extends AbstractPacketListener {

    private final SidebarModule sidebarModule;

    @Inject
    public SidebarPacketListener(SidebarModule sidebarModule) {
        this.sidebarModule = sidebarModule;
    }

    @Override
    public void onPacketSend(PacketSendEvent event) {
        if (event.isCancelled()) return;
        if (event.getPacketType() != PacketType.Play.Server.JOIN_GAME) return;
        if (!sidebarModule.isEnable()) return;

        UUID uuid = event.getUser().getUUID();
        if (uuid == null) return;

        sidebarModule.create(uuid);
    }

}
