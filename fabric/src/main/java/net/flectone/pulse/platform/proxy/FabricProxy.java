package net.flectone.pulse.platform.proxy;

import com.google.common.collect.Iterables;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.flectone.pulse.FabricFlectonePulse;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.platform.handler.ProxyMessageHandler;
import net.flectone.pulse.processing.resolver.FileResolver;
import net.flectone.pulse.util.constant.MessageType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class FabricProxy implements Proxy {

    private final FileResolver fileResolver;
    private final FabricFlectonePulse fabricFlectonePulse;
    private final ProxyMessageHandler proxyMessageHandler;

    private CustomPacketPayload.Type<ProxyPayload> channel;
    private StreamCodec<FriendlyByteBuf, ProxyPayload> streamCodec;

    @Override
    public boolean isEnable() {
        return channel != null;
    }

    @Override
    public void onEnable() {
        String channelName = getChannel();
        if (channelName == null) return;

        channel = new CustomPacketPayload.Type<>(Identifier.parse(channelName));

        if (streamCodec == null) {
            streamCodec = StreamCodec.of(
                    (buf, payload) -> buf.writeBytes(payload.data()),
                    buf -> {
                        byte[] data = new byte[buf.readableBytes()];
                        buf.readBytes(data);
                        return new ProxyPayload(channel, data);
                    }
            );

            PayloadTypeRegistry.playC2S().register(channel, streamCodec);
            PayloadTypeRegistry.playS2C().register(channel, streamCodec);
        }

        ServerPlayNetworking.registerGlobalReceiver(channel, (payload, context) ->
                proxyMessageHandler.handleProxyMessage(payload.data())
        );
    }

    @Override
    public void onDisable() {
        if (!isEnable()) return;

        ServerPlayNetworking.unregisterGlobalReceiver(channel.id());
        channel = null;
    }

    @Override
    public boolean sendMessage(FEntity sender, MessageType tag, byte[] message) {
        if (!isEnable()) return false;
        if (tag == null) return false;

        MinecraftServer minecraftServer = fabricFlectonePulse.getMinecraftServer();
        if (minecraftServer == null) return false;

        ServerPlayer player = minecraftServer.getPlayerList().getPlayer(sender.getUuid());
        if (player == null) {
            player = Iterables.getFirst(minecraftServer.getPlayerList().getPlayers(), null);
        }

        if (player == null) return false;

        ServerPlayNetworking.send(player, new ProxyPayload(channel, message));
        return true;
    }

    @Nullable
    public String getChannel() {
        if (fileResolver.getConfig().getProxy().isBungeecord()) {
            return "bungeecord:main";
        }

        if (fileResolver.getConfig().getProxy().isVelocity()) {
            return "flectonepulse:main";
        }

        return null;
    }

    public record ProxyPayload(CustomPacketPayload.Type<ProxyPayload> type, byte[] data) implements CustomPacketPayload {

        @Override
        public Type<ProxyPayload> type() {
            return type;
        }

    }
}