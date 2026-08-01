package net.flectone.pulse;

import com.google.inject.Singleton;
import net.flectone.pulse.module.integration.MinecraftIntegrationModule;
import net.flectone.pulse.module.integration.NeoForgeIntegrationModule;
import net.flectone.pulse.platform.adapter.NeoForgePlayerAdapter;
import net.flectone.pulse.platform.adapter.NeoForgeServerAdapter;
import net.flectone.pulse.platform.adapter.PlatformPlayerAdapter;
import net.flectone.pulse.platform.adapter.PlatformServerAdapter;
import net.flectone.pulse.platform.registry.*;
import net.flectone.pulse.processing.resolver.LibraryResolver;
import net.flectone.pulse.processing.resolver.ReflectionResolver;
import net.flectone.pulse.util.checker.NeoForgePermissionChecker;
import net.flectone.pulse.util.checker.PermissionChecker;
import net.flectone.pulse.util.logging.FLogger;

import java.nio.file.Path;

@Singleton
public class NeoForgeInjector extends MinecraftPlatformInjector {

    private final NeoForgeFlectonePulse flectonePulse;

    public NeoForgeInjector(NeoForgeFlectonePulse flectonePulse,
                            LibraryResolver libraryResolver,
                            FLogger fLogger) {
        super(Path.of("config", BuildConfig.PROJECT_MOD_ID), libraryResolver, fLogger);

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
        bind(ProxyRegistry.class).to(NeoForgeProxyRegistry.class);
        bind(MinecraftListenerRegistry.class).to(NeoForgeListenerRegistry.class);
        bind(CommandRegistry.class).to(NeoForgeCommandRegistry.class);

        // checkers and utilities
        bind(PermissionChecker.class).to(NeoForgePermissionChecker.class);

        bind(MinecraftIntegrationModule.class).to(NeoForgeIntegrationModule.class);
    }
}
