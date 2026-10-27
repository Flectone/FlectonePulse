package net.flectone.pulse.platform.proxy;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.FabricFlectonePulse;
import net.flectone.pulse.constant.ModuleName;
import net.flectone.pulse.file.FileFacade;
import net.flectone.pulse.listener.proxy.ProxyMessageProcessor;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.platform.adapter.FabricVersionAdapter;
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
    private final FabricVersionAdapter fabricVersionAdapter;
    private final ProxyMessageProcessor proxyMessageProcessor;

    @Override
    public boolean isEnable() {
        return fileFacade.config().proxy().bungeecord() || fileFacade.config().proxy().velocity();
    }

    @Override
    public void onEnable() {
        fabricVersionAdapter.registerProxyChannel(Proxy.CHANNEL, proxyMessageProcessor::process);
    }

    @Override
    public void onDisable() {
        if (!isEnable()) return;

        fabricVersionAdapter.unregisterProxyChannel();
    }

    @Override
    public boolean sendMessage(@NonNull FEntity sender, @NonNull ModuleName tag, byte @NonNull [] message) {
        if (!isEnable()) return false;

        MinecraftServer minecraftServer = fabricFlectonePulse.getMinecraftServer();
        if (minecraftServer == null) return false;

        ServerPlayer player = getOnlinePlayer(sender);
        if (player == null) return false;

        fabricVersionAdapter.sendProxyMessage(player, message);
        return true;
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