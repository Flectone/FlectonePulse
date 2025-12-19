package net.flectone.pulse.module.message.status.listener;

import com.github.retrooper.packetevents.event.PacketListener;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.module.message.status.StatusModule;
import net.flectone.pulse.util.file.FileFacade;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class StatusPacketListener implements PacketListener {

    private final FileFacade fileFacade;
    private final StatusModule statusModule;

    @Override
    public void onPacketSend(PacketSendEvent event) {
        if (event.isCancelled()) return;
        if (event.getPacketType() != PacketType.Status.Server.RESPONSE) return;
        if (fileFacade.command().maintenance().turnedOn()) return;

        statusModule.update(event);
    }
}
