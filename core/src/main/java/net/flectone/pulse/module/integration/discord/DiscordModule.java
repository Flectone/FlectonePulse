package net.flectone.pulse.module.integration.discord;

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
import net.flectone.pulse.util.logging.FLogger;

import java.util.function.UnaryOperator;

@Singleton
public class DiscordModule extends AbstractModule {

    private final Integration.Discord integration;
    private final Permission.Integration.Discord permission;
    private final ReflectionResolver reflectionResolver;
    private final Injector injector;
    private final FLogger fLogger;

    @Inject
    public DiscordModule(FileResolver fileResolver,
                         ReflectionResolver reflectionResolver,
                         Injector injector,
                         FLogger fLogger) {
        this.integration = fileResolver.getIntegration().getDiscord();
        this.permission = fileResolver.getPermission().getIntegration().getDiscord();
        this.reflectionResolver = reflectionResolver;
        this.injector = injector;
        this.fLogger = fLogger;
    }

    @Override
    public void onEnable() {
        registerModulePermission(permission);

        reflectionResolver.hasClassOrElse("discord4j.core.DiscordClient", this::loadLibraries);

        addPredicate(fEntity -> fEntity instanceof FPlayer fPlayer && !fPlayer.isSetting(FPlayer.Setting.DISCORD));

        try {
            injector.getInstance(DiscordIntegration.class).hook();
        } catch (Exception e) {
            fLogger.warning(e);
        }
    }

    @Override
    public void onDisable() {
        injector.getInstance(DiscordIntegration.class).unhook();
    }

    private void loadLibraries(LibraryResolver libraryResolver) {
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
    protected boolean isConfigEnable() {
        return integration.isEnable();
    }

    public void sendMessage(FEntity sender, MessageType messageType, UnaryOperator<String> discordString) {
        if (isModuleDisabledFor(sender)) return;

        injector.getInstance(DiscordIntegration.class).sendMessage(sender, messageType, discordString);
    }
}
