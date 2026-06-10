package net.flectone.pulse.module.integration.twitch;

import com.alessiodp.libby.Library;
import com.alessiodp.libby.relocation.Relocation;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.BuildConfig;
import net.flectone.pulse.config.Integration;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.execution.scheduler.TaskScheduler;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.EventMetadata;
import net.flectone.pulse.model.event.IntegrationMetadata;
import net.flectone.pulse.module.ModuleLocalization;
import net.flectone.pulse.module.integration.telegram.sender.TelegramSender;
import net.flectone.pulse.module.integration.twitch.listener.TwitchPulseListener;
import net.flectone.pulse.platform.controller.ModuleController;
import net.flectone.pulse.platform.formatter.IntegrationFormatter;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.processing.resolver.LibraryResolver;
import net.flectone.pulse.processing.resolver.ReflectionResolver;
import net.flectone.pulse.service.SocialService;
import net.flectone.pulse.util.constant.ModuleName;
import net.flectone.pulse.util.constant.SettingText;
import net.flectone.pulse.util.file.FileFacade;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.function.UnaryOperator;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class TwitchModule implements ModuleLocalization<Localization.Integration.Twitch> {

    private final FileFacade fileFacade;
    private final ReflectionResolver reflectionResolver;
    private final ModuleController moduleController;
    private final IntegrationFormatter integrationFormatter;
    private final ListenerRegistry listenerRegistry;
    private final TaskScheduler taskScheduler;
    private final SocialService socialService;
    private final Injector injector;

    @Override
    public void onEnable() {
        reflectionResolver.hasClassOrElse("com.github.twitch4j.TwitchClient", this::loadLibraries);

        taskScheduler.runAsync(() -> injector.getInstance(TwitchIntegration.class).hook(), true);

        listenerRegistry.register(TwitchPulseListener.class);
    }

    @Override
    public void onDisable() {
        injector.getInstance(TwitchIntegration.class).unhook();
    }

    @Override
    public ModuleName name() {
        return ModuleName.INTEGRATION_TWITCH;
    }

    @Override
    public Integration.Twitch config() {
        return fileFacade.integration().twitch();
    }

    @Override
    public Permission.Integration.Twitch permission() {
        return fileFacade.permission().integration().twitch();
    }

    @Override
    public Localization.Integration.Twitch localization(FPlayer fPlayer) {
        return fileFacade.localization(socialService.getSetting(fPlayer, SettingText.LOCALE)).integration().twitch();
    }

    public void sendMessage(@NonNull EventMetadata<?> eventMetadata, @NonNull ModuleName moduleName, @NonNull String format) {
        IntegrationMetadata integrationMetadata = eventMetadata.integrationMetadata();
        if (integrationMetadata == null) return;

        // skip empty message names
        List<String> messageNames = integrationFormatter.getExistedMessageNames(moduleName, integrationMetadata, config());
        if (messageNames.isEmpty()) return;

        // skip vanished player
        if (integrationFormatter.isVanished(eventMetadata)) return;

        FEntity sender = eventMetadata.sender();
        if (moduleController.isDisabledFor(this, sender)) return;

        // create formatter
        UnaryOperator<String> integrationFormat = integrationFormatter.createFormat(eventMetadata, integrationMetadata, format);

        // send to discord
        TelegramSender telegramSender = injector.getInstance(TelegramSender.class);
        for (String specificMessageName : messageNames) {
            telegramSender.sendMessage(sender, specificMessageName, integrationFormat);
        }
    }

    // I hate this library...
    private void loadLibraries(LibraryResolver libraryResolver) {
        libraryResolver.loadLibrary(Library.builder()
                .groupId("com{}github{}philippheuer{}credentialmanager")
                .artifactId("credentialmanager")
                .version("0.3.1")
                .repository(BuildConfig.MAVEN_REPOSITORY)
                .resolveTransitiveDependencies(true)
                .build()
        );

        libraryResolver.loadLibrary(Library.builder()
                .groupId("com{}github{}philippheuer{}events4j")
                .artifactId("events4j-core")
                .version("0.12.2")
                .repository(BuildConfig.MAVEN_REPOSITORY)
                .resolveTransitiveDependencies(true)
                .build()
        );

        libraryResolver.loadLibrary(Library.builder()
                .groupId("com{}github{}philippheuer{}events4j")
                .artifactId("events4j-handler-simple")
                .version("0.12.2")
                .repository(BuildConfig.MAVEN_REPOSITORY)
                .resolveTransitiveDependencies(true)
                .build()
        );

        libraryResolver.loadLibrary(Library.builder()
                .groupId("com{}github{}twitch4j")
                .artifactId("twitch4j")
                .version(BuildConfig.TWITCH4J_VERSION)
                .repository(BuildConfig.MAVEN_REPOSITORY)
                .relocate(Relocation.builder()
                        .pattern("com{}fasterxml{}jackson")
                        .relocatedPattern(BuildConfig.RELOCATED_PATTERN + ".twitch.jackson")
                        .build()
                )
                .build()
        );

        libraryResolver.loadLibrary(Library.builder()
                .groupId("com{}github{}twitch4j")
                .artifactId("twitch4j-chat")
                .version(BuildConfig.TWITCH4J_VERSION)
                .repository(BuildConfig.MAVEN_REPOSITORY)
                .relocate(Relocation.builder()
                        .pattern("com{}fasterxml{}jackson")
                        .relocatedPattern(BuildConfig.RELOCATED_PATTERN + ".twitch.jackson")
                        .build()
                )
                .build()
        );

        libraryResolver.loadLibrary(Library.builder()
                .groupId("com{}github{}twitch4j")
                .artifactId("twitch4j-auth")
                .version(BuildConfig.TWITCH4J_VERSION)
                .repository(BuildConfig.MAVEN_REPOSITORY)
                .relocate(Relocation.builder()
                        .pattern("com{}fasterxml{}jackson")
                        .relocatedPattern(BuildConfig.RELOCATED_PATTERN + ".twitch.jackson")
                        .build()
                )
                .build()
        );

        libraryResolver.loadLibrary(Library.builder()
                .groupId("com{}github{}twitch4j")
                .artifactId("twitch4j-common")
                .version(BuildConfig.TWITCH4J_VERSION)
                .repository(BuildConfig.MAVEN_REPOSITORY)
                .relocate(Relocation.builder()
                        .pattern("com{}fasterxml{}jackson")
                        .relocatedPattern(BuildConfig.RELOCATED_PATTERN + ".twitch.jackson")
                        .build()
                )
                .build()
        );

        libraryResolver.loadLibrary(Library.builder()
                .groupId("com{}github{}twitch4j")
                .artifactId("twitch4j-client-websocket")
                .version(BuildConfig.TWITCH4J_VERSION)
                .repository(BuildConfig.MAVEN_REPOSITORY)
                .build()
        );

        libraryResolver.loadLibrary(Library.builder()
                .groupId("com{}github{}twitch4j")
                .artifactId("twitch4j-util")
                .version(BuildConfig.TWITCH4J_VERSION)
                .repository(BuildConfig.MAVEN_REPOSITORY)
                .build()
        );

        libraryResolver.loadLibrary(Library.builder()
                .groupId("com{}github{}twitch4j")
                .artifactId("twitch4j-eventsub-common")
                .version(BuildConfig.TWITCH4J_VERSION)
                .repository(BuildConfig.MAVEN_REPOSITORY)
                .build()
        );

        libraryResolver.loadLibrary(Library.builder()
                .groupId("com{}github{}twitch4j")
                .artifactId("twitch4j-eventsub-websocket")
                .version(BuildConfig.TWITCH4J_VERSION)
                .repository(BuildConfig.MAVEN_REPOSITORY)
                .build()
        );

        libraryResolver.loadLibrary(Library.builder()
                .groupId("com{}github{}twitch4j")
                .artifactId("twitch4j-extensions")
                .version(BuildConfig.TWITCH4J_VERSION)
                .repository(BuildConfig.MAVEN_REPOSITORY)
                .build()
        );

        libraryResolver.loadLibrary(Library.builder()
                .groupId("com{}github{}twitch4j")
                .artifactId("twitch4j-graphql")
                .version(BuildConfig.TWITCH4J_VERSION)
                .repository(BuildConfig.MAVEN_REPOSITORY)
                .build()
        );

        libraryResolver.loadLibrary(Library.builder()
                .groupId("com{}github{}twitch4j")
                .artifactId("twitch4j-helix")
                .version(BuildConfig.TWITCH4J_VERSION)
                .repository(BuildConfig.MAVEN_REPOSITORY)
                .relocate(Relocation.builder()
                        .pattern("com{}fasterxml{}jackson")
                        .relocatedPattern(BuildConfig.RELOCATED_PATTERN + ".twitch.jackson")
                        .build()
                )
                .build()
        );

        libraryResolver.loadLibrary(Library.builder()
                .groupId("com{}github{}twitch4j")
                .artifactId("twitch4j-kraken")
                .version(BuildConfig.TWITCH4J_VERSION)
                .repository(BuildConfig.MAVEN_REPOSITORY)
                .relocate(Relocation.builder()
                        .pattern("com{}fasterxml{}jackson")
                        .relocatedPattern(BuildConfig.RELOCATED_PATTERN + ".twitch.jackson")
                        .build()
                )
                .build()
        );

        libraryResolver.loadLibrary(Library.builder()
                .groupId("com{}github{}twitch4j")
                .artifactId("twitch4j-messaginginterface")
                .version(BuildConfig.TWITCH4J_VERSION)
                .repository(BuildConfig.MAVEN_REPOSITORY)
                .build()
        );

        libraryResolver.loadLibrary(Library.builder()
                .groupId("com{}github{}twitch4j")
                .artifactId("twitch4j-pubsub")
                .version(BuildConfig.TWITCH4J_VERSION)
                .repository(BuildConfig.MAVEN_REPOSITORY)
                .build()
        );

        libraryResolver.loadLibrary(Library.builder()
                .groupId("com{}github{}twitch4j")
                .artifactId("twitch4j-util")
                .version(BuildConfig.TWITCH4J_VERSION)
                .repository(BuildConfig.MAVEN_REPOSITORY)
                .build()
        );

        libraryResolver.loadLibrary(Library.builder()
                .groupId("com{}neovisionaries")
                .artifactId("nv-websocket-client")
                .version("2.14")
                .repository(BuildConfig.MAVEN_REPOSITORY)
                .resolveTransitiveDependencies(true)
                .build()
        );

        libraryResolver.loadLibrary(Library.builder()
                .groupId("com{}bucket4j")
                .artifactId("bucket4j_jdk8-core")
                .version("8.10.1")
                .repository(BuildConfig.MAVEN_REPOSITORY)
                .resolveTransitiveDependencies(true)
                .build()
        );

        libraryResolver.loadLibrary(Library.builder()
                .groupId("org{}slf4j")
                .artifactId("slf4j-api")
                .version("2.1.0-alpha1")
                .repository(BuildConfig.MAVEN_REPOSITORY)
                .resolveTransitiveDependencies(true)
                .build()
        );

        libraryResolver.loadLibrary(Library.builder()
                .groupId("io{}github{}openfeign")
                .artifactId("feign-slf4j")
                .version("13.6")
                .repository(BuildConfig.MAVEN_REPOSITORY)
                .resolveTransitiveDependencies(true)
                .relocate(Relocation.builder()
                        .pattern("com{}fasterxml{}jackson")
                        .relocatedPattern(BuildConfig.RELOCATED_PATTERN + ".twitch.jackson")
                        .build()
                )
                .build()
        );

        libraryResolver.loadLibrary(Library.builder()
                .groupId("io{}github{}openfeign")
                .artifactId("feign-okhttp")
                .version("13.6")
                .repository(BuildConfig.MAVEN_REPOSITORY)
                .resolveTransitiveDependencies(true)
                .relocate(Relocation.builder()
                        .pattern("com{}fasterxml{}jackson")
                        .relocatedPattern(BuildConfig.RELOCATED_PATTERN + ".twitch.jackson")
                        .build()
                )
                .build()
        );

        libraryResolver.loadLibrary(Library.builder()
                .groupId("io{}github{}openfeign")
                .artifactId("feign-jackson")
                .version("13.6")
                .repository(BuildConfig.MAVEN_REPOSITORY)
                .resolveTransitiveDependencies(true)
                .relocate(Relocation.builder()
                        .pattern("com{}fasterxml{}jackson")
                        .relocatedPattern(BuildConfig.RELOCATED_PATTERN + ".twitch.jackson")
                        .build()
                )
                .build()
        );

        libraryResolver.loadLibrary(Library.builder()
                .groupId("io{}github{}openfeign")
                .artifactId("feign-hystrix")
                .version("13.6")
                .repository(BuildConfig.MAVEN_REPOSITORY)
                .resolveTransitiveDependencies(true)
                .relocate(Relocation.builder()
                        .pattern("com{}fasterxml{}jackson")
                        .relocatedPattern(BuildConfig.RELOCATED_PATTERN + ".twitch.jackson")
                        .build()
                )
                .build()
        );

        libraryResolver.loadLibrary(Library.builder()
                .groupId("com{}fasterxml{}jackson{}core")
                .artifactId("jackson-core")
                .version("2.15.0-rc3")
                .repository(BuildConfig.MAVEN_REPOSITORY)
                .resolveTransitiveDependencies(true)
                .relocate(Relocation.builder()
                        .pattern("com{}fasterxml{}jackson")
                        .relocatedPattern(BuildConfig.RELOCATED_PATTERN + ".twitch.jackson")
                        .build()
                )
                .build()
        );

        libraryResolver.loadLibrary(Library.builder()
                .groupId("com{}fasterxml{}jackson{}core")
                .artifactId("jackson-databind")
                .version("2.15.0-rc3")
                .repository(BuildConfig.MAVEN_REPOSITORY)
                .resolveTransitiveDependencies(true)
                .relocate(Relocation.builder()
                        .pattern("com{}fasterxml{}jackson")
                        .relocatedPattern(BuildConfig.RELOCATED_PATTERN + ".twitch.jackson")
                        .build()
                )
                .build()
        );


        libraryResolver.loadLibrary(Library.builder()
                .groupId("com{}fasterxml{}jackson{}core")
                .artifactId("jackson-annotations")
                .version("2.15.0-rc3")
                .repository(BuildConfig.MAVEN_REPOSITORY)
                .resolveTransitiveDependencies(true)
                .relocate(Relocation.builder()
                        .pattern("com{}fasterxml{}jackson")
                        .relocatedPattern(BuildConfig.RELOCATED_PATTERN + ".twitch.jackson")
                        .build()
                )
                .build()
        );

        libraryResolver.loadLibrary(Library.builder()
                .groupId("com{}fasterxml{}jackson{}datatype")
                .artifactId("jackson-datatype-jsr310")
                .version("2.15.0-rc3")
                .repository(BuildConfig.MAVEN_REPOSITORY)
                .resolveTransitiveDependencies(true)
                .relocate(Relocation.builder()
                        .pattern("com{}fasterxml{}jackson")
                        .relocatedPattern(BuildConfig.RELOCATED_PATTERN + ".twitch.jackson")
                        .build()
                )
                .build()
        );

        libraryResolver.loadLibrary(Library.builder()
                .groupId("com{}github{}tony19")
                .artifactId("named-regexp")
                .version("1.0.0")
                .repository(BuildConfig.MAVEN_REPOSITORY)
                .resolveTransitiveDependencies(true)
                .build()
        );

        libraryResolver.loadLibrary(Library.builder()
                .groupId("org{}jetbrains")
                .artifactId("annotations")
                .version("26.0.2")
                .repository(BuildConfig.MAVEN_REPOSITORY)
                .resolveTransitiveDependencies(true)
                .build()
        );

        libraryResolver.loadLibrary(Library.builder()
                .groupId("com{}netflix{}hystrix")
                .artifactId("hystrix-core")
                .version("1.5.18")
                .repository(BuildConfig.MAVEN_REPOSITORY)
                .resolveTransitiveDependencies(true)
                .build()
        );

        libraryResolver.loadLibrary(Library.builder()
                .groupId("com{}squareup{}okhttp3")
                .artifactId("okhttp")
                .version("4.12.0")
                .repository(BuildConfig.MAVEN_REPOSITORY)
                .resolveTransitiveDependencies(true)
                .build()
        );

        libraryResolver.loadLibrary(Library.builder()
                .groupId("commons-configuration")
                .artifactId("commons-configuration")
                .version("1.10")
                .repository(BuildConfig.MAVEN_REPOSITORY)
                .resolveTransitiveDependencies(true)
                .build()
        );

        libraryResolver.loadLibrary(Library.builder()
                .groupId("commons-io")
                .artifactId("commons-io")
                .version("2.19.0")
                .repository(BuildConfig.MAVEN_REPOSITORY)
                .resolveTransitiveDependencies(true)
                .build()
        );

        libraryResolver.loadLibrary(Library.builder()
                .groupId("io{}github{}xanthic{}cache")
                .artifactId("cache-provider-caffeine")
                .version("0.6.1")
                .repository(BuildConfig.MAVEN_REPOSITORY)
                .resolveTransitiveDependencies(true)
                .build()
        );

        libraryResolver.loadLibrary(Library.builder()
                .groupId("org{}apache{}commons")
                .artifactId("commons-lang3")
                .version("3.17.0")
                .repository(BuildConfig.MAVEN_REPOSITORY)
                .resolveTransitiveDependencies(true)
                .build()
        );
    }
}
