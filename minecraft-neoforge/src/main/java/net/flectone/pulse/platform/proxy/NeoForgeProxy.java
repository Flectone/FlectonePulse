package net.flectone.pulse.platform.proxy;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.NeoForgeFlectonePulse;
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
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class NeoForgeProxy implements Proxy {

    private final FileFacade fileFacade;
    private final NeoForgeFlectonePulse neoForgeFlectonePulse;
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
        }

        neoForgeFlectonePulse.getLoader().getEventBus().addListener((RegisterPayloadHandlersEvent event) -> {
            if (payloadType == null || streamCodec == null) return;

            PayloadRegistrar registrar = event.registrar("flectonepulse");
            registrar.playBidirectional(
                    payloadType,
                    streamCodec,
                    (payload, _) -> proxyMessageProcessor.process(payload.data())
            );
        });
    }

    @Override
    public void onDisable() {
        if (!isEnable()) return;

        payloadType = null;
    }

    @Override
    public boolean sendMessage(@NonNull FEntity sender, @NonNull ModuleName tag, byte @NonNull [] message) {
        if (!isEnable()) return false;

        MinecraftServer minecraftServer = neoForgeFlectonePulse.getMinecraftServer();
        if (minecraftServer == null) return false;

        ServerPlayer player = getOnlinePlayer(sender);
        if (player == null) return false;

        PacketDistributor.sendToPlayer(player, new ProxyPayload(payloadType, message));
        return true;
    }

    public record ProxyPayload(
            CustomPacketPayload.Type<@NonNull ProxyPayload> type,
            byte[] data
    ) implements CustomPacketPayload {
    }

    @Nullable
    private ServerPlayer getOnlinePlayer(FEntity sender) {
        MinecraftServer minecraftServer = neoForgeFlectonePulse.getMinecraftServer();
        if (minecraftServer == null) return null;

        PlayerList playerList = minecraftServer.getPlayerList();
        return playerList.getPlayers().stream()
                .filter(player -> !player.getUUID().equals(sender.uuid())) // we always need another player, because sender may no longer be on the server
                .findAny()
                .orElse(playerList.getPlayer(sender.uuid()));
    }

}
