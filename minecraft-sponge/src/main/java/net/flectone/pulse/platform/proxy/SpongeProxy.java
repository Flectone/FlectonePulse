package net.flectone.pulse.platform.proxy;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.SpongeFlectonePulse;
import net.flectone.pulse.constant.ModuleName;
import net.flectone.pulse.file.FileFacade;
import net.flectone.pulse.listener.proxy.ProxyMessageProcessor;
import net.flectone.pulse.model.entity.FEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.network.EngineConnectionState;
import org.spongepowered.api.network.channel.raw.RawDataChannel;
import org.spongepowered.api.network.channel.raw.play.RawPlayDataHandler;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class SpongeProxy implements Proxy {

    private final FileFacade fileFacade;
    private final SpongeFlectonePulse spongeFlectonePulse;
    private final ProxyMessageProcessor proxyMessageProcessor;

    private RawDataChannel channel;
    private RawPlayDataHandler<EngineConnectionState> handler;

    @Override
    public boolean isEnable() {
        return fileFacade.config().proxy().bungeecord() || fileFacade.config().proxy().velocity();
    }

    @Override
    public void onEnable() {
        if (channel == null) {
            channel = Sponge.channelManager().ofType(ResourceKey.resolve(Proxy.CHANNEL), RawDataChannel.class);
        }

        handler = (buf, _) -> proxyMessageProcessor.process(buf.readBytes(buf.available()));
        channel.play().addHandler(handler);
    }

    @Override
    public void onDisable() {
        if (channel == null || handler == null) return;

        channel.play().removeHandler(handler);
        handler = null;
    }

    @Override
    public boolean sendMessage(@NonNull FEntity sender, @NonNull ModuleName tag, byte @NonNull [] message) {
        if (!isEnable()) return false;
        if (channel == null) return false;

        ServerPlayer player = getOnlinePlayer(sender);
        if (player == null) return false;

        channel.play().sendTo((org.spongepowered.api.entity.living.player.server.ServerPlayer) (Object) player, buf -> buf.writeBytes(message));
        return true;
    }

    @Nullable
    private ServerPlayer getOnlinePlayer(FEntity sender) {
        MinecraftServer minecraftServer = spongeFlectonePulse.getMinecraftServer();
        if (minecraftServer == null) return null;

        PlayerList playerList = minecraftServer.getPlayerList();
        return playerList.getPlayers().stream()
                .filter(player -> !player.getUUID().equals(sender.uuid())) // we always need another player, because sender may no longer be on the server
                .findAny()
                .orElse(playerList.getPlayer(sender.uuid()));
    }

}
