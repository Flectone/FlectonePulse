package net.flectone.pulse.platform.adapter;

import com.google.inject.Singleton;
import net.minecraft.server.network.ServerLoginPacketListenerImpl;

@Singleton
public class FabricVersionAdapterV_1_19_4 extends FabricVersionAdapterV_1_20_2 {

    @Override
    public boolean isAcceptingMessages(ServerLoginPacketListenerImpl listener) {
        return listener.getConnection().isConnected();
    }

}
