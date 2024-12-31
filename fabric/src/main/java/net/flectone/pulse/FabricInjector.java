package net.flectone.pulse;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import com.google.inject.matcher.Matchers;
import com.google.inject.name.Names;
import net.fabricmc.loader.api.FabricLoader;
import net.flectone.pulse.annotation.Async;
import net.flectone.pulse.annotation.CheckModuleEnabled;
import net.flectone.pulse.annotation.Sync;
import net.flectone.pulse.database.Database;
import net.flectone.pulse.database.mysql.MySQLDatabase;
import net.flectone.pulse.database.sqlite.SQLiteDatabase;
import net.flectone.pulse.logger.FLogger;
import net.flectone.pulse.manager.FabricThreadManager;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.manager.ModuleManager;
import net.flectone.pulse.manager.ThreadManager;
import net.flectone.pulse.platform.FabricSender;
import net.flectone.pulse.platform.PlatformSender;
import net.flectone.pulse.util.InterceptorAsync;
import net.flectone.pulse.util.InterceptorCheckModuleEnabled;
import net.flectone.pulse.util.InterceptorSync;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;

@Singleton
public class FabricInjector extends AbstractModule {

    private final String modId;
    private final FlectonePulse flectonePulse;
    private final FLogger fLogger;

    public FabricInjector(String modId, FlectonePulse flectonePulse, FLogger fLogger) {
        this.modId = modId;
        this.flectonePulse = flectonePulse;
        this.fLogger = fLogger;
    }

    @Override
    protected void configure() {
        bind(String.class).annotatedWith(Names.named("modId")).toInstance(modId);

        bind(ThreadManager.class).to(FabricThreadManager.class);
//        bind(FPlayerManager.class).to(BukkitFPlayerManager.class);
//        bind(ListenerManager.class).to(BukkitListenerManager.class);
//        bind(InventoryManager.class).to(BukkitInventoryManager.class);
//        bind(PermissionManager.class).to(BukkitPermissionManager.class);
//        bind(CommandManager.class).to(BukkitCommandManager.class);
//        bind(ProxyListener.class).to(BukkitProxyListener.class);
//        bind(ProxyManager.class).to(BukkitProxyManager.class);
//        bind(ItemUtil.class).to(BukkitItemUtil.class);
//        bind(ServerUtil.class).to(BukkitServerUtil.class);
//        bind(MetricsUtil.class).to(BukkitMetricsUtil.class);
//        bind(FileUtil.class).to(BukkitFileUtil.class);

        // modules
//        bind(PlayersModule.class).to(BukkitPlayersModule.class);
//        bind(CommandModule.class).to(BukkitCommandModule.class);
//        bind(IntegrationModule.class).to(BukkitIntegrationModule.class);
//        bind(InteractionModule.class).to(BukkitInteractionModule.class);
//        bind(TeamModule.class).to(BukkkitTeamModule.class);
//        bind(ObjectiveModule.class).to(BukkitObjectiveModule.class);
//        bind(AnvilModule.class).to(BukkitAnvilModule.class);
//        bind(BookModule.class).to(BukkitBookModule.class);
//        bind(AfkModule.class).to(BukkitAfkModule.class);
//        bind(BubbleModule.class).to(BukkitBubbleModule.class);
//        bind(ChatModule.class).to(BukkitChatModule.class);
//        bind(SignModule.class).to(BukkitSignModule.class);
//        bind(IStreamModule.class).to(StreamModule.class);
//        bind(BubbleManager.class).to(BukkitBubbleManager.class);
//        bind(IMailModule.class).to(MailModule.class);

        bind(PlatformSender.class).to(FabricSender.class).asEagerSingleton();
        bind(FlectonePulse.class).toInstance(flectonePulse);
        bind(FlectonePulseAPI.class).asEagerSingleton();
        bind(FLogger.class).toInstance(fLogger);

        InterceptorSync interceptorSync = new InterceptorSync();
        requestInjection(interceptorSync);

        InterceptorAsync interceptorAsync = new InterceptorAsync();
        requestInjection(interceptorAsync);

        InterceptorCheckModuleEnabled interceptorCheckModuleEnabled = new InterceptorCheckModuleEnabled();
        requestInjection(interceptorCheckModuleEnabled);

        bindInterceptor(Matchers.any(),
                Matchers.annotatedWith(Sync.class)
                        .or(Matchers.annotatedWith(Async.class))
                        .or(Matchers.annotatedWith(CheckModuleEnabled.class))
                        .or(Matchers.annotatedWith(Sync.class)),
                interceptorAsync,
                interceptorSync,
                interceptorCheckModuleEnabled
        );

        bind(MiniMessage.class).toInstance(MiniMessage.builder().tags(TagResolver.builder().build()).build());

        Path pluginPath = Paths.get(FabricLoader.getInstance().getConfigDir().toString(), "flectonepulse");

        bind(Path.class).annotatedWith(Names.named("pluginPath")).toInstance(pluginPath);

        FileManager fileManager = new FileManager(pluginPath);
        fileManager.reload();

        bind(FileManager.class).toInstance(fileManager);

        if (fileManager.getConfig().getPlugin().getDatabase() == Database.Type.MYSQL) {
            bind(InputStream.class).annotatedWith(Names.named("SQLFile")).toInstance(Thread.currentThread().getContextClassLoader().getResourceAsStream("sqls/mysql_flectonepulse.sql"));
            bind(Database.class).to(MySQLDatabase.class);
        } else {
            bind(InputStream.class).annotatedWith(Names.named("SQLFile")).toInstance(Thread.currentThread().getContextClassLoader().getResourceAsStream("sqls/sqlite_flectonepulse.sql"));
            bind(Database.class).to(SQLiteDatabase.class);
        }

        bind(ModuleManager.class).asEagerSingleton();
    }
}
