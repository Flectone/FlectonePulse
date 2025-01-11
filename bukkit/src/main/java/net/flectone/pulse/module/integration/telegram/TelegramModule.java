package net.flectone.pulse.module.integration.telegram;

import com.alessiodp.libby.Library;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import lombok.SneakyThrows;
import net.flectone.pulse.BuildConfig;
import net.flectone.pulse.file.Integration;
import net.flectone.pulse.file.Permission;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.model.FEntity;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.module.AbstractModule;
import net.flectone.pulse.platform.LibraryResolver;
import net.flectone.pulse.util.MessageTag;

import java.util.function.UnaryOperator;

@Singleton
public class TelegramModule extends AbstractModule {

    private final Integration.Telegram integration;
    private final Permission.Integration.Telegram permission;

    private final LibraryResolver libraryResolver;
    private final Injector injector;

    @Inject
    public TelegramModule(FileManager fileManager,
                          LibraryResolver libraryResolver,
                          Injector injector) {
        this.libraryResolver = libraryResolver;
        this.injector = injector;

        integration = fileManager.getIntegration().getTelegram();
        permission = fileManager.getPermission().getIntegration().getTelegram();

        addPredicate(fEntity -> fEntity instanceof FPlayer fPlayer && !fPlayer.is(FPlayer.Setting.TELEGRAM));
    }

    @Override
    public void reload() {
        registerModulePermission(permission);

        loadLibraries();

        disconnect();

        injector.getInstance(TelegramIntegration.class).hook();
    }

    private void loadLibraries() {
        libraryResolver.loadLibrary(Library.builder()
                .groupId("org{}telegram")
                .artifactId("telegrambots-longpolling")
                .version(BuildConfig.TELEGRAMBOTS_VERSION)
                .resolveTransitiveDependencies(true)
                .build()
        );

        libraryResolver.loadLibrary(Library.builder()
                .groupId("org{}telegram")
                .artifactId("telegrambots-client")
                .version(BuildConfig.TELEGRAMBOTS_VERSION)
                .resolveTransitiveDependencies(true)
                .build()
        );

        libraryResolver.loadLibrary(Library.builder()
                .groupId("com{}squareup{}okhttp3")
                .artifactId("okhttp")
                .version("5.0.0-alpha.14")
                .resolveTransitiveDependencies(true)
                .build()
        );
    }

    @Override
    public boolean isConfigEnable() {
        return integration.isEnable();
    }

    public void sendMessage(FEntity sender, MessageTag messageTag, UnaryOperator<String> telegramString) {
        if (checkModulePredicates(sender)) return;

        injector.getInstance(TelegramIntegration.class).sendMessage(sender, messageTag, telegramString);
    }

    @SneakyThrows
    public void disconnect() {
        if (!isEnable()) return;

        injector.getInstance(TelegramIntegration.class).disconnect();
    }
}
