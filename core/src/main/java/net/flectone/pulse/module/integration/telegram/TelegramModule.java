package net.flectone.pulse.module.integration.telegram;

import com.alessiodp.libby.Library;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.BuildConfig;
import net.flectone.pulse.config.Integration;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.module.AbstractModule;
import net.flectone.pulse.util.file.FileFacade;
import net.flectone.pulse.processing.resolver.LibraryResolver;
import net.flectone.pulse.processing.resolver.ReflectionResolver;

import java.util.function.UnaryOperator;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class TelegramModule extends AbstractModule {

    private final FileFacade fileFacade;
    private final ReflectionResolver reflectionResolver;
    private final Injector injector;

    @Override
    public void onEnable() {
        super.onEnable();

        reflectionResolver.hasClassOrElse("org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient", this::loadLibraries);

        injector.getInstance(TelegramIntegration.class).hook();
    }

    @Override
    public void onDisable() {
        super.onDisable();

        injector.getInstance(TelegramIntegration.class).unhook();
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
    public Integration.Telegram config() {
        return fileFacade.integration().telegram();
    }

    @Override
    public Permission.Integration.Telegram permission() {
        return fileFacade.permission().integration().telegram();
    }

    public void sendMessage(FEntity sender, String messageName, UnaryOperator<String> telegramString) {
        if (isModuleDisabledFor(sender)) return;

        injector.getInstance(TelegramIntegration.class).sendMessage(sender, messageName, telegramString);
    }
}
