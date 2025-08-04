package net.flectone.pulse.module.integration;

import com.google.inject.Injector;
import net.flectone.pulse.platform.adapter.PlatformServerAdapter;
import net.flectone.pulse.config.Integration;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.util.ExternalModeration;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.AbstractModule;
import net.flectone.pulse.module.integration.deepl.DeeplModule;
import net.flectone.pulse.module.integration.discord.DiscordModule;
import net.flectone.pulse.module.integration.luckperms.LuckPermsModule;
import net.flectone.pulse.module.integration.plasmovoice.PlasmoVoiceModule;
import net.flectone.pulse.module.integration.simplevoice.SimpleVoiceModule;
import net.flectone.pulse.module.integration.skinsrestorer.SkinsRestorerModule;
import net.flectone.pulse.module.integration.telegram.TelegramModule;
import net.flectone.pulse.module.integration.twitch.TwitchModule;
import net.flectone.pulse.module.integration.yandex.YandexModule;
import net.flectone.pulse.processing.resolver.FileResolver;
import net.flectone.pulse.processing.resolver.ReflectionResolver;
import net.flectone.pulse.util.constant.MessageType;
import net.flectone.pulse.util.logging.FLogger;

import java.util.Collections;
import java.util.Set;
import java.util.function.UnaryOperator;

public abstract class IntegrationModule extends AbstractModule {

    private final Integration integration;
    private final Permission.Integration permission;
    private final Injector injector;

    protected IntegrationModule(FileResolver fileResolver,
                                FLogger fLogger,
                                PlatformServerAdapter platformServerAdapter,
                                ReflectionResolver reflectionResolver,
                                Injector injector) {
        this.integration = fileResolver.getIntegration();
        this.permission = fileResolver.getPermission().getIntegration();
        this.injector = injector;

        if (platformServerAdapter.hasProject("SkinsRestorer")) {
            addChildren(SkinsRestorerModule.class);
        }

        if (platformServerAdapter.hasProject("LuckPerms")) {
            addChildren(LuckPermsModule.class);
        }

        if (platformServerAdapter.hasProject("VoiceChat")) {
            addChildren(SimpleVoiceModule.class);
        }

        if (platformServerAdapter.hasProject("PlasmoVoice")) {
            if (reflectionResolver.hasClass("su.plo.voice.api.server.event.audio.source.ServerSourceCreatedEvent")) {
                addChildren(PlasmoVoiceModule.class);
            } else {
                fLogger.warning("Update PlasmoVoice to the latest version");
            }
        }
    }

    @Override
    public void onEnable() {
        registerModulePermission(permission);

        addChildren(DeeplModule.class);
        addChildren(DiscordModule.class);
        addChildren(TelegramModule.class);
        addChildren(TwitchModule.class);
        addChildren(YandexModule.class);
    }

    @Override
    protected boolean isConfigEnable() {
        return integration.isEnable();
    }

    public abstract String checkMention(FEntity fPlayer, String message);

    public abstract boolean isVanished(FEntity sender);

    public abstract boolean hasSeeVanishPermission(FEntity sender);

    public abstract boolean isMuted(FPlayer fPlayer);

    public abstract ExternalModeration getMute(FPlayer fPlayer);

    public abstract String getTritonLocale(FPlayer fPlayer);

    public boolean hasFPlayerPermission(FPlayer fPlayer, String permission) {
        if (!isEnable()) return false;

        if (getChildren().contains(LuckPermsModule.class)) {
            return injector.getInstance(LuckPermsModule.class).hasLuckPermission(fPlayer, permission);
        }

        return false;
    }

    public String getTextureUrl(FEntity sender) {
        if (!isEnable()) return null;
        if (!getChildren().contains(SkinsRestorerModule.class)) return null;
        if (!(sender instanceof FPlayer fPlayer)) return null;

        return injector.getInstance(SkinsRestorerModule.class).getTextureUrl(fPlayer);
    }

    public String getPrefix(FPlayer fPlayer) {
        if (!isEnable()) return null;

        if (getChildren().contains(LuckPermsModule.class)) {
            return injector.getInstance(LuckPermsModule.class).getPrefix(fPlayer);
        }

        return null;
    }

    public String getSuffix(FPlayer fPlayer) {
        if (!isEnable()) return null;

        if (getChildren().contains(LuckPermsModule.class)) {
            return injector.getInstance(LuckPermsModule.class).getSuffix(fPlayer);
        }

        return null;
    }

    public Set<String> getGroups() {
        if (!isEnable()) return Collections.emptySet();

        if (getChildren().contains(LuckPermsModule.class)) {
            return injector.getInstance(LuckPermsModule.class).getGroups();
        }

        return Collections.emptySet();
    }

    public int getGroupWeight(FPlayer fPlayer) {
        if (!isEnable()) return 0;
        if (!getChildren().contains(LuckPermsModule.class)) return 0;

        return injector.getInstance(LuckPermsModule.class).getGroupWeight(fPlayer);
    }

    public void sendMessage(FEntity sender, MessageType messageType, UnaryOperator<String> discordString) {
        if (getChildren().contains(DiscordModule.class)) {
            injector.getInstance(DiscordModule.class).sendMessage(sender, messageType, discordString);
        }

        if (getChildren().contains(TwitchModule.class)) {
            injector.getInstance(TwitchModule.class).sendMessage(sender, messageType, discordString);
        }

        if (getChildren().contains(TelegramModule.class)) {
            injector.getInstance(TelegramModule.class).sendMessage(sender, messageType, discordString);
        }
    }

    public boolean hasMessenger() {
        return injector.getInstance(DiscordModule.class).isEnable()
                || injector.getInstance(TwitchModule.class).isEnable()
                || injector.getInstance(TelegramModule.class).isEnable();
    }

    public boolean isVanishedVisible(FEntity sender, FEntity receiver) {
        boolean isVanished = isVanished(sender);
        return !isVanished || hasSeeVanishPermission(receiver);
    }

    public String deeplTranslate(FPlayer sender, String source, String target, String text) {
        if (isModuleDisabledFor(sender)) return text;
        if (getChildren().contains(DeeplModule.class)) {
            return injector.getInstance(DeeplModule.class).translate(sender, source, target, text);
        }

        return text;
    }

    public String yandexTranslate(FPlayer sender, String source, String target, String text) {
        if (isModuleDisabledFor(sender)) return text;
        if (getChildren().contains(YandexModule.class)) {
            return injector.getInstance(YandexModule.class).translate(sender, source, target, text);
        }

        return text;
    }
}
