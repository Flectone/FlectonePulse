package net.flectone.pulse;

import com.google.gson.Gson;
import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import com.google.inject.matcher.Matchers;
import com.google.inject.name.Names;
import io.github.retrooper.packetevents.adventure.serializer.gson.GsonComponentSerializer;
import net.fabricmc.loader.api.FabricLoader;
import net.flectone.pulse.adapter.FabricPlayerAdapter;
import net.flectone.pulse.adapter.FabricServerAdapter;
import net.flectone.pulse.adapter.PlatformPlayerAdapter;
import net.flectone.pulse.adapter.PlatformServerAdapter;
import net.flectone.pulse.annotation.Async;
import net.flectone.pulse.annotation.Sync;
import net.flectone.pulse.checker.FabricPermissionChecker;
import net.flectone.pulse.checker.PermissionChecker;
import net.flectone.pulse.converter.LegacyMiniConvertor;
import net.flectone.pulse.database.Database;
import net.flectone.pulse.module.integration.FabricIntegrationModule;
import net.flectone.pulse.module.integration.IntegrationModule;
import net.flectone.pulse.module.message.anvil.AnvilModule;
import net.flectone.pulse.module.message.anvil.FabricAnvilModule;
import net.flectone.pulse.provider.PacketProvider;
import net.flectone.pulse.registry.*;
import net.flectone.pulse.resolver.FileResolver;
import net.flectone.pulse.resolver.LibraryResolver;
import net.flectone.pulse.scheduler.FabricTaskScheduler;
import net.flectone.pulse.scheduler.TaskScheduler;
import net.flectone.pulse.util.interceptor.AsyncInterceptor;
import net.flectone.pulse.util.interceptor.SyncInterceptor;
import net.flectone.pulse.util.logging.FLogger;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

import java.nio.file.Path;

@Singleton
public class FabricInjector extends AbstractModule {

    private final FlectonePulse flectonePulse;
    private final LibraryResolver libraryResolver;
    private final FLogger fLogger;
    private final PacketProvider packetProvider;

    public FabricInjector(FlectonePulse flectonePulse,
                          LibraryResolver libraryResolver,
                          FLogger fLogger) {
        this.flectonePulse = flectonePulse;
        this.libraryResolver = libraryResolver;
        this.fLogger = fLogger;
        this.packetProvider = new PacketProvider();
    }

    @Override
    protected void configure() {
        bind(PacketProvider.class).toInstance(packetProvider);

        // Bind project path
        Path projectPath = FabricLoader.getInstance().getConfigDir().resolve(BuildConfig.PROJECT_MOD_ID);
        bind(Path.class).annotatedWith(Names.named("projectPath")).toInstance(projectPath);

        // Initialize and bind FileManager
        FileResolver fileResolver = new FileResolver(projectPath, fLogger);
        fileResolver.reload();

        bind(FileResolver.class).toInstance(fileResolver);
        bind(Database.class).asEagerSingleton();

        // Adapters
        bind(PlatformPlayerAdapter.class).to(FabricPlayerAdapter.class);
        bind(PlatformServerAdapter.class).to(FabricServerAdapter.class);

        // Registries
        bind(PermissionRegistry.class).to(FabricPermissionRegistry.class);
//        bind(ProxyRegistry.class).to(BukkitProxyRegistry.class);
        bind(ListenerRegistry.class).to(FabricListenerRegistry.class);
        bind(CommandRegistry.class).to(FabricCommandRegistry.class);

        // Checkers and utilities
        bind(PermissionChecker.class).to(FabricPermissionChecker.class);
        bind(TaskScheduler.class).to(FabricTaskScheduler.class);

        // Modules
        bindModules();

        // Libraries and serialization
        bind(LibraryResolver.class).toInstance(libraryResolver);
        bind(Gson.class).toInstance(GsonComponentSerializer.gson().serializer());
        bind(LegacyMiniConvertor.class).asEagerSingleton();

        // Core bindings
        bind(FlectonePulse.class).toInstance(flectonePulse);
        bind(FlectonePulseAPI.class).asEagerSingleton();
        bind(FLogger.class).toInstance(fLogger);

        // Interceptors
        setupInterceptors();

        // MiniMessage
        bind(MiniMessage.class).toInstance(MiniMessage.builder().tags(TagResolver.builder().build()).build());

//        try {
//            Package[] packs = Package.getPackages();
//
//            Arrays.stream(packs)
//                    .map(Package::getName)
//                    .filter(string -> string.contains("net.flectone.pulse.library"))
//                    .sorted()
//                    .forEach(fLogger::warning);
//
//        } catch (Exception e) {
//            fLogger.warning(e);
//        }
    }

    private void bindModules() {
        bind(IntegrationModule.class).to(FabricIntegrationModule.class);
        bind(AnvilModule.class).to(FabricAnvilModule.class);
//        bind(BookModule.class).to(BukkitBookModule.class);
//        bind(AfkModule.class).to(BukkitAfkModule.class);
//        bind(BubbleModule.class).to(BukkitBubbleModule.class);
//        bind(SignModule.class).to(BukkitSignModule.class);
//        bind(SpyModule.class).to(BukkitSpyModule.class);
    }

    private void setupInterceptors() {
        SyncInterceptor syncInterceptor = new SyncInterceptor();
        requestInjection(syncInterceptor);

        AsyncInterceptor asyncInterceptor = new AsyncInterceptor();
        requestInjection(asyncInterceptor);

        bindInterceptor(Matchers.any(),
                Matchers.annotatedWith(Sync.class).or(Matchers.annotatedWith(Async.class)),
                asyncInterceptor,
                syncInterceptor
        );
    }


}
