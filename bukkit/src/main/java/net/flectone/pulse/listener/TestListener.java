package net.flectone.pulse.listener;

import com.github.retrooper.packetevents.event.PacketListener;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.packettype.PacketTypeCommon;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.util.logging.FLogger;

@Singleton
public class TestListener implements PacketListener {

    private final FLogger fLogger;

    @Inject
    public TestListener(FLogger fLogger) {
        this.fLogger = fLogger;
    }

    @Override
    public void onPacketSend(PacketSendEvent event) {
        PacketTypeCommon packetType = event.getPacketType();
        if (packetType == PacketType.Play.Client.CLIENT_TICK_END) return;
        if (packetType == PacketType.Play.Client.PLAYER_POSITION) return;

        if (packetType == PacketType.Play.Server.ENTITY_POSITION_SYNC) return;
        if (packetType == PacketType.Play.Server.ENTITY_HEAD_LOOK) return;
        if (packetType == PacketType.Play.Server.ENTITY_RELATIVE_MOVE) return;
        if (packetType == PacketType.Play.Server.ENTITY_VELOCITY) return;
        if (packetType == PacketType.Play.Server.ENTITY_METADATA) return;
        if (packetType == PacketType.Play.Server.CHUNK_DATA) return;
        if (packetType == PacketType.Play.Server.ENTITY_RELATIVE_MOVE_AND_ROTATION) return;
        if (packetType == PacketType.Play.Server.TIME_UPDATE) return;
        if (packetType == PacketType.Play.Server.KEEP_ALIVE) return;
        if (packetType == PacketType.Play.Server.PLAYER_INFO_UPDATE) return;
        if (packetType == PacketType.Play.Server.PLUGIN_MESSAGE) return;
        if (packetType == PacketType.Play.Server.PLAYER_LIST_HEADER_AND_FOOTER) return;



        fLogger.warning(event.getPacketType().getName());
    }

}
