package net.flectone.pulse.module.integration.twitch;

import com.alessiodp.libby.Library;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import net.flectone.pulse.BuildConfig;
import net.flectone.pulse.config.Integration;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.AbstractModule;
import net.flectone.pulse.processing.resolver.ReflectionResolver;
import net.flectone.pulse.processing.resolver.FileResolver;
import net.flectone.pulse.processing.resolver.LibraryResolver;
import net.flectone.pulse.util.constant.MessageType;

import java.util.function.UnaryOperator;

@Singleton
public class TwitchModule extends AbstractModule {

    private final Integration.Twitch integration;
    private final Permission.Integration.Twitch permission;
    private final ReflectionResolver reflectionResolver;
    private final Injector injector;

    @Inject
    public TwitchModule(FileResolver fileResolver,
                        ReflectionResolver reflectionResolver,
                        Injector injector) {
        this.integration = fileResolver.getIntegration().getTwitch();
        this.permission = fileResolver.getPermission().getIntegration().getTwitch();
        this.reflectionResolver = reflectionResolver;
        this.injector = injector;
    }

    @Override
    public void onEnable() {
        registerModulePermission(permission);

        reflectionResolver.hasClassOrElse("com.github.twitch4j.TwitchClient", this::loadLibraries);

        injector.getInstance(TwitchIntegration.class).hook();

        addPredicate(fEntity -> fEntity instanceof FPlayer fPlayer && !fPlayer.isSetting(FPlayer.Setting.TWITCH));
    }

    @Override
    public void onDisable() {
        injector.getInstance(TwitchIntegration.class).unhook();
    }

    private void loadLibraries(LibraryResolver libraryResolver) {
        libraryResolver.loadLibrary(Library.builder()
                .groupId("com{}github{}philippheuer{}credentialmanager")
                .artifactId("credentialmanager")
                .version("0.3.1")
                .resolveTransitiveDependencies(true)
                .build()
        );

        libraryResolver.loadLibrary(Library.builder()
                .groupId("com{}github{}philippheuer{}events4j")
                .artifactId("events4j-core")
                .version("0.12.2")
                .resolveTransitiveDependencies(true)
                .build()
        );

        libraryResolver.loadLibrary(Library.builder()
                .groupId("com{}github{}philippheuer{}events4j")
                .artifactId("events4j-handler-simple")
                .version("0.12.2")
                .resolveTransitiveDependencies(true)
                .build()
        );

        libraryResolver.loadLibrary(Library.builder()
                .groupId("com{}github{}twitch4j")
                .artifactId("twitch4j")
                .version(BuildConfig.TWITCH4J_VERSION)
                .build()
        );

        libraryResolver.loadLibrary(Library.builder()
                .groupId("com{}github{}twitch4j")
                .artifactId("twitch4j-chat")
                .version(BuildConfig.TWITCH4J_VERSION)
                .build()
        );

        libraryResolver.loadLibrary(Library.builder()
                .groupId("com{}github{}twitch4j")
                .artifactId("twitch4j-auth")
                .version(BuildConfig.TWITCH4J_VERSION)
                .build()
        );

        libraryResolver.loadLibrary(Library.builder()
                .groupId("com{}github{}twitch4j")
                .artifactId("twitch4j-common")
                .version(BuildConfig.TWITCH4J_VERSION)
                .build()
        );

        libraryResolver.loadLibrary(Library.builder()
                .groupId("com{}github{}twitch4j")
                .artifactId("twitch4j-client-websocket")
                .version(BuildConfig.TWITCH4J_VERSION)
                .build()
        );

        libraryResolver.loadLibrary(Library.builder()
                .groupId("com{}github{}twitch4j")
                .artifactId("twitch4j-util")
                .version(BuildConfig.TWITCH4J_VERSION)
                .build()
        );

        libraryResolver.loadLibrary(Library.builder()
                .groupId("com{}github{}twitch4j")
                .artifactId("twitch4j-eventsub-common")
                .version(BuildConfig.TWITCH4J_VERSION)
                .build()
        );

        libraryResolver.loadLibrary(Library.builder()
                .groupId("com{}github{}twitch4j")
                .artifactId("twitch4j-eventsub-websocket")
                .version(BuildConfig.TWITCH4J_VERSION)
                .build()
        );

        libraryResolver.loadLibrary(Library.builder()
                .groupId("com{}github{}twitch4j")
                .artifactId("twitch4j-extensions")
                .version(BuildConfig.TWITCH4J_VERSION)
                .build()
        );

        libraryResolver.loadLibrary(Library.builder()
                .groupId("com{}github{}twitch4j")
                .artifactId("twitch4j-graphql")
                .version(BuildConfig.TWITCH4J_VERSION)
                .build()
        );

        libraryResolver.loadLibrary(Library.builder()
                .groupId("com{}github{}twitch4j")
                .artifactId("twitch4j-helix")
                .version(BuildConfig.TWITCH4J_VERSION)
                .build()
        );

        libraryResolver.loadLibrary(Library.builder()
                .groupId("com{}github{}twitch4j")
                .artifactId("twitch4j-kraken")
                .version(BuildConfig.TWITCH4J_VERSION)
                .build()
        );

        libraryResolver.loadLibrary(Library.builder()
                .groupId("com{}github{}twitch4j")
                .artifactId("twitch4j-messaginginterface")
                .version(BuildConfig.TWITCH4J_VERSION)
                .build()
        );

        libraryResolver.loadLibrary(Library.builder()
                .groupId("com{}github{}twitch4j")
                .artifactId("twitch4j-pubsub")
                .version(BuildConfig.TWITCH4J_VERSION)
                .build()
        );

        libraryResolver.loadLibrary(Library.builder()
                .groupId("com{}github{}twitch4j")
                .artifactId("twitch4j-util")
                .version(BuildConfig.TWITCH4J_VERSION)
                .build()
        );

        libraryResolver.loadLibrary(Library.builder()
                .groupId("com{}neovisionaries")
                .artifactId("nv-websocket-client")
                .version("2.14")
                .resolveTransitiveDependencies(true)
                .build()
        );

        libraryResolver.loadLibrary(Library.builder()
                .groupId("com{}bucket4j")
                .artifactId("bucket4j_jdk8-core")
                .version("8.10.1")
                .resolveTransitiveDependencies(true)
                .build()
        );

        libraryResolver.loadLibrary(Library.builder()
                .groupId("org{}slf4j")
                .artifactId("slf4j-api")
                .version("2.1.0-alpha1")
                .resolveTransitiveDependencies(true)
                .build()
        );

        libraryResolver.loadLibrary(Library.builder()
                .groupId("io{}github{}openfeign")
                .artifactId("feign-slf4j")
                .version("13.6")
                .resolveTransitiveDependencies(true)
                .build()
        );

        libraryResolver.loadLibrary(Library.builder()
                .groupId("io{}github{}openfeign")
                .artifactId("feign-okhttp")
                .version("13.6")
                .resolveTransitiveDependencies(true)
                .build()
        );

        libraryResolver.loadLibrary(Library.builder()
                .groupId("io{}github{}openfeign")
                .artifactId("feign-jackson")
                .version("13.6")
                .resolveTransitiveDependencies(true)
                .build()
        );

        libraryResolver.loadLibrary(Library.builder()
                .groupId("io{}github{}openfeign")
                .artifactId("feign-hystrix")
                .version("13.6")
                .resolveTransitiveDependencies(true)
                .build()
        );

        libraryResolver.loadLibrary(Library.builder()
                .groupId("com{}fasterxml{}jackson{}core")
                .artifactId("jackson-core")
                .version("2.18.0-rc1")
                .resolveTransitiveDependencies(true)
                .build()
        );

        libraryResolver.loadLibrary(Library.builder()
                .groupId("com{}fasterxml{}jackson{}core")
                .artifactId("jackson-databind")
                .version("2.19.1")
                .resolveTransitiveDependencies(true)
                .build()
        );


        libraryResolver.loadLibrary(Library.builder()
                .groupId("com{}fasterxml{}jackson{}core")
                .artifactId("jackson-annotations")
                .version("2.19.1")
                .resolveTransitiveDependencies(true)
                .build()
        );

        libraryResolver.loadLibrary(Library.builder()
                .groupId("com{}fasterxml{}jackson{}datatype")
                .artifactId("jackson-datatype-jsr310")
                .version("2.19.1")
                .resolveTransitiveDependencies(true)
                .build()
        );

        libraryResolver.loadLibrary(Library.builder()
                .groupId("com{}github{}tony19")
                .artifactId("named-regexp")
                .version("1.0.0")
                .resolveTransitiveDependencies(true)
                .build()
        );

        libraryResolver.loadLibrary(Library.builder()
                .groupId("org{}jetbrains")
                .artifactId("annotations")
                .version("26.0.2")
                .resolveTransitiveDependencies(true)
                .build()
        );

        libraryResolver.loadLibrary(Library.builder()
                .groupId("com{}netflix{}hystrix")
                .artifactId("hystrix-core")
                .version("1.5.18")
                .resolveTransitiveDependencies(true)
                .build()
        );

        libraryResolver.loadLibrary(Library.builder()
                .groupId("com{}squareup{}okhttp3")
                .artifactId("okhttp")
                .version("4.12.0")
                .resolveTransitiveDependencies(true)
                .build()
        );

        libraryResolver.loadLibrary(Library.builder()
                .groupId("commons-configuration")
                .artifactId("commons-configuration")
                .version("1.10")
                .resolveTransitiveDependencies(true)
                .build()
        );

        libraryResolver.loadLibrary(Library.builder()
                .groupId("commons-io")
                .artifactId("commons-io")
                .version("2.19.0")
                .resolveTransitiveDependencies(true)
                .build()
        );

        libraryResolver.loadLibrary(Library.builder()
                .groupId("io{}github{}xanthic{}cache")
                .artifactId("cache-provider-caffeine")
                .version("0.6.1")
                .resolveTransitiveDependencies(true)
                .build()
        );

        libraryResolver.loadLibrary(Library.builder()
                .groupId("org{}apache{}commons")
                .artifactId("commons-lang3")
                .version("3.17.0")
                .resolveTransitiveDependencies(true)
                .build()
        );
    }

    @Override
    protected boolean isConfigEnable() {
        return integration.isEnable();
    }

    public void sendMessage(FEntity sender, String messageName, UnaryOperator<String> twitchString) {
        if (isModuleDisabledFor(sender)) return;

        injector.getInstance(TwitchIntegration.class).sendMessage(sender, messageName, twitchString);
    }
}
