package net.flectone.pulse.module.integration;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Injector;
import com.google.inject.Provider;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.ModuleSimple;
import net.flectone.pulse.module.integration.floodgate.MinecraftFloodgateModule;
import net.flectone.pulse.module.integration.geyser.MinecraftGeyserModule;
import net.flectone.pulse.module.integration.minimotd.MinecraftMiniMOTDModule;
import net.flectone.pulse.module.integration.plasmovoice.MinecraftPlasmoVoiceModule;
import net.flectone.pulse.module.integration.simplevoice.MinecraftSimpleVoiceModule;
import net.flectone.pulse.module.integration.skinsrestorer.MinecraftSkinsRestorerModule;
import net.flectone.pulse.platform.adapter.PlatformServerAdapter;
import net.flectone.pulse.platform.controller.ModuleController;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.processing.resolver.ReflectionResolver;
import net.flectone.pulse.util.constant.PlatformType;
import net.flectone.pulse.util.file.FileFacade;
import net.flectone.pulse.util.logging.FLogger;
import net.kyori.adventure.text.object.PlayerHeadObjectContents;
import org.jspecify.annotations.NonNull;

public abstract class MinecraftIntegrationModule extends IntegrationModule {

    private final Provider<PlatformServerAdapter> platformServerAdapterProvider;
    private final ReflectionResolver reflectionResolver;
    private final ModuleController moduleController;
    private final FLogger fLogger;
    private final Injector injector;

    protected MinecraftIntegrationModule(FileFacade fileFacade,
                                         FLogger fLogger,
                                         Provider<PlatformServerAdapter> platformServerAdapterProvider,
                                         ReflectionResolver reflectionResolver,
                                         ListenerRegistry listenerRegistry,
                                         ModuleController moduleController,
                                         Injector injector) {
        super(fileFacade, platformServerAdapterProvider, listenerRegistry, moduleController, injector);

        this.platformServerAdapterProvider = platformServerAdapterProvider;
        this.reflectionResolver = reflectionResolver;
        this.moduleController = moduleController;
        this.fLogger = fLogger;
        this.injector = injector;
    }

    @Override
    public ImmutableSet.Builder<@NonNull Class<? extends ModuleSimple>> childrenBuilder() {
        ImmutableSet.Builder<@NonNull Class<? extends ModuleSimple>> builder = super.childrenBuilder();

        PlatformServerAdapter platformServerAdapter = platformServerAdapterProvider.get();
        if (platformServerAdapter.hasProject("SkinsRestorer")) {
            builder.add(MinecraftSkinsRestorerModule.class);
        }

        if (platformServerAdapter.getPlatformType() == PlatformType.FABRIC
                ? platformServerAdapter.hasProject("minimotd-fabric")
                : platformServerAdapter.hasProject("MiniMOTD")) {
            builder.add(MinecraftMiniMOTDModule.class);
        }

        if (platformServerAdapter.hasProject("voicechat")) {
            builder.add(MinecraftSimpleVoiceModule.class);
        }

        if (platformServerAdapter.hasProject("PlasmoVoice")) {
            if (reflectionResolver.hasClass("su.plo.voice.api.server.event.audio.source.ServerSourceCreatedEvent")) {
                builder.add(MinecraftPlasmoVoiceModule.class);
            } else {
                fLogger.warning("Update PlasmoVoice to the latest version");
            }
        }

        if (platformServerAdapter.hasProject("floodgate")) {
            builder.add(MinecraftFloodgateModule.class);
        }

        if (platformServerAdapter.getPlatformType() == PlatformType.FABRIC
                ? platformServerAdapter.hasProject("geyser-fabric")
                : platformServerAdapter.hasProject("Geyser-Spigot")) {
            if (reflectionResolver.hasClass("org.geysermc.geyser.api.GeyserApi")) {
                builder.add(MinecraftGeyserModule.class);
            } else {
                fLogger.warning("Geyser hook is failed, check that Geyser is turned on and working");
            }
        }

        return builder;
    }

    public boolean isBedrockPlayer(FEntity fPlayer) {
        if (!moduleController.isEnable(this)) return false;

        // bedrock players use a nil uuid bit masked with their xbox user id (xuid). The version is always zero.
        // https://github.com/ocelotpotpie/FreedomChat/blob/main/paper/src/main/java/ru/bk/oharass/freedomchat/FreedomHandler.java#L111
        if (fPlayer.uuid().version() == 0) return true;

        if (containsEnabledChild(MinecraftFloodgateModule.class)) {
            return injector.getInstance(MinecraftFloodgateModule.class).isBedrockPlayer(fPlayer);
        }

        if (containsEnabledChild(MinecraftGeyserModule.class)) {
            return injector.getInstance(MinecraftGeyserModule.class).isBedrockPlayer(fPlayer);
        }

        return false;
    }

    public String getTextureUrl(FEntity sender) {
        if (!moduleController.isEnable(this)) return null;
        if (!containsEnabledChild(MinecraftSkinsRestorerModule.class)) return null;
        if (!(sender instanceof FPlayer fPlayer)) return null;

        return injector.getInstance(MinecraftSkinsRestorerModule.class).getTextureUrl(fPlayer);
    }

    public PlayerHeadObjectContents.ProfileProperty getProfileProperty(FEntity sender) {
        if (!moduleController.isEnable(this)) return null;
        if (!containsEnabledChild(MinecraftSkinsRestorerModule.class)) return null;
        if (!(sender instanceof FPlayer fPlayer)) return null;

        return injector.getInstance(MinecraftSkinsRestorerModule.class).getProfileProperty(fPlayer);
    }

}
