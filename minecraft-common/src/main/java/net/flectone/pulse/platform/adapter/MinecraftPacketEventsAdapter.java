package net.flectone.pulse.platform.adapter;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.PacketEventsAPI;

public interface MinecraftPacketEventsAdapter {

    void load();

    default void init() {
        PacketEvents.getAPI().init();
    }

    default void terminateFailed() {
        try {
            PacketEventsAPI<?> packetEventsAPI = PacketEvents.getAPI();
            if (!packetEventsAPI.isInitialized()) {
                packetEventsAPI.getInjector().uninject();
            }
        } catch (Exception _) {
            // ignore
        }
    }

    default void terminate() {
        PacketEvents.getAPI().terminate();
    }

}
