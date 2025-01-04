package net.flectone.pulse;

import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.wrapper.login.server.WrapperLoginServerDisconnect;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.Getter;
import net.flectone.pulse.listener.AbstractPacketListener;
import net.flectone.pulse.logger.FLogger;
import net.kyori.adventure.text.Component;

@Singleton
public class TestListener extends AbstractPacketListener {

    @Getter
    private final FLogger fLogger;

    @Inject
    public TestListener(FLogger fLogger) {
        this.fLogger = fLogger;
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
//        if (event.getPacketType().getName().equalsIgnoreCase("ENTITY_RELATIVE_MOVE")) return;
//        if (event.getPacketType().getName().equalsIgnoreCase("ENTITY_STATUS")) return;
//        if (event.getPacketType().getName().equalsIgnoreCase("ENTITY_HEAD_LOOK")) return;
//        if (event.getPacketType().getName().equalsIgnoreCase("ENTITY_VELOCITY")) return;
//        if (event.getPacketType().getName().equalsIgnoreCase("ENTITY_HEAD_LOOK")) return;
//        if (event.getPacketType().getName().equalsIgnoreCase("ENTITY_POSITION_SYNC")) return;
//        if (event.getPacketType().getName().equalsIgnoreCase("ENTITY_RELATIVE_MOVE_AND_ROTATION")) return;
//        if (event.getPacketType().getName().equalsIgnoreCase("CHUNK_DATA")) return;
//        if (event.getPacketType().getName().equalsIgnoreCase("TIME_UPDATE")) return;
//        if (event.getPacketType().getName().equalsIgnoreCase("BLOCK_CHANGE")) return;
//        if (event.getPacketType().getName().equalsIgnoreCase("SPAWN_ENTITY")) return;
//        if (event.getPacketType().getName().equalsIgnoreCase("DESTROY_ENTITIES")) return;
//        if (event.getPacketType().getName().equalsIgnoreCase("BUNDLE")) return;
//        if (event.getPacketType().getName().equalsIgnoreCase("ENTITY_EQUIPMENT")) return;
//        if (event.getPacketType().getName().equalsIgnoreCase("DAMAGE_EVENT")) return;
//        if (event.getPacketType().getName().equalsIgnoreCase("ENTITY_ROTATION")) return;
//        if (event.getPacketType().getName().equalsIgnoreCase("PLAYER_LIST_HEADER_AND_FOOTER")) return;
//        if (event.getPacketType().getName().equalsIgnoreCase("PLUGIN_MESSAGE")) return;
//        if (event.getPacketType().getName().equalsIgnoreCase("UPDATE_SCORE")) return;
//        if (event.getPacketType().getName().equalsIgnoreCase("UNLOAD_CHUNK")) return;
//        if (event.getPacketType().getName().equalsIgnoreCase("UPDATE_VIEW_POSITION")) return;
//        if (event.getPacketType().getName().equalsIgnoreCase("KEEP_ALIVE")) return;
//        if (event.getPacketType().getName().equalsIgnoreCase("PLAYER_INFO_UPDATE")) return;
//        if (event.getPacketType().getName().equalsIgnoreCase("CLIENT_TICK_END")) return;
//        if (event.getPacketType().getName().equalsIgnoreCase("UPDATE_ATTRIBUTES")) return;
//        if (event.getPacketType().getName().equalsIgnoreCase("ENTITY_METADATA")) return;
//
//        if (event.getPacketType() == PacketType.Play.Client.PLAYER_DIGGING) {
//            WrapperPlayClientPlayerDigging wrapperPlayClientPlayerDigging = new WrapperPlayClientPlayerDigging(event);
//            fLogger.warning(String.valueOf(wrapperPlayClientPlayerDigging.getBlockPosition()));
//        }
//
//        fLogger.warning(event.getPacketType().getName());

        WrapperLoginServerDisconnect wrapperLoginServerDisconnect = new WrapperLoginServerDisconnect(Component.text("TI LOX"));

    }
}
