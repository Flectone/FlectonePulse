package net.flectone.pulse.platform.adapter;

import com.github.retrooper.packetevents.PacketEvents;
import com.google.inject.Singleton;
import io.netty.channel.Channel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerLoginPacketListenerImpl;
import org.jspecify.annotations.Nullable;

@Singleton
public class FabricVersionAdapterV_1_19_4 extends FabricVersionAdapterV_1_19_3 {

    @Override
    public Channel getChannel(ServerPlayer player) {
        return (Channel) PacketEvents.getAPI().getProtocolManager().getChannel(player.getUUID());
    }

    @Override
    public int getPing(ServerPlayer player) {
        return player.latency;
    }

    @Override
    public void disconnect(ServerPlayer player, String message) {
        player.connection.disconnect(literal(message));
    }

    @Override
    public boolean isAcceptingMessages(ServerLoginPacketListenerImpl listener) {
        return listener.isAcceptingMessages();
    }

    @Override
    public @Nullable String getLanguage(ServerPlayer player) {
        return null;
    }

}
