package net.flectone.pulse.module.integration.telegram;

import com.alessiodp.libby.Library;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.BuildConfig;
import net.flectone.pulse.config.Integration;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.execution.scheduler.TaskScheduler;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.IntegrationMessageFormat;
import net.flectone.pulse.model.event.message.context.MessageContext;
import net.flectone.pulse.module.integration.telegram.listener.TelegramPulseListener;
import net.flectone.pulse.module.integration.telegram.sender.TelegramSender;
import net.flectone.pulse.platform.controller.ModuleController;
import net.flectone.pulse.platform.formatter.IntegrationFormatter;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.processing.resolver.LibraryResolver;
import net.flectone.pulse.processing.resolver.ReflectionResolver;
import net.flectone.pulse.service.SocialService;
import net.flectone.pulse.util.LazyInstance;
import net.flectone.pulse.util.constant.ModuleName;
import net.flectone.pulse.util.constant.SettingText;
import net.flectone.pulse.util.file.FileFacade;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.function.UnaryOperator;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class TelegramModuleImpl implements TelegramModule {

    private final FileFacade fileFacade;
    private final ReflectionResolver reflectionResolver;
    private final ModuleController moduleController;
    private final IntegrationFormatter integrationFormatter;
    private final ListenerRegistry listenerRegistry;
    private final TaskScheduler taskScheduler;
    private final SocialService socialService;
    private final LazyInstance<TelegramIntegration> telegramIntegration;
    private final LazyInstance<TelegramSender> telegramSender;

    @Override
    public void onEnable() {
        reflectionResolver.hasClassOrElse("org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient", this::loadLibraries);

        taskScheduler.runAsync(() -> telegramIntegration.get().hook(), true);

        listenerRegistry.register(TelegramPulseListener.class);
    }

    @Override
    public void onDisable() {
        telegramIntegration.get().unhook();
    }

    private void loadLibraries(LibraryResolver libraryResolver) {
        libraryResolver.loadLibrary(Library.builder()
                .groupId("org{}telegram")
                .artifactId("telegrambots-longpolling")
                .version(BuildConfig.TELEGRAMBOTS_VERSION)
                .repository(BuildConfig.MAVEN_REPOSITORY)
                .resolveTransitiveDependencies(true)
                .build()
        );

        libraryResolver.loadLibrary(Library.builder()
                .groupId("org{}telegram")
                .artifactId("telegrambots-client")
                .version(BuildConfig.TELEGRAMBOTS_VERSION)
                .repository(BuildConfig.MAVEN_REPOSITORY)
                .resolveTransitiveDependencies(true)
                .build()
        );

        libraryResolver.loadLibrary(Library.builder()
                .groupId("com{}squareup{}okhttp3")
                .artifactId("okhttp")
                .version("5.0.0-alpha.14")
                .repository(BuildConfig.MAVEN_REPOSITORY)
                .resolveTransitiveDependencies(true)
                .build()
        );
    }

    @Override
    public ModuleName name() {
        return ModuleName.INTEGRATION_TELEGRAM;
    }

    @Override
    public Integration.Telegram config() {
        return fileFacade.integration().telegram();
    }

    @Override
    public Permission.Integration.Telegram permission() {
        return fileFacade.permission().integration().telegram();
    }

    @Override
    public Localization.Integration.Telegram localization(FPlayer fPlayer) {
        return fileFacade.localization(socialService.getSetting(fPlayer, SettingText.LOCALE)).integration().telegram();
    }

    @Override
    public void sendMessage(@NonNull ModuleName moduleName, @NonNull MessageContext messageContext, @NonNull IntegrationMessageFormat integrationMessageFormat) {
        // skip empty message names
        List<String> messageNames = integrationFormatter.getExistedMessageNames(moduleName, integrationMessageFormat, config());
        if (messageNames.isEmpty()) return;

        // skip vanished player
        if (integrationFormatter.isVanished(messageContext)) return;

        FEntity sender = messageContext.sender();
        if (moduleController.isDisabledFor(this, sender)) return;

        // create formatter
        UnaryOperator<String> integrationFormat = integrationFormatter.createFormat(integrationMessageFormat, messageContext);

        // send to discord
        for (String specificMessageName : messageNames) {
            telegramSender.get().sendMessage(sender, specificMessageName.toUpperCase(), integrationFormat);
        }
    }

}
