package net.flectone.pulse.module.integration;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.util.ExternalModeration;
import net.flectone.pulse.platform.adapter.PlatformServerAdapter;
import net.flectone.pulse.util.file.FileFacade;
import net.kyori.adventure.text.Component;

@Singleton
public class HytaleIntegrationModule extends IntegrationModule {

    @Inject
    public HytaleIntegrationModule(FileFacade fileFacade,
                                   PlatformServerAdapter platformServerAdapter,
                                   Injector injector) {
        super(fileFacade, platformServerAdapter, injector);
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
    public boolean sendMessageWithInteractiveChat(FEntity fReceiver, Component message) {
        return false;
    }

    @Override
    public boolean isMuted(FPlayer fPlayer) {
        return false;
    }

    @Override
    public boolean isBedrockPlayer(FEntity fPlayer) {
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
