package net.flectone.pulse.module.integration;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.util.ExternalModeration;
import net.flectone.pulse.module.AbstractModule;
import net.flectone.pulse.module.integration.placeholderapi.HytalePlaceholderAPIModule;
import net.flectone.pulse.platform.adapter.PlatformServerAdapter;
import net.flectone.pulse.util.file.FileFacade;
import net.kyori.adventure.text.Component;
import org.jspecify.annotations.NonNull;

@Singleton
public class HytaleIntegrationModule extends IntegrationModule {

    private final PlatformServerAdapter platformServerAdapter;

    @Inject
    public HytaleIntegrationModule(FileFacade fileFacade,
                                   PlatformServerAdapter platformServerAdapter,
                                   Injector injector) {
        super(fileFacade, platformServerAdapter, injector);

        this.platformServerAdapter = platformServerAdapter;
    }

    @Override
    public ImmutableList.Builder<@NonNull Class<? extends AbstractModule>> childrenBuilder() {
        ImmutableList.Builder<@NonNull Class<? extends AbstractModule>> builder = super.childrenBuilder();

        if (platformServerAdapter.hasProject("HelpChat:PlaceholderAPI")) {
            builder.add(HytalePlaceholderAPIModule.class);
        }

        return builder;
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
