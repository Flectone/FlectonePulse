package net.flectone.pulse.module.integration.discord;

import com.alessiodp.libby.Library;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import lombok.SneakyThrows;
import net.flectone.pulse.BuildConfig;
import net.flectone.pulse.config.Integration;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.util.logging.FLogger;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.model.FEntity;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.module.AbstractModule;
import net.flectone.pulse.platform.LibraryResolver;
import net.flectone.pulse.util.MessageTag;

import java.util.function.UnaryOperator;

@Singleton
public class DiscordModule extends AbstractModule {

    private final Integration.Discord integration;
    private final Permission.Integration.Discord permission;

    private final LibraryResolver libraryResolver;
    private final Injector injector;
    private final FLogger fLogger;

    @Inject
    public DiscordModule(FileManager fileManager,
                         LibraryResolver libraryResolver,
                         Injector injector,
                         FLogger fLogger) {
        this.libraryResolver = libraryResolver;
        this.injector = injector;
        this.fLogger = fLogger;

        integration = fileManager.getIntegration().getDiscord();
        permission = fileManager.getPermission().getIntegration().getDiscord();

        addPredicate(fEntity -> fEntity instanceof FPlayer fPlayer && !fPlayer.isSetting(FPlayer.Setting.DISCORD));
    }

    @Override
    public void reload() {
        registerModulePermission(permission);

        loadLibraries();

        DiscordIntegration discordIntegration = injector.getInstance(DiscordIntegration.class);
        discordIntegration.setEnable(isEnable());

        try {
            discordIntegration.reload();
        } catch (Exception e) {
            fLogger.warning(e);
        }
    }

    private void loadLibraries() {
        libraryResolver.loadLibrary(Library.builder()
                .groupId("com{}discord4j")
                .artifactId("discord4j-core")
                .version(BuildConfig.DISCORD4J_VERSION)
                .resolveTransitiveDependencies(true)
                .build()
        );

        libraryResolver.loadLibrary(Library.builder()
                .groupId("io{}netty")
                .artifactId("netty-resolver-dns")
                .version("5.0.0.Alpha2")
                .resolveTransitiveDependencies(true)
                .build()
        );

        libraryResolver.loadLibrary(Library.builder()
                .groupId("com{}discord4j")
                .artifactId("discord4j-common")
                .version("3.3.0-RC1")
                .resolveTransitiveDependencies(true)
                .build()
        );
    }

    @Override
    public boolean isConfigEnable() {
        return integration.isEnable();
    }

    public void sendMessage(FEntity sender, MessageTag messageTag, UnaryOperator<String> discordString) {
        if (checkModulePredicates(sender)) return;

        injector.getInstance(DiscordIntegration.class).sendMessage(sender, messageTag, discordString);
    }

    @SneakyThrows
    public void disconnect() {
        if (!isEnable()) return;

        injector.getInstance(DiscordIntegration.class).disconnect();
    }
}
