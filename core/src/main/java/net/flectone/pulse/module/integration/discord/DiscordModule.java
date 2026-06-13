package net.flectone.pulse.module.integration.discord;

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
import net.flectone.pulse.module.integration.discord.listener.DiscordPulseListener;
import net.flectone.pulse.module.integration.discord.sender.DiscordSender;
import net.flectone.pulse.platform.controller.ModuleController;
import net.flectone.pulse.platform.formatter.IntegrationFormatter;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.processing.resolver.LibraryResolver;
import net.flectone.pulse.processing.resolver.ReflectionResolver;
import net.flectone.pulse.service.SocialService;
import net.flectone.pulse.util.constant.ModuleName;
import net.flectone.pulse.util.constant.SettingText;
import net.flectone.pulse.util.file.FileFacade;
import net.flectone.pulse.util.logging.FLogger;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.function.UnaryOperator;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class DiscordModule implements ModuleLocalization<Localization.Integration.Discord> {

    private final FileFacade fileFacade;
    private final ReflectionResolver reflectionResolver;
    private final ModuleController moduleController;
    private final IntegrationFormatter integrationFormatter;
    private final ListenerRegistry listenerRegistry;
    private final TaskScheduler taskScheduler;
    private final SocialService socialService;
    private final Injector injector;
    private final FLogger fLogger;

    @Override
    public void onEnable() {
        reflectionResolver.hasClassOrElse("discord4j.core.DiscordClient", this::loadLibraries);

        taskScheduler.runAsync(() -> {
            try {
                injector.getInstance(DiscordIntegration.class).hook();
            } catch (Exception e) {
                fLogger.warning(e);
            }
        }, true);

        listenerRegistry.register(DiscordPulseListener.class);
    }

    @Override
    public void onDisable() {
        injector.getInstance(DiscordIntegration.class).unhook();
    }

    @Override
    public ModuleName name() {
        return ModuleName.INTEGRATION_DISCORD;
    }

    @Override
    public Integration.Discord config() {
        return fileFacade.integration().discord();
    }

    @Override
    public Permission.Integration.Discord permission() {
        return fileFacade.permission().integration().discord();
    }

    @Override
    public Localization.Integration.Discord localization(FPlayer fPlayer) {
        return fileFacade.localization(socialService.getSetting(fPlayer, SettingText.LOCALE)).integration().discord();
    }

    private void loadLibraries(LibraryResolver libraryResolver) {
        libraryResolver.loadLibrary(Library.builder()
                .groupId("com{}discord4j")
                .artifactId("discord4j-core")
                .version(BuildConfig.DISCORD4J_VERSION)
                .repository(BuildConfig.MAVEN_REPOSITORY)
                .resolveTransitiveDependencies(true)
                .relocate(Relocation.builder()
                        .pattern("io{}netty")
                        .relocatedPattern(BuildConfig.RELOCATED_PATTERN + ".discord.netty")
                        .build()
                )
                .relocate(Relocation.builder()
                        .pattern("com{}fasterxml")
                        .relocatedPattern(BuildConfig.RELOCATED_PATTERN + ".discord.fasterxml")
                        .build()
                )
                .build()
        );
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
        DiscordSender discordSender = injector.getInstance(DiscordSender.class);
        for (String specificMessageName : messageNames) {
            discordSender.sendMessage(sender, specificMessageName, integrationFormat);
        }
    }

}
