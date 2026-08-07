package net.flectone.pulse.module.integration;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.util.ExternalModeration;
import net.flectone.pulse.module.integration.deepl.DeeplModule;
import net.flectone.pulse.module.integration.floodgate.MinecraftFloodgateModule;
import net.flectone.pulse.module.integration.geyser.MinecraftGeyserModule;
import net.flectone.pulse.module.integration.luckperms.LuckPermsModule;
import net.flectone.pulse.module.integration.skinsrestorer.MinecraftSkinsRestorerModule;
import net.flectone.pulse.module.integration.yandex.YandexModule;
import net.flectone.pulse.platform.adapter.PlatformServerAdapter;
import net.flectone.pulse.platform.controller.ModuleController;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.processing.resolver.ReflectionResolver;
import net.flectone.pulse.util.LazyInstance;
import net.flectone.pulse.util.checker.PermissionChecker;
import net.flectone.pulse.util.file.FileFacade;
import net.flectone.pulse.util.logging.FLogger;
import net.kyori.adventure.text.Component;

@Singleton
public class NeoForgeIntegrationModule extends MinecraftIntegrationModule {

    private final LazyInstance<PermissionChecker> permissionChecker;

    @Inject
    public NeoForgeIntegrationModule(FileFacade fileManager,
                                     FLogger fLogger,
                                     LazyInstance<PlatformServerAdapter> platformServerAdapter,
                                     LazyInstance<PermissionChecker> permissionChecker,
                                     ReflectionResolver reflectionResolver,
                                     ListenerRegistry listenerRegistry,
                                     ModuleController moduleController,
                                     LazyInstance<LuckPermsModule> luckPermsModule,
                                     LazyInstance<DeeplModule> deeplModule,
                                     LazyInstance<YandexModule> yandexModule,
                                     LazyInstance<MinecraftFloodgateModule> floodgateModule,
                                     LazyInstance<MinecraftGeyserModule> geyserModule,
                                     LazyInstance<MinecraftSkinsRestorerModule> skinsRestorerModule) {
        super(fileManager, fLogger, platformServerAdapter, reflectionResolver, listenerRegistry, moduleController,
                luckPermsModule, deeplModule, yandexModule, floodgateModule, geyserModule, skinsRestorerModule);

        this.permissionChecker = permissionChecker;
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
        return permissionChecker.get().check(sender, "vanish.feature.view");
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