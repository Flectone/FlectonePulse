package net.flectone.pulse.platform.adapter;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.manager.registry.ItemRegistry;
import com.github.retrooper.packetevents.protocol.PacketSide;
import com.github.retrooper.packetevents.protocol.item.type.ItemType;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.github.retrooper.packetevents.factory.fabric.FabricPacketEventsAPI;
import io.github.retrooper.packetevents.factory.fabric.FabricPacketEventsAPIManagerFactory;
import io.github.retrooper.packetevents.loader.ChainLoadData;
import io.github.retrooper.packetevents.manager.registry.FabricRegistryManager;
import io.github.retrooper.packetevents.util.FabricInjectionUtil;
import io.github.retrooper.packetevents.util.LazyHolder;
import io.netty.channel.Channel;
import io.netty.channel.ChannelPipeline;
import lombok.RequiredArgsConstructor;
import net.fabricmc.api.EnvType;
import net.flectone.pulse.BuildConfig;
import net.flectone.pulse.FabricFlectonePulse;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.level.ServerPlayer;
import org.jspecify.annotations.Nullable;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class FabricPacketEventsAdapter implements MinecraftPacketEventsAdapter {

    private final FabricFlectonePulse fabricFlectonePulse;

    @Override
    public void load() {
        // configure packetevents api
        System.setProperty("packetevents.nbt.default-max-size", "2097152");

        ChainLoadData chainLoadData = new ChainLoadData();
        chainLoadData.setPlayerManagerIfNull(LazyHolder.threadSafe(() -> fabricFlectonePulse.get(FabricPacketEventsPlayerManager.class)));
        chainLoadData.setRegistryManagerIfNull(LazyHolder.simple(() -> new FabricRegistryManager(new ItemRegistry() {
            @Override
            public @Nullable ItemType getByName(String name) {
                return null;
            }

            @Override
            public @Nullable ItemType getById(int id) {
                return null;
            }
        })));
        FabricPacketEventsAPIManagerFactory.init(chainLoadData);

        FabricPacketEventsAPI fabricPacketEventsAPI = new FabricPacketEventsAPI(BuildConfig.PROJECT_MOD_ID, EnvType.SERVER);
        FabricPacketEventsAPI.setServerAPI(fabricPacketEventsAPI);
        PacketEvents.setAPI(fabricPacketEventsAPI);

        PacketEvents.getAPI().getSettings().reEncodeByDefault(false).checkForUpdates(false).debug(false);
        PacketEvents.getAPI().load();
    }

    public void preNewPlayerPlace(Channel channel, ServerPlayer player) {
        PacketEvents.getAPI().getInjector().setPlayer(channel, player);
    }

    public void onPlayerLogin(ServerPlayer player) {
        FabricInjectionUtil.fireUserLoginEvent(player);
    }

    public void postRespawn(ServerPlayer player) {
        PacketEvents.getAPI().getInjector().setPlayer(PacketEvents.getAPI().getPlayerManager().getChannel(player), player);
    }

    public void configureSerialization(ChannelPipeline pipeline, PacketFlow flow) {
        if (flow != PacketFlow.SERVERBOUND) {
            // if pipeline side doesn't match api side, don't inject into
            // this pipeline - it probably means this is the pipeline from
            // integrated server to minecraft client, which is currently unsupported
            PacketEvents.getAPI().getLogManager().debug("Skipped pipeline injection on " + flow);
            return;
        }

        FabricInjectionUtil.injectAtPipelineBuilder(pipeline, PacketSide.SERVER);
    }

    public void setupCompression(ChannelPipeline pipeline) {
        FabricInjectionUtil.reorderHandlers(pipeline, PacketSide.SERVER);
    }

}
