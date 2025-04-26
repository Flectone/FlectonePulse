package net.flectone.pulse.util;

import com.github.retrooper.packetevents.PacketEvents;
import io.github.retrooper.packetevents.PacketEventsServerMod;

public class TestUtil {

    public static void setup(String MOD_ID) {
        PacketEvents.setAPI(PacketEventsServerMod.constructApi("packetevents"));
        PacketEvents.getAPI().load();
        PacketEventsServerMod.constructApi(MOD_ID).init();
    }

}
