package net.flectone.pulse.module.integration;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import net.flectone.pulse.logger.FLogger;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.model.FEntity;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.module.integration.discord.DiscordModule;
import net.flectone.pulse.module.integration.interactivechat.InteractiveChatModule;
import net.flectone.pulse.module.integration.luckperms.LuckPermsModule;
import net.flectone.pulse.module.integration.placeholderapi.PlaceholderAPIModule;
import net.flectone.pulse.module.integration.plasmovoice.PlasmoVoiceModule;
import net.flectone.pulse.module.integration.simplevoice.SimpleVoiceModule;
import net.flectone.pulse.module.integration.skinsrestorer.SkinsRestorerModule;
import net.flectone.pulse.module.integration.supervanish.SuperVanishModule;
import net.flectone.pulse.module.integration.telegram.TelegramModule;
import net.flectone.pulse.module.integration.twitch.TwitchModule;
import net.flectone.pulse.module.integration.vault.VaultModule;
import net.flectone.pulse.util.MessageTag;
import net.flectone.pulse.util.ServerUtil;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.function.UnaryOperator;

@Singleton
public class BukkitIntegrationModule extends IntegrationModule {

    private final FLogger fLogger;
    private final Injector injector;

    @Inject
    private ServerUtil serverUtil;

    @Inject
    public BukkitIntegrationModule(FileManager fileManager,
                                   FLogger fLogger,
                                   Injector injector) {
        super(fileManager);

        this.fLogger = fLogger;
        this.injector = injector;
    }

    @Override
    public void reload() {
        super.reload();

        if (serverUtil.hasProject("PlaceholderAPI")) {
            addChildren(PlaceholderAPIModule.class);
        }

        if (serverUtil.hasProject("Vault")) {
            addChildren(VaultModule.class);
        }

        if (serverUtil.hasProject("LuckPerms")) {
            addChildren(LuckPermsModule.class);
        }

        if (serverUtil.hasProject("SuperVanish") || serverUtil.hasProject("PremiumVanish")) {
            addChildren(SuperVanishModule.class);
        }

        if (serverUtil.hasProject("SkinsRestorer")) {
            addChildren(SkinsRestorerModule.class);
        }

        if (serverUtil.hasProject("InteractiveChat")) {
            addChildren(InteractiveChatModule.class);
        }

        if (serverUtil.hasProject("VoiceChat")) {
            addChildren(SimpleVoiceModule.class);
        }

        if (serverUtil.hasProject("PlasmoVoice")) {
            try {
                Class.forName("su.plo.voice.api.server.event.audio.source.ServerSourceCreatedEvent");

                addChildren(PlasmoVoiceModule.class);
            } catch (ClassNotFoundException e) {
                fLogger.warning("Update PlasmoVoice to the latest version");
            }
        }

        addChildren(DiscordModule.class);
        addChildren(TwitchModule.class);
        addChildren(TelegramModule.class);
    }

    @Override
    public String checkMention(FPlayer fPlayer, Object event) {
        if (!(event instanceof AsyncPlayerChatEvent bukkitEvent)) return "";

        String message = bukkitEvent.getMessage();

        if (checkModulePredicates(fPlayer)) return message;

        return injector.getInstance(InteractiveChatModule.class).checkMention(bukkitEvent);
    }

    @Override
    public String mark(FEntity sender, String message) {
        if (checkModulePredicates(sender)) return message;
        if (!getChildren().contains(InteractiveChatModule.class)) return message;

        return injector.getInstance(InteractiveChatModule.class).mark(sender, message);
    }

    @Override
    public String setPlaceholders(FEntity sender, FEntity receiver, String message, boolean permission) {
        if (message == null) return null;
        if (checkModulePredicates(sender)) return message;
        if (checkModulePredicates(receiver)) return message;
        if (!getChildren().contains(PlaceholderAPIModule.class)) return message;

        return injector.getInstance(PlaceholderAPIModule.class).setPlaceholders(sender, receiver, message, permission);
    }

    @Override
    public boolean hasFPlayerPermission(FPlayer fPlayer, String permission) {
        if (!isEnable()) return false;

        boolean value = true;

        if (getChildren().contains(LuckPermsModule.class)) {
            value = injector.getInstance(LuckPermsModule.class).hasLuckPermission(fPlayer, permission);
        }

        if (getChildren().contains(VaultModule.class)) {
            value = value && injector.getInstance(VaultModule.class).hasVaultPermission(fPlayer, permission);
        }

        return value;
    }

    @Override
    public String getPrefix(FPlayer fPlayer) {
        if (!isEnable()) return null;

        if (getChildren().contains(LuckPermsModule.class)) {
            return injector.getInstance(LuckPermsModule.class).getPrefix(fPlayer);
        }

        if (getChildren().contains(VaultModule.class)) {
            return injector.getInstance(VaultModule.class).getPrefix(fPlayer);
        }

        return null;
    }

    @Override
    public String getSuffix(FPlayer fPlayer) {
        if (!isEnable()) return null;

        if (getChildren().contains(LuckPermsModule.class)) {
            return injector.getInstance(LuckPermsModule.class).getSuffix(fPlayer);
        }

        if (getChildren().contains(VaultModule.class)) {
            return injector.getInstance(VaultModule.class).getSuffix(fPlayer);
        }

        return null;
    }

    @Override
    public int getGroupWeight(FPlayer fPlayer) {
        if (!isEnable()) return 0;
        if (!getChildren().contains(LuckPermsModule.class)) return 0;

        return injector.getInstance(LuckPermsModule.class).getGroupWeight(fPlayer);
    }

    @Override
    public String getTextureUrl(FEntity sender) {
        if (!isEnable()) return null;
        if (!getChildren().contains(SkinsRestorerModule.class)) return null;
        if (!(sender instanceof FPlayer fPlayer)) return null;

        return injector.getInstance(SkinsRestorerModule.class).getTextureUrl(fPlayer);
    }

    @Override
    public void sendMessage(FEntity sender, MessageTag messageTag, UnaryOperator<String> discordString) {
        if (getChildren().contains(DiscordModule.class)) {
            injector.getInstance(DiscordModule.class).sendMessage(sender, messageTag, discordString);
        }

        if (getChildren().contains(TwitchModule.class)) {
            injector.getInstance(TwitchModule.class).sendMessage(sender, messageTag, discordString);
        }

        if (getChildren().contains(TelegramModule.class)) {
            injector.getInstance(TelegramModule.class).sendMessage(sender, messageTag, discordString);
        }
    }

    @Override
    public boolean isVanished(FEntity sender) {
        if (getChildren().contains(SuperVanishModule.class)) {
            return injector.getInstance(SuperVanishModule.class).isVanished(sender);
        }

        return false;
    }
}
