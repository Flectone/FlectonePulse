package net.flectone.pulse.module.integration;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.file.FileFacade;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.value.ExternalModeration;
import net.flectone.pulse.module.ModuleSimple;
import net.flectone.pulse.module.integration.deepl.DeeplModule;
import net.flectone.pulse.module.integration.luckperms.LuckPermsModule;
import net.flectone.pulse.module.integration.placeholderapi.HytalePlaceholderAPIModule;
import net.flectone.pulse.module.integration.yandex.YandexModule;
import net.flectone.pulse.platform.adapter.PlatformServerAdapter;
import net.flectone.pulse.platform.controller.ModuleController;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.util.LazyInstance;
import net.kyori.adventure.text.Component;
import org.jspecify.annotations.NonNull;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

@Singleton
public class HytaleIntegrationModule extends IntegrationModuleImpl {

    private final LazyInstance<PlatformServerAdapter> platformServerAdapter;

    @Inject
    public HytaleIntegrationModule(FileFacade fileFacade,
                                   LazyInstance<PlatformServerAdapter> platformServerAdapter,
                                   ListenerRegistry listenerRegistry,
                                   ModuleController moduleController,
                                   LazyInstance<LuckPermsModule> luckPermsModule,
                                   LazyInstance<DeeplModule> deeplModule,
                                   LazyInstance<YandexModule> yandexModule) {
        super(fileFacade, platformServerAdapter, listenerRegistry, moduleController,
                luckPermsModule, deeplModule, yandexModule);

        this.platformServerAdapter = platformServerAdapter;
    }

    @Override
    public Set<@NonNull Class<? extends ModuleSimple>> children() {
        Set<@NonNull Class<? extends ModuleSimple>> builder = new LinkedHashSet<>(super.children());

        if (platformServerAdapter.get().hasProject("HelpChat:PlaceholderAPI")) {
            builder.add(HytalePlaceholderAPIModule.class);
        }

        return Collections.unmodifiableSet(builder);
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
    public boolean hasVanishIntegration() {
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