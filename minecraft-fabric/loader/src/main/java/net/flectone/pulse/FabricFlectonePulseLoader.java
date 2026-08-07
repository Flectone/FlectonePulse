package net.flectone.pulse;

import lombok.Getter;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerLoginConnectionEvents;
import net.flectone.pulse.constant.HookType;
import net.flectone.pulse.mixin.ServerLoginPacketListenerImplAccessor;

import java.util.function.Supplier;

public class FabricFlectonePulseLoader implements DedicatedServerModInitializer, Supplier<FabricFlectonePulseLoader> {

    private static final String JAR_NAME = "flectonepulse-fabric.jarinjar";
    private static final String BOOTSTRAP_CLASS = "net.flectone.pulse.FabricFlectonePulse";

    @Getter
    private static LoaderBootstrap loaderBootstrap;

    @Override
    public void onInitializeServer() {
        JarInJarClassLoader loader = new JarInJarClassLoader(getClass().getClassLoader(), JAR_NAME);

        loaderBootstrap = loader.instantiatePlugin(BOOTSTRAP_CLASS, Supplier.class, this);
        loaderBootstrap.onLoad();
        loaderBootstrap.onEnable();

        ServerLoginConnectionEvents.QUERY_START.register((listener, _, _, _) ->
                loaderBootstrap.hook(HookType.ON_PLAYER_PRE_LOGIN, listener, ((ServerLoginPacketListenerImplAccessor) listener).getAuthenticatedProfile())
        );

        ServerLifecycleEvents.SERVER_STOPPING.register(_ -> loaderBootstrap.onDisable());
    }

    @Override
    public FabricFlectonePulseLoader get() {
        return this;
    }

}