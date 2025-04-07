package net.flectone.pulse.module.integration;

import com.google.inject.Injector;
import net.flectone.pulse.configuration.Integration;
import net.flectone.pulse.configuration.Permission;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.model.ExternalModeration;
import net.flectone.pulse.model.FEntity;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.module.AbstractModule;
import net.flectone.pulse.module.integration.deepl.DeeplModule;
import net.flectone.pulse.module.integration.discord.DiscordModule;
import net.flectone.pulse.module.integration.telegram.TelegramModule;
import net.flectone.pulse.module.integration.twitch.TwitchModule;
import net.flectone.pulse.module.integration.yandex.YandexModule;
import net.flectone.pulse.util.MessageTag;

import java.util.Set;
import java.util.function.UnaryOperator;

public abstract class IntegrationModule extends AbstractModule {

    private final Integration integration;
    private final Permission.Integration permission;
    private final Injector injector;

    public IntegrationModule(FileManager fileManager,
                             Injector injector) {
        this.injector = injector;

        integration = fileManager.getIntegration();
        permission = fileManager.getPermission().getIntegration();

        addChildren(DeeplModule.class);
        addChildren(DiscordModule.class);
        addChildren(TelegramModule.class);
        addChildren(TwitchModule.class);
        addChildren(YandexModule.class);
    }

    @Override
    public void reload() {
        registerModulePermission(permission);
    }

    @Override
    public boolean isConfigEnable() {
        return integration.isEnable();
    }

    public abstract String checkMention(FEntity fPlayer, String message);

    public abstract String markSender(FEntity sender, String message);

    public abstract String setPlaceholders(FEntity sender, FEntity receiver, String message, boolean permission);

    public abstract boolean hasFPlayerPermission(FPlayer fPlayer, String permission);

    public abstract String getPrefix(FPlayer fPlayer);

    public abstract String getSuffix(FPlayer fPlayer);

    public abstract Set<String> getGroups();

    public abstract int getGroupWeight(FPlayer fPlayer);

    public abstract String getTextureUrl(FEntity sender);

    public abstract void sendMessage(FEntity sender, MessageTag messageTag, UnaryOperator<String> discordString);

    public abstract boolean hasMessenger();

    public abstract boolean isVanished(FEntity sender);

    public abstract boolean isMuted(FPlayer fPlayer);

    public abstract ExternalModeration getMute(FPlayer fPlayer);

    public abstract String getTritonLocale(FPlayer fPlayer);

    public String deeplTranslate(FPlayer sender, String source, String target, String text) {
        if (checkModulePredicates(sender)) return text;
        if (getChildren().contains(DeeplModule.class)) {
            return injector.getInstance(DeeplModule.class).translate(sender, source, target, text);
        }

        return text;
    }

    public String yandexTranslate(FPlayer sender, String source, String target, String text) {
        if (checkModulePredicates(sender)) return text;
        if (getChildren().contains(YandexModule.class)) {
            return injector.getInstance(YandexModule.class).translate(sender, source, target, text);
        }

        return text;
    }
}
