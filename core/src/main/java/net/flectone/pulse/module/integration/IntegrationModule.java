package net.flectone.pulse.module.integration;

import com.google.common.collect.ImmutableList;
import com.google.inject.Injector;
import lombok.NonNull;
import net.flectone.pulse.config.Integration;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.util.ExternalModeration;
import net.flectone.pulse.module.AbstractModule;
import net.flectone.pulse.module.integration.deepl.DeeplModule;
import net.flectone.pulse.module.integration.discord.DiscordModule;
import net.flectone.pulse.module.integration.floodgate.FloodgateModule;
import net.flectone.pulse.module.integration.geyser.GeyserModule;
import net.flectone.pulse.module.integration.luckperms.LuckPermsModule;
import net.flectone.pulse.module.integration.plasmovoice.PlasmoVoiceModule;
import net.flectone.pulse.module.integration.simplevoice.SimpleVoiceModule;
import net.flectone.pulse.module.integration.skinsrestorer.SkinsRestorerModule;
import net.flectone.pulse.module.integration.telegram.TelegramModule;
import net.flectone.pulse.module.integration.twitch.TwitchModule;
import net.flectone.pulse.module.integration.yandex.YandexModule;
import net.flectone.pulse.platform.adapter.PlatformServerAdapter;
import net.flectone.pulse.processing.resolver.ReflectionResolver;
import net.flectone.pulse.util.constant.MessageType;
import net.flectone.pulse.util.file.FileFacade;
import net.flectone.pulse.util.logging.FLogger;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.object.PlayerHeadObjectContents;

import java.util.Collections;
import java.util.Set;
import java.util.function.UnaryOperator;

public abstract class IntegrationModule extends AbstractModule {

    private final FileFacade fileFacade;
    private final FLogger fLogger;
    private final PlatformServerAdapter platformServerAdapter;
    private final ReflectionResolver reflectionResolver;
    private final Injector injector;

    protected IntegrationModule(FileFacade fileFacade,
                                FLogger fLogger,
                                PlatformServerAdapter platformServerAdapter,
                                ReflectionResolver reflectionResolver,
                                Injector injector) {
        this.fileFacade = fileFacade;
        this.fLogger = fLogger;
        this.platformServerAdapter = platformServerAdapter;
        this.reflectionResolver = reflectionResolver;
        this.injector = injector;
    }

    @Override
    public ImmutableList.Builder<@NonNull Class<? extends AbstractModule>> childrenBuilder() {
        ImmutableList.Builder<@NonNull Class<? extends AbstractModule>> builder = super.childrenBuilder();

        if (platformServerAdapter.hasProject("SkinsRestorer")) {
            builder.add(SkinsRestorerModule.class);
        }

        if (platformServerAdapter.hasProject("LuckPerms")) {
            builder.add(LuckPermsModule.class);
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

    @Override
    public Integration config() {
        return fileFacade.integration();
    }

    @Override
    public Permission.Integration permission() {
        return fileFacade.permission().integration();
    }

    public abstract String checkMention(FEntity fPlayer, String message);

    public abstract boolean isVanished(FEntity sender);

    public abstract boolean hasSeeVanishPermission(FEntity sender);

    public abstract boolean sendMessageWithInteractiveChat(FEntity fReceiver, Component message);

    public abstract boolean isMuted(FPlayer fPlayer);

    public abstract ExternalModeration getMute(FPlayer fPlayer);

    public abstract String getTritonLocale(FPlayer fPlayer);

    public <T> T getInstance(Class<T> clazz) {
        return injector.getInstance(clazz);
    }

    public boolean isBedrockPlayer(FEntity fPlayer) {
        if (!isEnable()) return false;

        if (containsChild(FloodgateModule.class)) {
            return injector.getInstance(FloodgateModule.class).isBedrockPlayer(fPlayer);
        }

        if (containsChild(GeyserModule.class)) {
            return injector.getInstance(GeyserModule.class).isBedrockPlayer(fPlayer);
        }

        return false;
    }

    public boolean hasFPlayerPermission(FPlayer fPlayer, String permission) {
        if (!isEnable()) return false;

        if (containsChild(LuckPermsModule.class)) {
            return injector.getInstance(LuckPermsModule.class).hasLuckPermission(fPlayer, permission);
        }

        return false;
    }

    public String getTextureUrl(FEntity sender) {
        if (!isEnable()) return null;
        if (!containsChild(SkinsRestorerModule.class)) return null;
        if (!(sender instanceof FPlayer fPlayer)) return null;

        return injector.getInstance(SkinsRestorerModule.class).getTextureUrl(fPlayer);
    }

    public PlayerHeadObjectContents.ProfileProperty getProfileProperty(FEntity sender) {
        if (!isEnable()) return null;
        if (!containsChild(SkinsRestorerModule.class)) return null;
        if (!(sender instanceof FPlayer fPlayer)) return null;

        return injector.getInstance(SkinsRestorerModule.class).getProfileProperty(fPlayer);
    }

    public String getPrefix(FPlayer fPlayer) {
        if (!isEnable()) return null;

        if (containsChild(LuckPermsModule.class)) {
            return injector.getInstance(LuckPermsModule.class).getPrefix(fPlayer);
        }

        return null;
    }

    public String getSuffix(FPlayer fPlayer) {
        if (!isEnable()) return null;

        if (containsChild(LuckPermsModule.class)) {
            return injector.getInstance(LuckPermsModule.class).getSuffix(fPlayer);
        }

        return null;
    }

    public Set<String> getGroups() {
        if (!isEnable()) return Collections.emptySet();

        if (containsChild(LuckPermsModule.class)) {
            return injector.getInstance(LuckPermsModule.class).getGroups();
        }

        return Collections.emptySet();
    }

    public int getGroupWeight(FPlayer fPlayer) {
        if (!isEnable()) return 0;
        if (!containsChild(LuckPermsModule.class)) return 0;

        return injector.getInstance(LuckPermsModule.class).getGroupWeight(fPlayer);
    }

    public void sendMessage(FEntity sender, String messageName, UnaryOperator<String> discordString) {
        if (containsChild(DiscordModule.class) && !MessageType.FROM_DISCORD_TO_MINECRAFT.name().equals(messageName)) {
            injector.getInstance(DiscordModule.class).sendMessage(sender, messageName, discordString);
        }

        if (containsChild(TwitchModule.class) && !MessageType.FROM_TWITCH_TO_MINECRAFT.name().equals(messageName)) {
            injector.getInstance(TwitchModule.class).sendMessage(sender, messageName, discordString);
        }

        if (containsChild(TelegramModule.class) && !MessageType.FROM_TELEGRAM_TO_MINECRAFT.name().equals(messageName)) {
            injector.getInstance(TelegramModule.class).sendMessage(sender, messageName, discordString);
        }
    }

    public boolean hasMessenger() {
        return injector.getInstance(DiscordModule.class).isEnable()
                || injector.getInstance(TwitchModule.class).isEnable()
                || injector.getInstance(TelegramModule.class).isEnable();
    }

    public boolean canSeeVanished(FEntity fTarget, FEntity fViewer) {
        if (fTarget.equals(fViewer)) return true;

        boolean isVanished = isVanished(fTarget);
        return !isVanished || hasSeeVanishPermission(fViewer);
    }

    public String deeplTranslate(FPlayer sender, String source, String target, String text) {
        if (isModuleDisabledFor(sender)) return text;
        if (containsChild(DeeplModule.class)) {
            return injector.getInstance(DeeplModule.class).translate(sender, source, target, text);
        }

        return text;
    }

    public String yandexTranslate(FPlayer sender, String source, String target, String text) {
        if (isModuleDisabledFor(sender)) return text;
        if (containsChild(YandexModule.class)) {
            return injector.getInstance(YandexModule.class).translate(sender, source, target, text);
        }

        return text;
    }
}
