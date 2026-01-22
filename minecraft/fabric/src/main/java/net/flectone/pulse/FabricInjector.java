package net.flectone.pulse;

import com.google.inject.Singleton;
import net.fabricmc.loader.api.FabricLoader;
import net.flectone.pulse.execution.scheduler.FabricTaskScheduler;
import net.flectone.pulse.execution.scheduler.TaskScheduler;
import net.flectone.pulse.module.integration.FabricIntegrationModule;
import net.flectone.pulse.module.integration.MinecraftIntegrationModule;
import net.flectone.pulse.platform.adapter.FabricPlayerAdapter;
import net.flectone.pulse.platform.adapter.FabricServerAdapter;
import net.flectone.pulse.platform.adapter.PlatformPlayerAdapter;
import net.flectone.pulse.platform.adapter.PlatformServerAdapter;
import net.flectone.pulse.platform.registry.*;
import net.flectone.pulse.processing.resolver.LibraryResolver;
import net.flectone.pulse.processing.resolver.ReflectionResolver;
import net.flectone.pulse.util.checker.FabricPermissionChecker;
import net.flectone.pulse.util.checker.PermissionChecker;
import net.flectone.pulse.util.logging.FLogger;

@Singleton
public class FabricInjector extends MinecraftPlatformInjector {

    private final FabricFlectonePulse flectonePulse;

    public FabricInjector(FabricFlectonePulse flectonePulse,
                          LibraryResolver libraryResolver,
                          FLogger fLogger) {
        super(FabricLoader.getInstance().getConfigDir().resolve(BuildConfig.PROJECT_MOD_ID), libraryResolver, fLogger);

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
        bind(ProxyRegistry.class).to(FabricProxyRegistry.class);
        bind(MinecraftListenerRegistry.class).to(FabricListenerRegistry.class);
        bind(MinecraftCommandRegistry.class).to(FabricCommandRegistry.class);

        // checkers and utilities
        bind(PermissionChecker.class).to(FabricPermissionChecker.class);
        bind(TaskScheduler.class).to(FabricTaskScheduler.class);

        bind(MinecraftIntegrationModule.class).to(FabricIntegrationModule.class);

// TODO
//        bind(AnvilModule.class).to(FabricAnvilModule.class);
//        bind(BookModule.class).to(BukkitBookModule.class);
//        bind(AfkModule.class).to(BukkitAfkModule.class);
//        bind(BubbleModule.class).to(BukkitBubbleModule.class);
//        bind(SignModule.class).to(BukkitSignModule.class);
//        bind(SpyModule.class).to(BukkitSpyModule.class);
    }
}
