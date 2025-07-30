package net.flectone.pulse.module.integration.telegram;

import com.alessiodp.libby.Library;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import net.flectone.pulse.BuildConfig;
import net.flectone.pulse.configuration.Integration;
import net.flectone.pulse.configuration.Permission;
import net.flectone.pulse.model.FEntity;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.module.AbstractModule;
import net.flectone.pulse.resolver.FileResolver;
import net.flectone.pulse.resolver.LibraryResolver;
import net.flectone.pulse.constant.MessageType;

import java.util.function.UnaryOperator;

@Singleton
public class TelegramModule extends AbstractModule {

    private final Integration.Telegram integration;
    private final Permission.Integration.Telegram permission;

    private final LibraryResolver libraryResolver;
    private final Injector injector;

    @Inject
    public TelegramModule(FileResolver fileResolver,
                          LibraryResolver libraryResolver,
                          Injector injector) {
        this.libraryResolver = libraryResolver;
        this.injector = injector;

        integration = fileResolver.getIntegration().getTelegram();
        permission = fileResolver.getPermission().getIntegration().getTelegram();
    }

    @Override
    public void onEnable() {
        registerModulePermission(permission);

        try {
            Class.forName("org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient");
        } catch (ClassNotFoundException e) {
            loadLibraries();
        }

        injector.getInstance(TelegramIntegration.class).hook();

        addPredicate(fEntity -> fEntity instanceof FPlayer fPlayer && !fPlayer.isSetting(FPlayer.Setting.TELEGRAM));
    }

    @Override
    public void onDisable() {
        injector.getInstance(TelegramIntegration.class).unhook();
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
    protected boolean isConfigEnable() {
        return integration.isEnable();
    }

    public void sendMessage(FEntity sender, MessageType messageType, UnaryOperator<String> telegramString) {
        if (checkModulePredicates(sender)) return;

        injector.getInstance(TelegramIntegration.class).sendMessage(sender, messageType, telegramString);
    }
}
