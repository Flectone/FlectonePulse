package net.flectone.pulse.platform.adapter;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.PacketEventsAPI;
import com.github.retrooper.packetevents.event.UserConnectEvent;
import com.github.retrooper.packetevents.event.UserLoginEvent;
import com.github.retrooper.packetevents.manager.server.ServerManager;
import com.github.retrooper.packetevents.protocol.ConnectionState;
import com.github.retrooper.packetevents.protocol.PacketSide;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.protocol.player.UserProfile;
import com.github.retrooper.packetevents.util.FakeChannelUtil;
import com.github.retrooper.packetevents.util.PacketEventsImplHelper;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.github.retrooper.packetevents.factory.neoforge.NeoForgePacketEventsAPI;
import io.github.retrooper.packetevents.factory.neoforge.NeoForgePlayerManager;
import io.github.retrooper.packetevents.factory.neoforge.NeoForgeServerManager;
import io.github.retrooper.packetevents.handler.PacketDecoder;
import io.github.retrooper.packetevents.handler.PacketEncoder;
import io.github.retrooper.packetevents.impl.netty.manager.player.PlayerManagerAbstract;
import io.github.retrooper.packetevents.util.NeoforgeUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelPipeline;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.BuildConfig;
import net.minecraft.SharedConstants;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.fml.loading.FMLLoader;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class NeoForgePacketEventsAdapter implements MinecraftPacketEventsAdapter {

    // doesn't account for mods like ViaFabric
    @Unique
    private static final ClientVersion CLIENT_VERSION = ClientVersion.getById(SharedConstants.getProtocolVersion());

    @Override
    public void load() {
        System.setProperty("packetevents.nbt.default-max-size", "2097152");
        PacketEvents.setAPI(new NeoForgePacketEventsAPI(BuildConfig.PROJECT_MOD_ID, FMLLoader.getCurrent().getDist()) {
            @Override
            protected ServerManager constructServerManager() {
                SharedConstants.tryDetectVersion();
                String version = SharedConstants.getCurrentVersion().id();
                return new NeoForgeServerManager(version);
            }

            @Override
            protected PlayerManagerAbstract constructPlayerManager() {
                return new NeoForgePlayerManager();
            }
        });
        PacketEvents.getAPI().getSettings().reEncodeByDefault(false).checkForUpdates(false).debug(false);
        PacketEvents.getAPI().load();
    }

    public void preNewPlayerPlace(Connection connection, ServerPlayer player) {
        if (NeoforgeUtil.isOurConnection(connection)) {
            PacketEvents.getAPI().getInjector().setPlayer(connection.channel(), player);
        }
    }

    public void onPlayerLogin(Connection connection, ServerPlayer player) {
        if (!NeoforgeUtil.isOurConnection(connection)) {
            return;
        }

        PacketEventsAPI<?> api = PacketEvents.getAPI();
        User user = api.getPlayerManager().getUser(player);
        if (user == null) {
            Object channelObj = api.getPlayerManager().getChannel(player);

            // Check if it's a fake connection
            if (!FakeChannelUtil.isFakeChannel(channelObj) &&
                    (!api.isTerminated() || api.getSettings().isKickIfTerminated())) {
                // Kick the player if they're not a fake player
                player.connection.disconnect(Component.literal("PacketEvents failed to inject into a channel."));
            }
            return;
        }

        api.getEventManager().callEvent(new UserLoginEvent(user, player));
    }

    public void postRespawn(CallbackInfoReturnable<ServerPlayer> cir) {
        ServerPlayer player = cir.getReturnValue();
        if (NeoforgeUtil.isOurConnection(player.connection.getConnection())) {
            Channel channel = player.connection.getConnection().channel();
            PacketEvents.getAPI().getInjector().setPlayer(channel, player);
        }
    }

    public void configureSerialization(ChannelPipeline pipeline, PacketFlow flow) {
        if (!NeoforgeUtil.isOurConnection(flow)) {
            // if pipeline side doesn't match api side, don't inject into
            // this pipeline - it probably means this is the pipeline from
            // integrated server to minecraft client, which is currently unsupported
            PacketEvents.getAPI().getLogManager().debug("Skipped pipeline injection on " + flow);
            return;
        }

        PacketEvents.getAPI().getLogManager().debug("Game connected!");

        Channel channel = pipeline.channel();
        User user = new User(channel, ConnectionState.HANDSHAKING,
                CLIENT_VERSION, new UserProfile(null, null));
        PacketEvents.getAPI().getProtocolManager().setUser(channel, user);

        UserConnectEvent connectEvent = new UserConnectEvent(user);
        PacketEvents.getAPI().getEventManager().callEvent(connectEvent);
        if (connectEvent.isCancelled()) {
            channel.unsafe().closeForcibly();
            return;
        }

        PacketSide apiSide = PacketEvents.getAPI().getInjector().getPacketSide();
        channel.pipeline().addAfter("splitter", PacketEvents.DECODER_NAME, new PacketDecoder(apiSide, user));
        channel.pipeline().addAfter("prepender", PacketEvents.ENCODER_NAME, new PacketEncoder(apiSide, user));
        channel.closeFuture().addListener((ChannelFutureListener) _ -> PacketEventsImplHelper.handleDisconnection(user.getChannel(), user.getUUID()));
    }

}
