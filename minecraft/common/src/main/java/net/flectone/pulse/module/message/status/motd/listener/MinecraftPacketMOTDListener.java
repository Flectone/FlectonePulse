package net.flectone.pulse.module.message.status.motd.listener;

import com.github.retrooper.packetevents.event.PacketListener;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.module.message.status.motd.MinecraftMOTDModule;
import net.flectone.pulse.util.file.FileFacade;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class MinecraftPacketMOTDListener implements PacketListener {

    private final MinecraftMOTDModule motdModule;
    private final FileFacade fileFacade;

    @Override
    public void onPacketSend(PacketSendEvent event) {
        if (event.isCancelled()) return;
        if (event.getPacketType() != PacketType.Play.Server.SERVER_DATA) return;
        if (fileFacade.command().maintenance().turnedOn()) return;

        motdModule.update(event);
    }

}
