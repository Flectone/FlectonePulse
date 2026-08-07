package net.flectone.pulse.module.integration;

import net.flectone.pulse.constant.ModuleName;
import net.flectone.pulse.constant.PlatformType;
import net.flectone.pulse.file.FileFacade;
import net.flectone.pulse.logging.FLogger;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.ModuleSimple;
import net.flectone.pulse.module.integration.deepl.DeeplModule;
import net.flectone.pulse.module.integration.floodgate.MinecraftFloodgateModule;
import net.flectone.pulse.module.integration.geyser.MinecraftGeyserModule;
import net.flectone.pulse.module.integration.luckperms.LuckPermsModule;
import net.flectone.pulse.module.integration.minimotd.MinecraftMiniMOTDModule;
import net.flectone.pulse.module.integration.plasmovoice.MinecraftPlasmoVoiceModule;
import net.flectone.pulse.module.integration.simplevoice.MinecraftSimpleVoiceModule;
import net.flectone.pulse.module.integration.skinsrestorer.MinecraftSkinsRestorerModule;
import net.flectone.pulse.module.integration.yandex.YandexModule;
import net.flectone.pulse.platform.adapter.PlatformServerAdapter;
import net.flectone.pulse.platform.controller.ModuleController;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.resolver.ReflectionResolver;
import net.flectone.pulse.util.LazyInstance;
import net.kyori.adventure.text.object.PlayerHeadObjectContents;
import org.jspecify.annotations.NonNull;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

public abstract class MinecraftIntegrationModule extends IntegrationModuleImpl {

    private final LazyInstance<PlatformServerAdapter> platformServerAdapter;
    private final ReflectionResolver reflectionResolver;
    private final ModuleController moduleController;
    private final FLogger fLogger;
    private final LazyInstance<MinecraftFloodgateModule> floodgateModule;
    private final LazyInstance<MinecraftGeyserModule> geyserModule;
    private final LazyInstance<MinecraftSkinsRestorerModule> skinsRestorerModule;

    protected MinecraftIntegrationModule(FileFacade fileFacade,
                                         FLogger fLogger,
                                         LazyInstance<PlatformServerAdapter> platformServerAdapter,
                                         ReflectionResolver reflectionResolver,
                                         ListenerRegistry listenerRegistry,
                                         ModuleController moduleController,
                                         LazyInstance<LuckPermsModule> luckPermsModule,
                                         LazyInstance<DeeplModule> deeplModule,
                                         LazyInstance<YandexModule> yandexModule,
                                         LazyInstance<MinecraftFloodgateModule> floodgateModule,
                                         LazyInstance<MinecraftGeyserModule> geyserModule,
                                         LazyInstance<MinecraftSkinsRestorerModule> skinsRestorerModule) {
        super(fileFacade, platformServerAdapter, listenerRegistry, moduleController,
                luckPermsModule, deeplModule, yandexModule);

        this.platformServerAdapter = platformServerAdapter;
        this.reflectionResolver = reflectionResolver;
        this.moduleController = moduleController;
        this.fLogger = fLogger;
        this.floodgateModule = floodgateModule;
        this.geyserModule = geyserModule;
        this.skinsRestorerModule = skinsRestorerModule;
    }

    @Override
    public Set<@NonNull Class<? extends ModuleSimple>> children() {
        Set<@NonNull Class<? extends ModuleSimple>> builder = new LinkedHashSet<>(super.children());

        PlatformServerAdapter platformServerAdapterInstance = platformServerAdapter.get();
        if (platformServerAdapterInstance.hasProject("SkinsRestorer")) {
            builder.add(MinecraftSkinsRestorerModule.class);
        }

        if (platformServerAdapterInstance.getPlatformType() == PlatformType.FABRIC
                ? platformServerAdapterInstance.hasProject("minimotd-fabric")
                : platformServerAdapterInstance.hasProject("MiniMOTD")) {
            builder.add(MinecraftMiniMOTDModule.class);
        }

        if (platformServerAdapterInstance.hasProject("voicechat")) {
            builder.add(MinecraftSimpleVoiceModule.class);
        }

        if (platformServerAdapterInstance.hasProject("PlasmoVoice")) {
            if (reflectionResolver.hasClass("su.plo.voice.api.server.event.audio.source.ServerSourceCreatedEvent")) {
                builder.add(MinecraftPlasmoVoiceModule.class);
            } else {
                fLogger.warning("Update PlasmoVoice to the latest version");
            }
        }

        if (platformServerAdapterInstance.hasProject("floodgate")) {
            builder.add(MinecraftFloodgateModule.class);
        }

        if (hasGeyser(platformServerAdapterInstance)) {
            if (reflectionResolver.hasClass("org.geysermc.geyser.api.GeyserApi")) {
                builder.add(MinecraftGeyserModule.class);
            } else {
                fLogger.warning("Geyser hook is failed, check that Geyser is turned on and working");
            }
        }

        return Collections.unmodifiableSet(builder);
    }

    private boolean hasGeyser(PlatformServerAdapter platformServerAdapter) {
        return switch (platformServerAdapter.getPlatformType()) {
            case FABRIC -> platformServerAdapter.hasProject("geyser-fabric");
            case NEOFORGE -> platformServerAdapter.hasProject("geyser_neoforge");
            default -> platformServerAdapter.hasProject("Geyser-Spigot");
        };
    }

    public boolean isBedrockPlayer(FEntity fPlayer) {
        if (!moduleController.isEnable(this)) return false;

        if (containsEnabledChild(ModuleName.INTEGRATION_FLOODGATE)) {
            return floodgateModule.get().isBedrockPlayer(fPlayer);
        }

        if (containsEnabledChild(ModuleName.INTEGRATION_GEYSER)) {
            return geyserModule.get().isBedrockPlayer(fPlayer);
        }

        // if Floodgate and Geyser are not installed on the server, let's try to check player by UUID
        // this does not always mean that the player is playing from bedrock, because he can first enter from bedrock and then from java
        // bedrock players use a nil uuid bit masked with their xbox user id (xuid) -> version is always zero
        return fPlayer.uuid().version() == 0;
    }

    public String getTextureUrl(FEntity sender) {
        if (!moduleController.isEnable(this)) return null;
        if (!containsEnabledChild(ModuleName.INTEGRATION_SKINSRESTORER)) return null;
        if (!(sender instanceof FPlayer fPlayer)) return null;

        return skinsRestorerModule.get().getTextureUrl(fPlayer);
    }

    public PlayerHeadObjectContents.ProfileProperty getProfileProperty(FEntity sender) {
        if (!moduleController.isEnable(this)) return null;
        if (!containsEnabledChild(ModuleName.INTEGRATION_SKINSRESTORER)) return null;
        if (!(sender instanceof FPlayer fPlayer)) return null;

        return skinsRestorerModule.get().getProfileProperty(fPlayer);
    }

}