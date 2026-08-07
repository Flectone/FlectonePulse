package net.flectone.pulse;

import com.google.inject.Singleton;
import java.nio.file.Path;
import net.flectone.pulse.module.integration.MinecraftIntegrationModule;
import net.flectone.pulse.module.integration.NeoForgeIntegrationModule;
import net.flectone.pulse.platform.adapter.*;
import net.flectone.pulse.platform.registry.*;
import net.flectone.pulse.platform.regitry.ProxyRegistryImpl;
import net.flectone.pulse.platform.sender.MinecraftMessageSender;
import net.flectone.pulse.platform.sender.NeoForgeMessageSender;
import net.flectone.pulse.processing.resolver.LibraryResolver;
import net.flectone.pulse.processing.resolver.ReflectionResolver;
import net.flectone.pulse.processing.serializer.MinecraftComponentSerializer;
import net.flectone.pulse.processing.serializer.NeoForgeComponentSerializer;
import net.flectone.pulse.util.checker.NeoForgePermissionChecker;
import net.flectone.pulse.util.checker.PermissionChecker;
import net.flectone.pulse.util.logging.FLogger;

@Singleton
public class NeoForgeInjector extends MinecraftPlatformInjector {

    private final NeoForgeFlectonePulse flectonePulse;

    public NeoForgeInjector(NeoForgeFlectonePulse flectonePulse,
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
        bind(NeoForgeFlectonePulse.class).toInstance(flectonePulse);

        // adapters
        bind(PlatformPlayerAdapter.class).to(NeoForgePlayerAdapter.class);
        bind(PlatformServerAdapter.class).to(NeoForgeServerAdapter.class);

        // registries
        bind(PermissionRegistry.class).to(NeoForgePermissionRegistry.class);
        bind(ProxyRegistryImpl.class).to(NeoForgeProxyRegistry.class);
        bind(MinecraftListenerRegistry.class).to(NeoForgeListenerRegistry.class);
        bind(CommandRegistry.class).to(NeoForgeCommandRegistry.class);

        // checkers and utilities
        bind(PermissionChecker.class).to(NeoForgePermissionChecker.class);

        bind(MinecraftIntegrationModule.class).to(NeoForgeIntegrationModule.class);

        // sender
        bind(MinecraftMessageSender.class).to(NeoForgeMessageSender.class);

        // others
        bind(MinecraftComponentSerializer.class).to(NeoForgeComponentSerializer.class);
    }
}
