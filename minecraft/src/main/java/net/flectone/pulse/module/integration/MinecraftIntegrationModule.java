package net.flectone.pulse.module.integration;

import com.google.common.collect.ImmutableList;
import com.google.inject.Injector;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.AbstractModule;
import net.flectone.pulse.module.integration.deepl.DeeplModule;
import net.flectone.pulse.module.integration.discord.DiscordModule;
import net.flectone.pulse.module.integration.floodgate.FloodgateModule;
import net.flectone.pulse.module.integration.geyser.GeyserModule;
import net.flectone.pulse.module.integration.minimotd.MiniMOTDModule;
import net.flectone.pulse.module.integration.plasmovoice.PlasmoVoiceModule;
import net.flectone.pulse.module.integration.simplevoice.SimpleVoiceModule;
import net.flectone.pulse.module.integration.skinsrestorer.SkinsRestorerModule;
import net.flectone.pulse.module.integration.telegram.TelegramModule;
import net.flectone.pulse.module.integration.twitch.TwitchModule;
import net.flectone.pulse.module.integration.yandex.YandexModule;
import net.flectone.pulse.platform.adapter.PlatformServerAdapter;
import net.flectone.pulse.processing.resolver.ReflectionResolver;
import net.flectone.pulse.util.file.FileFacade;
import net.flectone.pulse.util.logging.FLogger;
import net.kyori.adventure.text.object.PlayerHeadObjectContents;
import org.jspecify.annotations.NonNull;

public abstract class MinecraftIntegrationModule extends IntegrationModule {

    private final PlatformServerAdapter platformServerAdapter;
    private final ReflectionResolver reflectionResolver;
    private final FLogger fLogger;
    private final Injector injector;

    protected MinecraftIntegrationModule(FileFacade fileFacade,
                                         FLogger fLogger,
                                         PlatformServerAdapter platformServerAdapter,
                                         ReflectionResolver reflectionResolver,
                                         Injector injector) {
        super(fileFacade, platformServerAdapter, injector);

        this.platformServerAdapter = platformServerAdapter;
        this.reflectionResolver = reflectionResolver;
        this.fLogger = fLogger;
        this.injector = injector;
    }

    @Override
    public ImmutableList.Builder<@NonNull Class<? extends AbstractModule>> childrenBuilder() {
        ImmutableList.Builder<@NonNull Class<? extends AbstractModule>> builder = super.childrenBuilder();

        if (platformServerAdapter.hasProject("SkinsRestorer")) {
            builder.add(SkinsRestorerModule.class);
        }

        if (platformServerAdapter.hasProject("MiniMOTD")) {
            builder.add(MiniMOTDModule.class);
        }

        if (platformServerAdapter.hasProject("voicechat")) {
            builder.add(SimpleVoiceModule.class);
        }

        if (platformServerAdapter.hasProject("PlasmoVoice")) {
            if (reflectionResolver.hasClass("su.plo.voice.api.server.event.audio.source.ServerSourceCreatedEvent")) {
                builder.add(PlasmoVoiceModule.class);
            } else {
                fLogger.warning("Update PlasmoVoice to the latest version");
            }
        }

        if (platformServerAdapter.hasProject("floodgate")) {
            builder.add(FloodgateModule.class);
        }

        if (platformServerAdapter.hasProject("Geyser-Spigot") || platformServerAdapter.hasProject("geyser-fabric")) {
            if (reflectionResolver.hasClass("org.geysermc.geyser.api.GeyserApi")) {
                builder.add(GeyserModule.class);
            } else {
                fLogger.warning("Geyser hook is failed, check that Geyser is turned on and working");
            }
        }

        return builder.add(
                DeeplModule.class,
                DiscordModule.class,
                TelegramModule.class,
                TwitchModule.class,
                YandexModule.class
        );
    }

    public boolean isBedrockPlayer(FEntity fPlayer) {
        if (!isEnable()) return false;

        if (containsEnabledChild(FloodgateModule.class)) {
            return injector.getInstance(FloodgateModule.class).isBedrockPlayer(fPlayer);
        }

        if (containsEnabledChild(GeyserModule.class)) {
            return injector.getInstance(GeyserModule.class).isBedrockPlayer(fPlayer);
        }

        return false;
    }

    public String getTextureUrl(FEntity sender) {
        if (!isEnable()) return null;
        if (!containsEnabledChild(SkinsRestorerModule.class)) return null;
        if (!(sender instanceof FPlayer fPlayer)) return null;

        return injector.getInstance(SkinsRestorerModule.class).getTextureUrl(fPlayer);
    }

    public PlayerHeadObjectContents.ProfileProperty getProfileProperty(FEntity sender) {
        if (!isEnable()) return null;
        if (!containsEnabledChild(SkinsRestorerModule.class)) return null;
        if (!(sender instanceof FPlayer fPlayer)) return null;

        return injector.getInstance(SkinsRestorerModule.class).getProfileProperty(fPlayer);
    }

}
