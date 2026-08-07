package net.flectone.pulse;

import com.google.inject.Singleton;
import net.fabricmc.loader.api.FabricLoader;
import net.flectone.pulse.module.integration.FabricIntegrationModule;
import net.flectone.pulse.module.integration.MinecraftIntegrationModule;
import net.flectone.pulse.platform.adapter.*;
import net.flectone.pulse.platform.registry.*;
import net.flectone.pulse.platform.regitry.ProxyRegistryImpl;
import net.flectone.pulse.platform.sender.FabricMessageSender;
import net.flectone.pulse.platform.sender.MinecraftMessageSender;
import net.flectone.pulse.processing.resolver.LibraryResolver;
import net.flectone.pulse.processing.resolver.ReflectionResolver;
import net.flectone.pulse.processing.serializer.FabricComponentSerializer;
import net.flectone.pulse.processing.serializer.MinecraftComponentSerializer;
import net.flectone.pulse.util.checker.FabricPermissionChecker;
import net.flectone.pulse.util.checker.PermissionChecker;
import net.flectone.pulse.util.logging.FLogger;

@Singleton
public class FabricInjector extends MinecraftPlatformInjector {

    private final FabricFlectonePulse flectonePulse;

    public FabricInjector(FabricFlectonePulse flectonePulse,
                          MinecraftPacketEventsAdapter minecraftPacketEventsAdapter,
                          LibraryResolver libraryResolver,
                          FLogger fLogger) {
        super(FabricLoader.getInstance().getConfigDir().resolve(BuildConfig.PROJECT_MOD_ID), minecraftPacketEventsAdapter, libraryResolver, fLogger);

        this.flectonePulse = flectonePulse;
    }

    @Override
    public void setupPlatform(ReflectionResolver reflectionResolver) {
        super.setupPlatform(reflectionResolver);

        bind(FlectonePulse.class).toInstance(flectonePulse);
        bind(FabricFlectonePulse.class).toInstance(flectonePulse);

        // adapters
        bind(PlatformPlayerAdapter.class).to(FabricPlayerAdapter.class);
        bind(PlatformServerAdapter.class).to(FabricServerAdapter.class);

        // registries
        bind(PermissionRegistry.class).to(FabricPermissionRegistry.class);
        bind(ProxyRegistryImpl.class).to(FabricProxyRegistry.class);
        bind(MinecraftListenerRegistry.class).to(FabricListenerRegistry.class);
        bind(CommandRegistry.class).to(FabricCommandRegistry.class);

        // checkers and utilities
        bind(PermissionChecker.class).to(FabricPermissionChecker.class);

        bind(MinecraftIntegrationModule.class).to(FabricIntegrationModule.class);

        // sender
        bind(MinecraftMessageSender.class).to(FabricMessageSender.class);

        // others
        bind(MinecraftComponentSerializer.class).to(FabricComponentSerializer.class);
    }
}
