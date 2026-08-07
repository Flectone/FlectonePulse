package net.flectone.pulse.module.integration;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.util.ExternalModeration;
import net.flectone.pulse.module.ModuleSimple;
import net.flectone.pulse.module.integration.deepl.DeeplModule;
import net.flectone.pulse.module.integration.floodgate.MinecraftFloodgateModule;
import net.flectone.pulse.module.integration.geyser.MinecraftGeyserModule;
import net.flectone.pulse.module.integration.luckperms.LuckPermsModule;
import net.flectone.pulse.module.integration.miniplaceholders.FabricMiniPlaceholdersModule;
import net.flectone.pulse.module.integration.placeholderapi.FabricPlaceholderAPIModule;
import net.flectone.pulse.module.integration.skinsrestorer.MinecraftSkinsRestorerModule;
import net.flectone.pulse.module.integration.supervanish.FabricVanishModule;
import net.flectone.pulse.module.integration.yandex.YandexModule;
import net.flectone.pulse.platform.adapter.PlatformServerAdapter;
import net.flectone.pulse.platform.controller.ModuleController;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.processing.resolver.ReflectionResolver;
import net.flectone.pulse.util.LazyInstance;
import net.flectone.pulse.util.checker.PermissionChecker;
import net.flectone.pulse.util.constant.ModuleName;
import net.flectone.pulse.util.file.FileFacade;
import net.flectone.pulse.util.logging.FLogger;
import net.kyori.adventure.text.Component;
import org.jspecify.annotations.NonNull;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

@Singleton
public class FabricIntegrationModule extends MinecraftIntegrationModule {

    private final LazyInstance<PermissionChecker> permissionChecker;
    private final LazyInstance<PlatformServerAdapter> platformServerAdapter;
    private final LazyInstance<FabricVanishModule> vanishModule;

    @Inject
    public FabricIntegrationModule(FileFacade fileManager,
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
                                   LazyInstance<MinecraftSkinsRestorerModule> skinsRestorerModule,
                                   LazyInstance<FabricVanishModule> vanishModule) {
        super(fileManager, fLogger, platformServerAdapter, reflectionResolver, listenerRegistry, moduleController,
                luckPermsModule, deeplModule, yandexModule, floodgateModule, geyserModule, skinsRestorerModule);

        this.permissionChecker = permissionChecker;
        this.platformServerAdapter = platformServerAdapter;
        this.vanishModule = vanishModule;
    }

    @Override
    public Set<@NonNull Class<? extends ModuleSimple>> children() {
        Set<@NonNull Class<? extends ModuleSimple>> builder = new LinkedHashSet<>(super.children());

        PlatformServerAdapter platformServerAdapterInstance = platformServerAdapter.get();
        if (platformServerAdapterInstance.hasProject("melius-vanish")) {
            builder.add(FabricVanishModule.class);
        }

        if (platformServerAdapterInstance.hasProject("MiniPlaceholders")) {
            builder.add(FabricMiniPlaceholdersModule.class);
        }

        if (platformServerAdapterInstance.hasProject("placeholder-api")) {
            builder.add(FabricPlaceholderAPIModule.class);
        }

        return Collections.unmodifiableSet(builder);
    }

    @Override
    public String checkMention(FEntity fPlayer, String message) {
        return message;
    }

    @Override
    public boolean isVanished(FEntity sender) {
        if (containsEnabledChild(ModuleName.INTEGRATION_SUPERVANISH)) {
            return vanishModule.get().isVanished(sender);
        }

        return false;
    }

    @Override
    public boolean hasVanishIntegration() {
        if (containsEnabledChild(ModuleName.INTEGRATION_SUPERVANISH)) {
            return vanishModule.get().isHooked();
        }

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