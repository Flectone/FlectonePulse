package net.flectone.pulse.module.integration.twitch;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import net.flectone.pulse.BuildConfig;
import net.flectone.pulse.FlectonePulsePlugin;
import net.flectone.pulse.file.Integration;
import net.flectone.pulse.file.Permission;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.model.FEntity;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.module.AbstractModule;
import net.flectone.pulse.platform.PlatformDependency;
import net.flectone.pulse.util.MessageTag;

import java.util.function.UnaryOperator;

@Singleton
public class TwitchModule extends AbstractModule {

    private final Integration.Twitch integration;
    private final Permission.Integration.Twitch permission;

    private final FlectonePulsePlugin flectonePulsePlugin;
    private final Injector injector;

    private boolean isLoaded;

    @Inject
    public TwitchModule(FileManager fileManager,
                        FlectonePulsePlugin flectonePulsePlugin,
                        Injector injector) {
        this.flectonePulsePlugin = flectonePulsePlugin;
        this.injector = injector;

        integration = fileManager.getIntegration().getTwitch();
        permission = fileManager.getPermission().getIntegration().getTwitch();

        addPredicate(fEntity -> fEntity instanceof FPlayer fPlayer && !fPlayer.is(FPlayer.Setting.TWITCH));
    }

    @Override
    public void reload() {
        registerModulePermission(permission);

        disconnect();

        if (!isEnable()) return;

        loadLibraries();

        injector.getInstance(TwitchIntegration.class).hook();
    }

    @Override
    public boolean isConfigEnable() {
        return integration.isEnable();
    }

    public void loadLibraries() {
        if (isLoaded) return;
        PlatformDependency platformDependency = flectonePulsePlugin.getDependencyResolver();
        platformDependency.getLibraryManager().loadLibrary(platformDependency.buildLibrary(
                "com.github.philippheuer.credentialmanager", "credentialmanager", "0.3.1", true, null, null)
                .build()
        );
        platformDependency.getLibraryManager().loadLibrary(platformDependency.buildLibrary(
                "com.github.philippheuer.events4j", "events4j-core", "0.12.2", true, null, null)
                .build()
        );
        platformDependency.getLibraryManager().loadLibrary(platformDependency.buildLibrary(
                "com.github.philippheuer.events4j", "events4j-handler-simple", "0.12.2", true, null, null)
                .build()
        );
        platformDependency.getLibraryManager().loadLibrary(platformDependency.buildLibrary(
                "com.github.twitch4j", "twitch4j", BuildConfig.TWITCH4J_VERSION, false, null, null)
                .build()
        );
        platformDependency.getLibraryManager().loadLibrary(platformDependency.buildLibrary(
                "com.github.twitch4j", "twitch4j-chat", BuildConfig.TWITCH4J_VERSION, false, null, null)
                .build()
        );
        platformDependency.getLibraryManager().loadLibrary(platformDependency.buildLibrary(
                "com.github.twitch4j", "twitch4j-auth", BuildConfig.TWITCH4J_VERSION, false, null, null)
                .build()
        );
        platformDependency.getLibraryManager().loadLibrary(platformDependency.buildLibrary(
                "com.github.twitch4j", "twitch4j-common", BuildConfig.TWITCH4J_VERSION, false, null, null)
                .build()
        );
        platformDependency.getLibraryManager().loadLibrary(platformDependency.buildLibrary(
                "com.github.twitch4j", "twitch4j-client-websocket", BuildConfig.TWITCH4J_VERSION, false, null, null)
                .build()
        );
        platformDependency.getLibraryManager().loadLibrary(platformDependency.buildLibrary(
                "com.github.twitch4j", "twitch4j-util", BuildConfig.TWITCH4J_VERSION, false, null, null)
                .build()
        );
        platformDependency.getLibraryManager().loadLibrary(platformDependency.buildLibrary(
                "com.github.twitch4j", "twitch4j-eventsub-common", BuildConfig.TWITCH4J_VERSION, false, null, null)
                .build()
        );
        platformDependency.getLibraryManager().loadLibrary(platformDependency.buildLibrary(
                "com.github.twitch4j", "twitch4j-eventsub-websocket", BuildConfig.TWITCH4J_VERSION, false, null, null)
                .build()
        );
        platformDependency.getLibraryManager().loadLibrary(platformDependency.buildLibrary(
                "com.github.twitch4j", "twitch4j-extensions", BuildConfig.TWITCH4J_VERSION, false, null, null)
                .build()
        );
        platformDependency.getLibraryManager().loadLibrary(platformDependency.buildLibrary(
                "com.github.twitch4j", "twitch4j-graphql", BuildConfig.TWITCH4J_VERSION, false, null, null)
                .build()
        );
        platformDependency.getLibraryManager().loadLibrary(platformDependency.buildLibrary(
                "com.github.twitch4j", "twitch4j-helix", BuildConfig.TWITCH4J_VERSION, false, null, null)
                .build()
        );
        platformDependency.getLibraryManager().loadLibrary(platformDependency.buildLibrary(
                "com.github.twitch4j", "twitch4j-kraken", BuildConfig.TWITCH4J_VERSION, false, null, null)
                .build()
        );
        platformDependency.getLibraryManager().loadLibrary(platformDependency.buildLibrary(
                "com.github.twitch4j", "twitch4j-messaginginterface", BuildConfig.TWITCH4J_VERSION, false, null, null)
                .build()
        );
        platformDependency.getLibraryManager().loadLibrary(platformDependency.buildLibrary(
                "com.github.twitch4j", "twitch4j-pubsub", BuildConfig.TWITCH4J_VERSION, false, null, null)
                .build()
        );
        platformDependency.getLibraryManager().loadLibrary(platformDependency.buildLibrary(
                "com.github.twitch4j", "twitch4j-util", BuildConfig.TWITCH4J_VERSION, false, null, null)
                .build()
        );
        platformDependency.getLibraryManager().loadLibrary(platformDependency.buildLibrary(
                "com.neovisionaries", "nv-websocket-client", "2.14", true, null, null)
                .build()
        );
        platformDependency.getLibraryManager().loadLibrary(platformDependency.buildLibrary(
                "com.bucket4j", "bucket4j_jdk8-core", "8.10.1", true, null, null)
                .build()
        );
        platformDependency.getLibraryManager().loadLibrary(platformDependency.buildLibrary(
                "org.slf4j", "slf4j-api", "2.1.0-alpha1", true, null, null)
                .build()
        );
        platformDependency.getLibraryManager().loadLibrary(platformDependency.buildLibrary(
                "io.github.openfeign", "feign-slf4j", "13.4", true, null, null)
                .build()
        );
        platformDependency.getLibraryManager().loadLibrary(platformDependency.buildLibrary(
                "io.github.openfeign", "feign-okhttp", "13.4", true, null, null)
                .build()
        );
        platformDependency.getLibraryManager().loadLibrary(platformDependency.buildLibrary(
                "io.github.openfeign", "feign-jackson", "13.4", true, null, null)
                .build()
        );
        platformDependency.getLibraryManager().loadLibrary(platformDependency.buildLibrary(
                "io.github.openfeign", "feign-hystrix", "13.4", true, null, null)
                .build()
        );
        platformDependency.getLibraryManager().loadLibrary(platformDependency.buildLibrary(
                "com.fasterxml.jackson.core", "jackson-databind", "2.18.0-rc1", true, null, null)
                .build()
        );
        platformDependency.getLibraryManager().loadLibrary(platformDependency.buildLibrary(
                "com.fasterxml.jackson.core", "jackson-core", "2.18.0-rc1", true, null, null)
                .build()
        );
        platformDependency.getLibraryManager().loadLibrary(platformDependency.buildLibrary(
                "com.fasterxml.jackson.core", "jackson-annotations", "2.18.0-rc1", true, null, null)
                .build()
        );
        platformDependency.getLibraryManager().loadLibrary(platformDependency.buildLibrary(
                "com.fasterxml.jackson.datatype", "jackson-datatype-jsr310", "2.18.0-rc1", true, null, null)
                .build()
        );
        platformDependency.getLibraryManager().loadLibrary(platformDependency.buildLibrary(
                "com.github.tony19", "named-regexp", "1.0.0", true, null, null)
                .build()
        );
        platformDependency.getLibraryManager().loadLibrary(platformDependency.buildLibrary(
                "org.jetbrains", "annotations", "24.1.0", true, null, null)
                .build()
        );
        platformDependency.getLibraryManager().loadLibrary(platformDependency.buildLibrary(
                "com.netflix.hystrix", "hystrix-core", "1.5.18", true, null, null)
                .build()
        );
        platformDependency.getLibraryManager().loadLibrary(platformDependency.buildLibrary(
                "com.squareup.okhttp3", "okhttp", "5.0.0-alpha.14", true, null, null)
                .build()
        );
        platformDependency.getLibraryManager().loadLibrary(platformDependency.buildLibrary(
                "commons-configuration", "commons-configuration", "1.10", true, null, null)
                .build()
        );
        platformDependency.getLibraryManager().loadLibrary(platformDependency.buildLibrary(
                "commons-io", "commons-io", "2.17.0", true, null, null)
                .build()
        );
        platformDependency.getLibraryManager().loadLibrary(platformDependency.buildLibrary(
                "io.github.xanthic.cache", "cache-provider-caffeine", "0.6.1", true, null, null)
                .build()
        );
        platformDependency.getLibraryManager().loadLibrary(platformDependency.buildLibrary(
                "org.apache.commons", "commons-lang3", "3.17.0", true, null, null)
                .build()
        );

        isLoaded = true;
    }

    public void sendMessage(FEntity sender, MessageTag messageTag, UnaryOperator<String> twitchString) {
        if (checkModulePredicates(sender)) return;

        injector.getInstance(TwitchIntegration.class).sendMessage(sender, messageTag, twitchString);
    }

    public void disconnect() {
        if (!isLoaded) return;

        injector.getInstance(TwitchIntegration.class).disconnect();
    }
}
