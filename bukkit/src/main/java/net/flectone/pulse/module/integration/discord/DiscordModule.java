package net.flectone.pulse.module.integration.discord;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import net.flectone.pulse.FlectonePulsePlugin;
import net.flectone.pulse.BuildConfig;
import net.flectone.pulse.file.Integration;
import net.flectone.pulse.file.Permission;
import net.flectone.pulse.logger.FLogger;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.model.FEntity;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.platform.PlatformDependency;
import net.flectone.pulse.util.MessageTag;
import net.flectone.pulse.module.AbstractModule;

import java.util.function.UnaryOperator;

@Singleton
public class DiscordModule extends AbstractModule {

    private final Integration.Discord integration;
    private final Permission.Integration.Discord permission;

    private final FlectonePulsePlugin flectonePulsePlugin;
    private final Injector injector;
    private final FLogger fLogger;

    private boolean isLoaded;

    @Inject
    public DiscordModule(FileManager fileManager,
                         FlectonePulsePlugin flectonePulsePlugin,
                         Injector injector,
                         FLogger fLogger) {
        this.flectonePulsePlugin = flectonePulsePlugin;
        this.injector = injector;
        this.fLogger = fLogger;

        integration = fileManager.getIntegration().getDiscord();
        permission = fileManager.getPermission().getIntegration().getDiscord();

        addPredicate(fEntity -> fEntity instanceof FPlayer fPlayer && !fPlayer.is(FPlayer.Setting.DISCORD));
    }

    @Override
    public void reload() {
        registerModulePermission(permission);

        loadLibraries();

        if (!isLoaded) return;

        DiscordIntegration discordIntegration = injector.getInstance(DiscordIntegration.class);
        discordIntegration.setEnable(isEnable());

        try {
            discordIntegration.reload();
        } catch (Exception e) {
            fLogger.warning(e);
        }
    }

    @Override
    public boolean isConfigEnable() {
        return integration.isEnable();
    }

    public void loadLibraries() {
        if (isLoaded) return;
        PlatformDependency platformDependency = flectonePulsePlugin.getDependencyResolver();
        platformDependency.getLibraryManager().loadLibrary(platformDependency.buildLibrary(
                "com.discord4j", "discord4j-core", BuildConfig.DISCORD4J_VERSION, true, null, null)
                .build()
        );
        platformDependency.getLibraryManager().loadLibrary(platformDependency.buildLibrary(
                "io.netty", "netty-resolver-dns", "5.0.0.Alpha2", true, null, null)
                .build()
        );
        platformDependency.getLibraryManager().loadLibrary(platformDependency.buildLibrary(
                "com.discord4j", "discord4j-common", "3.3.0-RC1", true, null, null)
                .build()
        );

        isLoaded = true;
    }

    public void sendMessage(FEntity sender, MessageTag messageTag, UnaryOperator<String> discordString) {
        if (checkModulePredicates(sender)) return;

        injector.getInstance(DiscordIntegration.class).sendMessage(sender, messageTag, discordString);
    }

    public void disconnect() {
        if (!isLoaded) return;

        injector.getInstance(DiscordIntegration.class).disconnect();
    }
}
