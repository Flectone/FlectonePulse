package net.flectone.pulse;

import lombok.Getter;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.LoginPacketSender;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerLoginConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerLoginNetworking;
import net.flectone.pulse.constant.HookType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerLoginPacketListenerImpl;

import java.util.function.Supplier;

public class FabricFlectonePulseLoader implements DedicatedServerModInitializer, Supplier<FabricFlectonePulseLoader> {

    private static final String JAR_NAME = "flectonepulse-fabric.jarinjar";
    private static final String PLATFORM_JAR_NAME = "flectonepulse-fabric-platform.jarinjar";
    private static final String BOOTSTRAP_CLASS = "net.flectone.pulse.FabricFlectonePulse";

    @Getter
    private static LoaderBootstrap loaderBootstrap;

    @Override
    public void onInitializeServer() {
        JarInJarClassLoader loader = new JarInJarClassLoader(getClass().getClassLoader(), JAR_NAME, PLATFORM_JAR_NAME);

        loaderBootstrap = loader.instantiatePlugin(BOOTSTRAP_CLASS, Supplier.class, this);
        loaderBootstrap.onLoad();
        loaderBootstrap.onEnable();

        ServerLoginConnectionEvents.QUERY_START.register(new ServerLoginConnectionEvents.QueryStart() {
            @Override
            public void onLoginStart(ServerLoginPacketListenerImpl listener, MinecraftServer server, LoginPacketSender sender, ServerLoginNetworking.LoginSynchronizer synchronizer) {
                loaderBootstrap.hook(HookType.ON_PLAYER_PRE_LOGIN, listener);
            }

            public void onLoginStart(ServerLoginPacketListenerImpl listener, MinecraftServer server, PacketSender sender, ServerLoginNetworking.LoginSynchronizer synchronizer) {
                loaderBootstrap.hook(HookType.ON_PLAYER_PRE_LOGIN, listener);
            }
        });

        ServerLifecycleEvents.SERVER_STOPPING.register(_ -> loaderBootstrap.onDisable());
    }

    @Override
    public FabricFlectonePulseLoader get() {
        return this;
    }

}