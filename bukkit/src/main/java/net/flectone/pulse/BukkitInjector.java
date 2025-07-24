package net.flectone.pulse;

import com.github.Anon8281.universalScheduler.UniversalScheduler;
import com.google.gson.Gson;
import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import com.google.inject.matcher.Matchers;
import com.google.inject.name.Names;
import net.flectone.pulse.adapter.BukkitPlayerAdapter;
import net.flectone.pulse.adapter.BukkitServerAdapter;
import net.flectone.pulse.adapter.PlatformPlayerAdapter;
import net.flectone.pulse.adapter.PlatformServerAdapter;
import net.flectone.pulse.annotation.Async;
import net.flectone.pulse.annotation.Sync;
import net.flectone.pulse.checker.BukkitPermissionChecker;
import net.flectone.pulse.checker.PermissionChecker;
import net.flectone.pulse.converter.LegacyMiniConvertor;
import net.flectone.pulse.database.Database;
import net.flectone.pulse.module.command.spy.BukkitSpyModule;
import net.flectone.pulse.module.command.spy.SpyModule;
import net.flectone.pulse.module.integration.BukkitIntegrationModule;
import net.flectone.pulse.module.integration.IntegrationModule;
import net.flectone.pulse.module.message.afk.AfkModule;
import net.flectone.pulse.module.message.afk.BukkitAfkModule;
import net.flectone.pulse.module.message.anvil.AnvilModule;
import net.flectone.pulse.module.message.anvil.BukkitAnvilModule;
import net.flectone.pulse.module.message.book.BookModule;
import net.flectone.pulse.module.message.book.BukkitBookModule;
import net.flectone.pulse.module.message.bubble.BubbleModule;
import net.flectone.pulse.module.message.bubble.BukkitBubbleModule;
import net.flectone.pulse.module.message.chat.BukkitChatModule;
import net.flectone.pulse.module.message.chat.ChatModule;
import net.flectone.pulse.module.message.join.BukkitJoinModule;
import net.flectone.pulse.module.message.join.JoinModule;
import net.flectone.pulse.module.message.quit.BukkitQuitModule;
import net.flectone.pulse.module.message.quit.QuitModule;
import net.flectone.pulse.module.message.sign.BukkitSignModule;
import net.flectone.pulse.module.message.sign.SignModule;
import net.flectone.pulse.provider.*;
import net.flectone.pulse.registry.*;
import net.flectone.pulse.resolver.FileResolver;
import net.flectone.pulse.resolver.LibraryResolver;
import net.flectone.pulse.scheduler.BukkitTaskScheduler;
import net.flectone.pulse.scheduler.TaskScheduler;
import net.flectone.pulse.sender.BukkitMessageSender;
import net.flectone.pulse.sender.MessageSender;
import net.flectone.pulse.util.interceptor.AsyncInterceptor;
import net.flectone.pulse.util.interceptor.SyncInterceptor;
import net.flectone.pulse.util.logging.FLogger;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.nio.file.Path;

@Singleton
public class BukkitInjector extends AbstractModule {

    private final BukkitFlectonePulse instance;
    private final Plugin plugin;
    private final LibraryResolver libraryResolver;
    private final FLogger fLogger;
    private final PacketProvider packetProvider;

    public BukkitInjector(BukkitFlectonePulse instance,
                          Plugin plugin,
                          LibraryResolver libraryResolver,
                          FLogger fLogger) {
        this.instance = instance;
        this.plugin = plugin;
        this.libraryResolver = libraryResolver;
        this.fLogger = fLogger;
        this.packetProvider = new PacketProvider();
    }

    @Override
    protected void configure() {
        bind(PacketProvider.class).toInstance(packetProvider);

        // Bind project path
        Path projectPath = plugin.getDataFolder().toPath();
        bind(Path.class).annotatedWith(Names.named("projectPath")).toInstance(projectPath);

        // Initialize and bind FileManager
        FileResolver fileResolver = new FileResolver(projectPath, fLogger);
        fileResolver.reload();

        bind(FileResolver.class).toInstance(fileResolver);
        bind(Database.class).asEagerSingleton();

        // Adapters
        bind(PlatformPlayerAdapter.class).to(BukkitPlayerAdapter.class);
        bind(PlatformServerAdapter.class).to(BukkitServerAdapter.class);

        // Providers
        try {
            Class.forName("org.bukkit.attribute.Attribute");
            bind(AttributesProvider.class).to(ModernAttributesProvider.class);
        } catch (ClassNotFoundException e) {
            bind(AttributesProvider.class).to(LegacyAttributesProvider.class);
        }

        try {
            Player.class.getMethod("getPassengers");
            bind(PassengersProvider.class).to(ModernPassengersProvider.class);
        } catch (NoSuchMethodException e) {
            bind(PassengersProvider.class).to(LegacyPassengersProvider.class);
        }

        // Registries
        bind(PermissionRegistry.class).to(BukkitPermissionRegistry.class);
        bind(ListenerRegistry.class).to(BukkitListenerRegistry.class);
        bind(ProxyRegistry.class).to(BukkitProxyRegistry.class);

        try {
            Class.forName("com.mojang.brigadier.arguments.ArgumentType");
            bind(CommandRegistry.class).to(ModernBukkitCommandRegistry.class);
        } catch (ClassNotFoundException e) {
            bind(CommandRegistry.class).to(LegacyBukkitCommandRegistry.class);
        }

        // Checkers and utilities
        bind(PermissionChecker.class).to(BukkitPermissionChecker.class);
        bind(TaskScheduler.class).to(BukkitTaskScheduler.class);
        bind(MessageSender.class).to(BukkitMessageSender.class);

        // Modules
        bind(IntegrationModule.class).to(BukkitIntegrationModule.class);
        bind(AnvilModule.class).to(BukkitAnvilModule.class);
        bind(BookModule.class).to(BukkitBookModule.class);
        bind(AfkModule.class).to(BukkitAfkModule.class);
        bind(BubbleModule.class).to(BukkitBubbleModule.class);
        bind(ChatModule.class).to(BukkitChatModule.class);
        bind(SignModule.class).to(BukkitSignModule.class);
        bind(SpyModule.class).to(BukkitSpyModule.class);
        bind(JoinModule.class).to(BukkitJoinModule.class);
        bind(QuitModule.class).to(BukkitQuitModule.class);

        // Libraries and serialization
        bind(LibraryResolver.class).toInstance(libraryResolver);
        bind(Gson.class).toInstance(GsonComponentSerializer.gson().serializer());
        bind(LegacyMiniConvertor.class).asEagerSingleton();

        // Core bindings
        bind(FlectonePulse.class).toInstance(instance);
        bind(BukkitFlectonePulse.class).toInstance(instance);
        bind(FlectonePulseAPI.class).asEagerSingleton();
        bind(Plugin.class).toInstance(plugin);
        bind(FLogger.class).toInstance(fLogger);

        // Scheduler
        bind(com.github.Anon8281.universalScheduler.scheduling.schedulers.TaskScheduler.class)
                .toInstance(UniversalScheduler.getScheduler(plugin));

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
