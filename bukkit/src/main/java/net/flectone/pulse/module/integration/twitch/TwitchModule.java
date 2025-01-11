package net.flectone.pulse.module.integration.twitch;

import com.alessiodp.libby.Library;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import lombok.SneakyThrows;
import net.flectone.pulse.BuildConfig;
import net.flectone.pulse.file.Integration;
import net.flectone.pulse.file.Permission;
import net.flectone.pulse.logger.FLogger;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.model.FEntity;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.module.AbstractModule;
import net.flectone.pulse.platform.LibraryResolver;
import net.flectone.pulse.util.MessageTag;

import java.util.function.UnaryOperator;

@Singleton
public class TwitchModule extends AbstractModule {

    private final Integration.Twitch integration;
    private final Permission.Integration.Twitch permission;

    private final LibraryResolver libraryResolver;
    private final Injector injector;

    @Inject
    public TwitchModule(FileManager fileManager,
                        LibraryResolver libraryResolver,
                        Injector injector) {
        this.libraryResolver = libraryResolver;
        this.injector = injector;

        integration = fileManager.getIntegration().getTwitch();
        permission = fileManager.getPermission().getIntegration().getTwitch();

        addPredicate(fEntity -> fEntity instanceof FPlayer fPlayer && !fPlayer.is(FPlayer.Setting.TWITCH));
    }

    @Inject
    private FLogger fLogger;

    @Override
    public void reload() {
        registerModulePermission(permission);

        libraryResolver.loadLibrary(Library.builder()
                .groupId("com{}github{}twitch4j")
                .artifactId("twitch4j")
                .version(BuildConfig.TWITCH4J_VERSION)
                .resolveTransitiveDependencies(true)
                .url("https://mvnrepository.com/artifact/com.github.twitch4j/twitch4j")
                .build()
        );

        disconnect();

        injector.getInstance(TwitchIntegration.class).hook();
    }

    @Override
    public boolean isConfigEnable() {
        return integration.isEnable();
    }

    public void sendMessage(FEntity sender, MessageTag messageTag, UnaryOperator<String> twitchString) {
        if (checkModulePredicates(sender)) return;

        injector.getInstance(TwitchIntegration.class).sendMessage(sender, messageTag, twitchString);
    }

    @SneakyThrows
    public void disconnect() {
        if (!isEnable()) return;

        injector.getInstance(TwitchIntegration.class).disconnect();
    }
}
