package net.flectone.pulse;

import com.google.inject.Singleton;
import net.flectone.pulse.checker.PermissionCheckerImpl;
import net.flectone.pulse.checker.SpongePermissionChecker;
import net.flectone.pulse.logging.FLogger;
import net.flectone.pulse.module.integration.MinecraftIntegrationModule;
import net.flectone.pulse.module.integration.SpongeIntegrationModule;
import net.flectone.pulse.platform.adapter.*;
import net.flectone.pulse.platform.registry.*;
import net.flectone.pulse.platform.sender.MinecraftMessageSender;
import net.flectone.pulse.platform.sender.SpongeMessageSender;
import net.flectone.pulse.resolver.LibraryResolver;
import net.flectone.pulse.resolver.ReflectionResolver;
import net.flectone.pulse.serializer.MinecraftComponentSerializer;
import net.flectone.pulse.serializer.SpongeComponentSerializer;

import java.nio.file.Path;

@Singleton
public class SpongeInjector extends MinecraftPlatformInjector {

    private final SpongeFlectonePulse flectonePulse;

    public SpongeInjector(SpongeFlectonePulse flectonePulse,
                          MinecraftPacketEventsAdapter minecraftPacketEventsAdapter,
                          LibraryResolver libraryResolver,
                          FLogger fLogger) {
        super(Path.of("config", BuildConfig.PROJECT_MOD_ID), minecraftPacketEventsAdapter, libraryResolver, fLogger);

        this.flectonePulse = flectonePulse;
    }

    @Override
    public void setupPlatform(ReflectionResolver reflectionResolver) {
        super.setupPlatform(reflectionResolver);

        bind(FlectonePulse.class).toInstance(flectonePulse);
        bind(SpongeFlectonePulse.class).toInstance(flectonePulse);

        // adapters
        bind(PlatformPlayerAdapter.class).to(SpongePlayerAdapter.class);
        bind(PlatformServerAdapter.class).to(SpongeServerAdapter.class);

        // registries
        bind(PermissionRegistry.class).to(SpongePermissionRegistry.class);
        bind(ProxyRegistryImpl.class).to(SpongeProxyRegistry.class);
        bind(MinecraftListenerRegistry.class).to(SpongeListenerRegistry.class);
        bind(CommandRegistry.class).to(SpongeCommandRegistry.class);

        // checkers and utilities
        bind(PermissionCheckerImpl.class).to(SpongePermissionChecker.class);

        bind(MinecraftIntegrationModule.class).to(SpongeIntegrationModule.class);

        // sender
        bind(MinecraftMessageSender.class).to(SpongeMessageSender.class);

        // others
        bind(MinecraftComponentSerializer.class).to(SpongeComponentSerializer.class);
    }
}
