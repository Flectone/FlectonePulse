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
import net.flectone.pulse.util.file.FileFacade;
import net.flectone.pulse.util.constant.MessageType;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class FabricProxy implements Proxy {

    private final FileFacade fileFacade;
    private final FabricFlectonePulse fabricFlectonePulse;
    private final ProxyMessageHandler proxyMessageHandler;

    private CustomPayload.Id<ProxyPayload> channel;
    private PacketCodec<RegistryByteBuf, ProxyPayload> packetCodec;

    @Override
    public boolean isEnable() {
        return channel != null;
    }

    @Override
    public void onEnable() {
        String channelName = getChannel();
        if (channelName == null) return;

        channel = new CustomPayload.Id<>(Identifier.of(channelName));

        if (packetCodec == null) {
            packetCodec = PacketCodec.of(
                    (value, buf) -> buf.writeBytes(value.data),
                    buf -> {
                        byte[] data = new byte[buf.readableBytes()];
                        buf.readBytes(data);
                        return new ProxyPayload(channel, data);
                    }
            );

            PayloadTypeRegistry.playC2S().register(channel, packetCodec);
            PayloadTypeRegistry.playS2C().register(channel, packetCodec);
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

        ServerPlayerEntity player = minecraftServer.getPlayerManager().getPlayer(sender.getUuid());
        if (player == null) {
            player = Iterables.getFirst(minecraftServer.getPlayerManager().getPlayerList(), null);
        }

        if (player == null) return false;

        ServerPlayNetworking.send(player, new ProxyPayload(channel, message));
        return true;
    }

    @Nullable
    public String getChannel() {
        if (fileFacade.config().proxy().bungeecord()) {
            return "bungeecord:main";
        }

        if (fileFacade.config().proxy().velocity()) {
            return "flectonepulse:main";
        }

        return null;
    }

    public record ProxyPayload(Id<? extends CustomPayload> id, byte[] data) implements CustomPayload {

        @Override
        public CustomPayload.Id<? extends CustomPayload> getId() {
            return id;
        }

    }
}