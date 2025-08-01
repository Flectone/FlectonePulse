package net.flectone.pulse.module.integration;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import net.flectone.pulse.platform.adapter.PlatformServerAdapter;
import net.flectone.pulse.util.checker.PermissionChecker;
import net.flectone.pulse.model.util.ExternalModeration;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.integration.placeholderapi.PlaceholderAPIModule;
import net.flectone.pulse.module.integration.supervanish.VanishModule;
import net.flectone.pulse.processing.resolver.FileResolver;
import net.flectone.pulse.util.logging.FLogger;

@Singleton
public class FabricIntegrationModule extends IntegrationModule {

    private final PermissionChecker permissionChecker;
    private final Injector injector;

    @Inject
    public FabricIntegrationModule(FileResolver fileManager,
                                   FLogger fLogger,
                                   PlatformServerAdapter platformServerAdapter,
                                   PermissionChecker permissionChecker,
                                   Injector injector) {
        super(fileManager, fLogger, platformServerAdapter, injector);

        this.permissionChecker = permissionChecker;
        this.injector = injector;

        if (platformServerAdapter.hasProject("melius-vanish")) {
            addChildren(VanishModule.class);
        }

        if (platformServerAdapter.hasProject("placeholder-api")) {
            addChildren(PlaceholderAPIModule.class);
        }
    }

    @Override
    public String checkMention(FEntity fPlayer, String message) {
        return message;
    }

    @Override
    public boolean isVanished(FEntity sender) {
        if (getChildren().contains(VanishModule.class)) {
            return injector.getInstance(VanishModule.class).isVanished(sender);
        }

        return false;
    }

    @Override
    public boolean hasSeeVanishPermission(FEntity sender) {
        return permissionChecker.check(sender, "vanish.feature.view");
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
