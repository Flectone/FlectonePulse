package net.flectone.pulse.module.integration;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Injector;
import net.flectone.pulse.config.Integration;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.util.ExternalModeration;
import net.flectone.pulse.module.ModuleSimple;
import net.flectone.pulse.module.integration.deepl.DeeplModule;
import net.flectone.pulse.module.integration.discord.DiscordModule;
import net.flectone.pulse.module.integration.luckperms.LuckPermsModule;
import net.flectone.pulse.module.integration.telegram.TelegramModule;
import net.flectone.pulse.module.integration.twitch.TwitchModule;
import net.flectone.pulse.module.integration.yandex.YandexModule;
import net.flectone.pulse.platform.adapter.PlatformServerAdapter;
import net.flectone.pulse.platform.controller.ModuleController;
import net.flectone.pulse.util.constant.ModuleName;
import net.flectone.pulse.util.file.FileFacade;
import net.kyori.adventure.text.Component;
import org.jspecify.annotations.NonNull;

import java.util.Collections;
import java.util.Set;
import java.util.function.UnaryOperator;

public abstract class IntegrationModule implements ModuleSimple {

    private final FileFacade fileFacade;
    private final PlatformServerAdapter platformServerAdapter;
    private final ModuleController moduleController;
    private final Injector injector;

    protected IntegrationModule(FileFacade fileFacade,
                                PlatformServerAdapter platformServerAdapter,
                                ModuleController moduleController,
                                Injector injector) {
        this.fileFacade = fileFacade;
        this.platformServerAdapter = platformServerAdapter;
        this.moduleController = moduleController;
        this.injector = injector;
    }

    @Override
    public ImmutableSet.Builder<@NonNull Class<? extends ModuleSimple>> childrenBuilder() {
        ImmutableSet.Builder<@NonNull Class<? extends ModuleSimple>> builder = ModuleSimple.super.childrenBuilder();

        if (platformServerAdapter.hasProject("LuckPerms")) {
            builder.add(LuckPermsModule.class);
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
    public ModuleName name() {
        return ModuleName.INTEGRATION;
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

    public abstract boolean isBedrockPlayer(FEntity fPlayer);

    public abstract ExternalModeration getMute(FPlayer fPlayer);

    public abstract String getTritonLocale(FPlayer fPlayer);

    public boolean containsEnabledChild(Class<? extends ModuleSimple> clazz) {
        if (!moduleController.containsChild(this, clazz)) return false;

        return moduleController.isEnable(clazz);
    }

    public <T> T getInstance(Class<T> clazz) {
        return injector.getInstance(clazz);
    }

    public boolean hasFPlayerPermission(FPlayer fPlayer, String permission) {
        if (!moduleController.isEnable(this)) return false;

        if (containsEnabledChild(LuckPermsModule.class)) {
            return getInstance(LuckPermsModule.class).hasLuckPermission(fPlayer, permission);
        }

        return false;
    }

    public String getPrefix(FPlayer fPlayer) {
        if (!moduleController.isEnable(this)) return null;

        if (containsEnabledChild(LuckPermsModule.class)) {
            return injector.getInstance(LuckPermsModule.class).getPrefix(fPlayer);
        }

        return null;
    }

    public String getSuffix(FPlayer fPlayer) {
        if (!moduleController.isEnable(this)) return null;

        if (containsEnabledChild(LuckPermsModule.class)) {
            return injector.getInstance(LuckPermsModule.class).getSuffix(fPlayer);
        }

        return null;
    }

    public Set<String> getGroups() {
        if (!moduleController.isEnable(this)) return Collections.emptySet();

        if (containsEnabledChild(LuckPermsModule.class)) {
            return injector.getInstance(LuckPermsModule.class).getGroups();
        }

        return Collections.emptySet();
    }

    public int getGroupWeight(FPlayer fPlayer) {
        if (!moduleController.isEnable(this)) return 0;
        if (!containsEnabledChild(LuckPermsModule.class)) return 0;

        return injector.getInstance(LuckPermsModule.class).getGroupWeight(fPlayer);
    }

    public void sendMessage(FEntity sender, String messageName, UnaryOperator<String> discordString) {
        if (containsEnabledChild(DiscordModule.class) && !ModuleName.INTEGRATION_DISCORD.name().equals(messageName)) {
            injector.getInstance(DiscordModule.class).sendMessage(sender, messageName, discordString);
        }

        if (containsEnabledChild(TwitchModule.class) && !ModuleName.INTEGRATION_TWITCH.name().equals(messageName)) {
            injector.getInstance(TwitchModule.class).sendMessage(sender, messageName, discordString);
        }

        if (containsEnabledChild(TelegramModule.class) && !ModuleName.INTEGRATION_TELEGRAM.name().equals(messageName)) {
            injector.getInstance(TelegramModule.class).sendMessage(sender, messageName, discordString);
        }
    }

    public boolean hasMessenger() {
        return moduleController.isEnable(DiscordModule.class)
                || moduleController.isEnable(TwitchModule.class)
                || moduleController.isEnable(TelegramModule.class);
    }

    public boolean canSeeVanished(FEntity fTarget, FEntity fViewer) {
        if (fTarget.equals(fViewer)) return true;

        boolean isVanished = isVanished(fTarget);
        return !isVanished || hasSeeVanishPermission(fViewer);
    }

    public String deeplTranslate(FPlayer sender, String source, String target, String text) {
        if (moduleController.isDisabledFor(this, sender)) return text;
        if (containsEnabledChild(DeeplModule.class)) {
            return injector.getInstance(DeeplModule.class).translate(sender, source, target, text);
        }

        return text;
    }

    public String yandexTranslate(FPlayer sender, String source, String target, String text) {
        if (moduleController.isDisabledFor(this, sender)) return text;
        if (containsEnabledChild(YandexModule.class)) {
            return injector.getInstance(YandexModule.class).translate(sender, source, target, text);
        }

        return text;
    }
}
