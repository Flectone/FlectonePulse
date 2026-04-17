package net.flectone.pulse.platform.proxy;

import com.google.common.collect.Iterables;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.flectone.pulse.FabricFlectonePulse;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.event.EventMetadata;
import net.flectone.pulse.module.message.quit.model.QuitMetadata;
import net.flectone.pulse.platform.handler.ProxyMessageHandler;
import net.flectone.pulse.util.constant.ModuleName;
import net.flectone.pulse.util.file.FileFacade;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.List;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class FabricProxy implements Proxy {

    private final FileFacade fileFacade;
    private final FabricFlectonePulse fabricFlectonePulse;
    private final ProxyMessageHandler proxyMessageHandler;

    private CustomPacketPayload.Type<@NonNull ProxyPayload> channel;
    private StreamCodec<@NonNull FriendlyByteBuf, @NonNull ProxyPayload> streamCodec;

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

            PayloadTypeRegistry.clientboundPlay().register(channel, streamCodec);
            PayloadTypeRegistry.serverboundPlay().register(channel, streamCodec);
        }

        ServerPlayNetworking.registerGlobalReceiver(channel, (payload, _) ->
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
    public boolean sendMessage(@NonNull FEntity sender, @NonNull ModuleName tag, byte @NonNull [] message, @Nullable EventMetadata<?> eventMetadata) {
        if (!isEnable()) return false;

        MinecraftServer minecraftServer = fabricFlectonePulse.getMinecraftServer();
        if (minecraftServer == null) return false;

        ServerPlayer player = getOnlinePlayer(sender, tag, eventMetadata);
        if (player == null) return false;

        ServerPlayNetworking.send(player, new ProxyPayload(channel, message));
        return true;
    }

    public @Nullable String getChannel() {
        if (fileFacade.config().proxy().bungeecord()) {
            return "bungeecord:main";
        }

        if (fileFacade.config().proxy().velocity()) {
            return "flectonepulse:main";
        }

        return null;
    }

    public record ProxyPayload(
            CustomPacketPayload.Type<@NonNull ProxyPayload> type,
            byte[] data
    ) implements CustomPacketPayload {
    }

    @Nullable
    private ServerPlayer getOnlinePlayer(FEntity sender, ModuleName tag, @Nullable EventMetadata<?> eventMetadata) {
        MinecraftServer minecraftServer = fabricFlectonePulse.getMinecraftServer();
        if (minecraftServer == null) return null;

        PlayerList playerList = minecraftServer.getPlayerList();
        List<ServerPlayer> onlinePlayers = playerList.getPlayers();

        if (tag == ModuleName.MESSAGE_QUIT && eventMetadata instanceof QuitMetadata<?> quitMetadata && !quitMetadata.ignoreVanish()) {
            return onlinePlayers.stream()
                    .filter(player -> !player.getUUID().equals(sender.uuid()))
                    .findFirst()
                    .orElse(null);
        }

        ServerPlayer player = playerList.getPlayer(sender.uuid());
        return player != null ? player : Iterables.getFirst(onlinePlayers, null);
    }

}