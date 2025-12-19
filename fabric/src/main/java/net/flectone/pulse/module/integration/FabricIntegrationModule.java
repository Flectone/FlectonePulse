package net.flectone.pulse.module.integration;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import net.flectone.pulse.module.integration.miniplaceholders.MiniPlaceholdersModule;
import net.flectone.pulse.platform.adapter.PlatformServerAdapter;
import net.flectone.pulse.processing.resolver.ReflectionResolver;
import net.flectone.pulse.util.checker.PermissionChecker;
import net.flectone.pulse.model.util.ExternalModeration;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.integration.placeholderapi.PlaceholderAPIModule;
import net.flectone.pulse.module.integration.supervanish.VanishModule;
import net.flectone.pulse.util.file.FileFacade;
import net.flectone.pulse.util.logging.FLogger;
import net.kyori.adventure.text.Component;

@Singleton
public class FabricIntegrationModule extends IntegrationModule {

    private final Provider<PermissionChecker> permissionCheckerProvider;
    private final PlatformServerAdapter platformServerAdapter;
    private final Injector injector;

    @Inject
    public FabricIntegrationModule(FileFacade fileManager,
                                   FLogger fLogger,
                                   PlatformServerAdapter platformServerAdapter,
                                   Provider<PermissionChecker> permissionCheckerProvider,
                                   ReflectionResolver reflectionResolver,
                                   Injector injector) {
        super(fileManager, fLogger, platformServerAdapter, reflectionResolver, injector);

        this.permissionCheckerProvider = permissionCheckerProvider;
        this.platformServerAdapter = platformServerAdapter;
        this.injector = injector;
    }

    @Override
    public void configureChildren() {
        super.configureChildren();

        if (platformServerAdapter.hasProject("melius-vanish")) {
            addChild(VanishModule.class);
        }

        if (platformServerAdapter.hasProject("MiniPlaceholders")) {
            addChild(MiniPlaceholdersModule.class);
        }

        if (platformServerAdapter.hasProject("placeholder-api")) {
            addChild(PlaceholderAPIModule.class);
        }
    }

    @Override
    public String checkMention(FEntity fPlayer, String message) {
        return message;
    }

    @Override
    public boolean isVanished(FEntity sender) {
        if (containsChild(VanishModule.class)) {
            return injector.getInstance(VanishModule.class).isVanished(sender);
        }

        return false;
    }

    @Override
    public boolean hasSeeVanishPermission(FEntity sender) {
        return permissionCheckerProvider.get().check(sender, "vanish.feature.view");
    }

    @Override
    public boolean sendMessageWithInteractiveChat(FEntity fReceiver, Component message) {
        return false;
    }

    @Override
    public boolean isMuted(FPlayer fPlayer) {
        return false;
    }

    @Override
    public ExternalModeration getMute(FPlayer fPlayer) {
        return null;
    }

    @Override
    public String getTritonLocale(FPlayer fPlayer) {
        return null;
    }
}
