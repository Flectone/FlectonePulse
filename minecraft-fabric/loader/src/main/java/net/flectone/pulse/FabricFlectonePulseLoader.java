package net.flectone.pulse;

import lombok.Getter;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerLoginConnectionEvents;
import net.flectone.pulse.mixin.ServerLoginPacketListenerImplAccessor;
import net.flectone.pulse.util.constant.HookType;

import java.util.function.Supplier;

public class FabricFlectonePulseLoader implements DedicatedServerModInitializer, Supplier<FabricFlectonePulseLoader> {

    private static final String JAR_NAME = "flectonepulse-fabric.jarinjar";
    private static final String BOOTSTRAP_CLASS = "net.flectone.pulse.FabricFlectonePulse";

    @Getter
    private static LoaderBoostrap loaderBoostrap;

    @Override
    public void onInitializeServer() {
        JarInJarClassLoader loader = new JarInJarClassLoader(getClass().getClassLoader(), JAR_NAME);

        loaderBoostrap = loader.instantiatePlugin(BOOTSTRAP_CLASS, Supplier.class, this);
        loaderBoostrap.onLoad();
        loaderBoostrap.onEnable();

        ServerLoginConnectionEvents.QUERY_START.register((listener, _, _, _) ->
                loaderBoostrap.hook(HookType.ON_PLAYER_PRE_LOGIN, listener, ((ServerLoginPacketListenerImplAccessor) listener).getAuthenticatedProfile())
        );

        ServerLifecycleEvents.SERVER_STOPPING.register(_ -> loaderBoostrap.onDisable());
    }

    @Override
    public FabricFlectonePulseLoader get() {
        return this;
    }

}