package net.flectone.pulse.platform.proxy;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.flectone.pulse.FabricFlectonePulse;
import net.flectone.pulse.constant.ModuleName;
import net.flectone.pulse.file.FileFacade;
import net.flectone.pulse.listener.proxy.ProxyMessageProcessor;
import net.flectone.pulse.model.entity.FEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class FabricProxy implements Proxy {

    private final FileFacade fileFacade;
    private final FabricFlectonePulse fabricFlectonePulse;
    private final ProxyMessageProcessor proxyMessageProcessor;

    private CustomPacketPayload.Type<@NonNull ProxyPayload> payloadType;
    private StreamCodec<@NonNull FriendlyByteBuf, @NonNull ProxyPayload> streamCodec;

    @Override
    public boolean isEnable() {
        return fileFacade.config().proxy().bungeecord() || fileFacade.config().proxy().velocity();
    }

    @Override
    public void onEnable() {
        payloadType = new CustomPacketPayload.Type<>(Identifier.parse(Proxy.CHANNEL));

        if (streamCodec == null) {
            streamCodec = StreamCodec.of(
                    (buf, payload) -> buf.writeBytes(payload.data()),
                    buf -> {
                        byte[] data = new byte[buf.readableBytes()];
                        buf.readBytes(data);
                        return new ProxyPayload(payloadType, data);
                    }
            );

            PayloadTypeRegistry.clientboundPlay().register(payloadType, streamCodec);
            PayloadTypeRegistry.serverboundPlay().register(payloadType, streamCodec);
        }

        ServerPlayNetworking.registerGlobalReceiver(payloadType, (payload, _) ->
                proxyMessageProcessor.process(payload.data())
        );
    }

    @Override
    public void onDisable() {
        if (!isEnable()) return;

        ServerPlayNetworking.unregisterGlobalReceiver(payloadType.id());
        payloadType = null;
    }

    @Override
    public boolean sendMessage(@NonNull FEntity sender, @NonNull ModuleName tag, byte @NonNull [] message) {
        if (!isEnable()) return false;

        MinecraftServer minecraftServer = fabricFlectonePulse.getMinecraftServer();
        if (minecraftServer == null) return false;

        ServerPlayer player = getOnlinePlayer(sender);
        if (player == null) return false;

        ServerPlayNetworking.send(player, new ProxyPayload(payloadType, message));
        return true;
    }

    public record ProxyPayload(
            CustomPacketPayload.Type<@NonNull ProxyPayload> type,
            byte[] data
    ) implements CustomPacketPayload {
    }

    @Nullable
    private ServerPlayer getOnlinePlayer(FEntity sender) {
        MinecraftServer minecraftServer = fabricFlectonePulse.getMinecraftServer();
        if (minecraftServer == null) return null;

        PlayerList playerList = minecraftServer.getPlayerList();
        return playerList.getPlayers().stream()
                .filter(player -> !player.getUUID().equals(sender.uuid())) // we always need another player, because sender may no longer be on the server
                .findAny()
                .orElse(playerList.getPlayer(sender.uuid()));
    }

}