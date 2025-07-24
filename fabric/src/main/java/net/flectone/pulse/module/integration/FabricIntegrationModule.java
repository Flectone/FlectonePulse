package net.flectone.pulse.module.integration;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import net.flectone.pulse.adapter.PlatformServerAdapter;
import net.flectone.pulse.model.ExternalModeration;
import net.flectone.pulse.model.FEntity;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.module.integration.placeholderapi.PlaceholderAPIModule;
import net.flectone.pulse.resolver.FileResolver;

@Singleton
public class FabricIntegrationModule extends IntegrationModule {

    @Inject
    public FabricIntegrationModule(FileResolver fileManager,
                                   PlatformServerAdapter platformServerAdapter,
                                   Injector injector) {
        super(fileManager, platformServerAdapter, injector);

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
        return false;
    }

    @Override
    public boolean hasSeeVanishPermission(FEntity sender) {
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
