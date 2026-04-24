package net.flectone.pulse.module.message.status.icon.listener;

import com.github.retrooper.packetevents.event.PacketListener;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.module.message.status.icon.MinecraftIconModule;
import net.flectone.pulse.util.file.FileFacade;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class MinecraftPacketIconListener implements PacketListener {

    private final MinecraftIconModule iconModule;
    private final FileFacade fileFacade;

    @Override
    public void onPacketSend(PacketSendEvent event) {
        if (event.isCancelled()) return;
        if (event.getPacketType() != PacketType.Play.Server.SERVER_DATA) return;
        if (fileFacade.command().maintenance().turnedOn()) return;

        iconModule.update(event);
    }

}
