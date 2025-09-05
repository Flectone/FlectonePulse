package net.flectone.pulse.processing.resolver;


import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import lombok.Getter;
import net.elytrium.serializer.language.object.AbstractSerializable;
import net.flectone.pulse.BuildConfig;
import net.flectone.pulse.config.*;
import net.flectone.pulse.model.FColor;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.util.constant.MessageType;
import net.flectone.pulse.util.constant.SettingText;
import net.flectone.pulse.util.logging.FLogger;
import org.apache.commons.lang3.Strings;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;


@Singleton
public class FileResolver {

    @Getter private final Map<String, Localization> localizationMap = new HashMap<>();

    @Getter private final Path projectPath;
    @Getter private final Command command;
    @Getter private final Config config;
    @Getter private final Integration integration;
    @Getter private final Message message;
    @Getter private final Permission permission;
    private final FLogger fLogger;

    @Getter private String preInitVersion;

    private Localization defaultLocalization;

    @Inject
    public FileResolver(@Named("projectPath") Path projectPath,
                        FLogger fLogger) {
        this.projectPath = projectPath;
        this.command = new Command(projectPath);
        this.config = new Config(projectPath);
        this.integration = new Integration(projectPath);
        this.message = new Message(projectPath);
        this.permission = new Permission(projectPath);
        this.fLogger = fLogger;
    }

    public Localization getLocalization() {
        return getLocalization(FPlayer.UNKNOWN);
    }

    public Localization getLocalization(FEntity sender) {
        if (!config.isLanguagePlayer()) return defaultLocalization;
        if (!(sender instanceof FPlayer fPlayer)) return defaultLocalization;

        return localizationMap.getOrDefault(fPlayer.getSetting(SettingText.LOCALE), defaultLocalization);
    }

    public void reload() {
        config.reload();
        config.setLanguage(config.getLanguage());

        preInitVersion = config.getVersion();

        if (!preInitVersion.equals(BuildConfig.PROJECT_VERSION)) {
            // fix update permission name
            if (isVersionOlderThan(preInitVersion, "1.4.3")) {
                migration_1_4_3();
            }

            if (isVersionOlderThan(preInitVersion, "1.5.0")) {
                migration_1_5_0();
            }

            if (isVersionOlderThan(preInitVersion, "1.5.2")) {
                migration_1_5_2();
            }

            config.setVersion(BuildConfig.PROJECT_VERSION);
            config.save();
        }

        command.reload();
        integration.reload();
        message.reload();
        permission.reload();

        reloadLanguages();

        defaultLocalization = localizationMap.get(config.getLanguage());
    }

    public void save() {
        command.save();
        config.save();
        integration.save();
        message.save();
        permission.save();
        localizationMap.values().forEach(AbstractSerializable::save);
    }

    private void reloadLanguages() {
        Set<String> newLanguages = new HashSet<>(Set.of("ru_ru", "en_us"));
        newLanguages.add(config.getLanguage());

        try (Stream<Path> paths = Files.walk(projectPath.resolve(Localization.FOLDER_NAME))) {
            paths.filter(Files::isRegularFile).forEach(path -> {
                File localization = path.toFile();
                String localizationName = localization.getName();
                if (localizationName.endsWith(".yml")) {
                    newLanguages.add(Strings.CS.replace(localizationName, ".yml", ""));
                }
            });
        } catch (NoSuchFileException ignored) {
            // ignore first startup
        } catch (IOException e) {
            fLogger.warning(e);
        }

        newLanguages.forEach(this::loadLanguage);
    }

    private void loadLanguage(String language) {
        Localization localization = new Localization(projectPath, language);
        localization.reload(localization.getPath());
        localizationMap.put(language, localization);
    }

    public boolean isVersionOlderThan(String first, String second) {
        String[] subFirst = parseVersionNumbers(first);
        if (subFirst.length != 3) return false;

        String[] subSecond = parseVersionNumbers(second);
        if (subSecond.length != 3) return true;

        for (int i = 0; i < 3; i++) {
            int intFirst = Integer.parseInt(subFirst[i]);
            int intSecond = Integer.parseInt(subSecond[i]);

            if (intFirst < intSecond) {
                return true;
            }

            if (intFirst > intSecond) {
                return false;
            }
        }

        return false;
    }

    private String[] parseVersionNumbers(String string) {
        int endIndex = string.indexOf('-');
        return (endIndex == -1 ? string : string.substring(0, endIndex)).split("\\.");
    }

    private void migration_1_4_3() {
        permission.reload();

        Permission.Message.Update update = permission.getMessage().getUpdate();
        if (update.getName().equals("flectonepulse.module.message.op")) {
            update.setName("flectonepulse.module.message.update");
            update.setSound(new Permission.PermissionEntry("flectonepulse.module.message.update.sound", Permission.Type.TRUE));
            permission.save();
        }
    }

    private void migration_1_5_0() {
        String oldChatKey = "CHAT";
        String newChatKey = "CHAT_GLOBAL";

        integration.reload();

        Integration.Discord discord = integration.getDiscord();
        if (discord.getMessageChannel().containsKey(oldChatKey)) {
            discord.getMessageChannel().put(newChatKey, discord.getMessageChannel().remove(oldChatKey));
        }

        Integration.Telegram telegram = integration.getTelegram();
        if (telegram.getMessageChannel().containsKey(oldChatKey)) {
            telegram.getMessageChannel().put(newChatKey, telegram.getMessageChannel().remove(oldChatKey));
        }

        Integration.Twitch twitch = integration.getTwitch();
        if (twitch.getMessageChannel().containsKey(oldChatKey)) {
            twitch.getMessageChannel().put(newChatKey, twitch.getMessageChannel().remove(oldChatKey));
        }

        integration.save();

        reloadLanguages();

        localizationMap.values().forEach(localization -> {
            Localization.Integration.Discord localizationDiscord = localization.getIntegration().getDiscord();
            if (localizationDiscord.getMessageChannel().containsKey(oldChatKey)) {
                localizationDiscord.getMessageChannel().put(newChatKey, localizationDiscord.getMessageChannel().remove(oldChatKey));
            }

            Localization.Integration.Twitch localizationTwitch = localization.getIntegration().getTwitch();
            if (localizationTwitch.getMessageChannel().containsKey(oldChatKey)) {
                localizationTwitch.getMessageChannel().put(newChatKey, localizationTwitch.getMessageChannel().remove(oldChatKey));
            }

            Localization.Integration.Telegram localizationTelegram = localization.getIntegration().getTelegram();
            if (localizationTelegram.getMessageChannel().containsKey(oldChatKey)) {
                localizationTelegram.getMessageChannel().put(newChatKey, localizationTelegram.getMessageChannel().remove(oldChatKey));
            }

            localization.save();
        });
    }

    private void migration_1_5_2() {
        command.reload();

        Map<String, Integer> types = command.getChatsetting().getCheckbox().getTypes();
        Map<String, Integer> oldTypes = new HashMap<>(types);
        types.clear();

        if (oldTypes.containsKey("AFK")) {
            types.put(MessageType.AFK.name(), 9);
        }

        if (oldTypes.containsKey("ADVANCEMENT")) {
            types.put(MessageType.ADVANCEMENT.name(), 10);
        }

        types.put(MessageType.CHAT.name(), 11);

        if (oldTypes.containsKey("ANON")) {
            types.put(MessageType.COMMAND_ANON.name(), 12);
        }

        if (oldTypes.containsKey("BALL")) {
            types.put(MessageType.COMMAND_BALL.name(), 13);
        }

        if (oldTypes.containsKey("BROADCAST")) {
            types.put(MessageType.COMMAND_BROADCAST.name(), 14);
        }

        if (oldTypes.containsKey("COIN")) {
            types.put(MessageType.COMMAND_COIN.name(), 15);
        }

        if (oldTypes.containsKey("DICE")) {
            types.put(MessageType.COMMAND_DICE.name(), 16);
        }

        if (oldTypes.containsKey("DO")) {
            types.put(MessageType.COMMAND_DO.name(), 17);
        }

        if (oldTypes.containsKey("MAIL")) {
            types.put(MessageType.COMMAND_MAIL.name(), 18);
        }

        if (oldTypes.containsKey("ME")) {
            types.put(MessageType.COMMAND_ME.name(), 19);
        }

        if (oldTypes.containsKey("POLL")) {
            types.put(MessageType.COMMAND_POLL.name(), 20);
        }

        if (oldTypes.containsKey("ROCKPAPERSCISSORS")) {
            types.put(MessageType.COMMAND_ROCKPAPERSCISSORS.name(), 21);
        }

        types.put(MessageType.COMMAND_STREAM.name(), 22);

        if (oldTypes.containsKey("TELL")) {
            types.put(MessageType.COMMAND_TELL.name(), 23);
        }

        if (oldTypes.containsKey("TICTACTOE")) {
            types.put(MessageType.COMMAND_TICTACTOE.name(), 24);
        }

        if (oldTypes.containsKey("TRY")) {
            types.put(MessageType.COMMAND_TRY.name(), 25);
        }

        if (oldTypes.containsKey("DEATH")) {
            types.put(MessageType.DEATH.name(), 26);
        }

        if (oldTypes.containsKey("DISCORD")) {
            types.put(MessageType.FROM_DISCORD_TO_MINECRAFT.name(), 27);
        }

        if (oldTypes.containsKey("TELEGRAM")) {
            types.put(MessageType.FROM_TELEGRAM_TO_MINECRAFT.name(), 28);
        }

        if (oldTypes.containsKey("TWITCH")) {
            types.put(MessageType.FROM_TWITCH_TO_MINECRAFT.name(), 29);
        }

        if (oldTypes.containsKey("JOIN")) {
            types.put(MessageType.JOIN.name(), 30);
        }

        if (oldTypes.containsKey("QUIT")) {
            types.put(MessageType.QUIT.name(), 31);
        }

        types.put(MessageType.SLEEP.name(), 32);

        command.save();

        permission.reload();

        Map<String, Permission.Command.Chatsetting.SettingItem> settings = permission.getCommand().getChatsetting().getSettings();
        settings.clear();

        settings.put(SettingText.CHAT_NAME.name(), new Permission.Command.Chatsetting.SettingItem("flectonepulse.module.command.chatsetting.chat_name", Permission.Type.TRUE));
        settings.put("FCOLOR_" + FColor.Type.SEE.name(), new Permission.Command.Chatsetting.SettingItem("flectonepulse.module.command.chatsetting.fcolor_see", Permission.Type.TRUE));
        settings.put("FCOLOR_" + FColor.Type.OUT.name(), new Permission.Command.Chatsetting.SettingItem("flectonepulse.module.command.chatsetting.fcolor_out", Permission.Type.OP));
        settings.put(MessageType.AFK.name(), new Permission.Command.Chatsetting.SettingItem("flectonepulse.module.command.chatsetting.afk", Permission.Type.TRUE));
        settings.put(MessageType.ADVANCEMENT.name(), new Permission.Command.Chatsetting.SettingItem("flectonepulse.module.command.chatsetting.advancement", Permission.Type.TRUE));
        settings.put(MessageType.CHAT.name(), new Permission.Command.Chatsetting.SettingItem("flectonepulse.module.command.chatsetting.chat", Permission.Type.TRUE));
        settings.put(MessageType.COMMAND_ANON.name(), new Permission.Command.Chatsetting.SettingItem("flectonepulse.module.command.chatsetting.command_anon", Permission.Type.TRUE));
        settings.put(MessageType.COMMAND_BALL.name(), new Permission.Command.Chatsetting.SettingItem("flectonepulse.module.command.chatsetting.command_ball", Permission.Type.TRUE));
        settings.put(MessageType.COMMAND_BROADCAST.name(), new Permission.Command.Chatsetting.SettingItem("flectonepulse.module.command.chatsetting.command_broadcast", Permission.Type.TRUE));
        settings.put(MessageType.COMMAND_COIN.name(), new Permission.Command.Chatsetting.SettingItem("flectonepulse.module.command.chatsetting.command_coin", Permission.Type.TRUE));
        settings.put(MessageType.COMMAND_DICE.name(), new Permission.Command.Chatsetting.SettingItem("flectonepulse.module.command.chatsetting.command_dice", Permission.Type.TRUE));
        settings.put(MessageType.COMMAND_DO.name(), new Permission.Command.Chatsetting.SettingItem("flectonepulse.module.command.chatsetting.command_do", Permission.Type.TRUE));
        settings.put(MessageType.COMMAND_MAIL.name(), new Permission.Command.Chatsetting.SettingItem("flectonepulse.module.command.chatsetting.command_mail", Permission.Type.TRUE));
        settings.put(MessageType.COMMAND_ME.name(), new Permission.Command.Chatsetting.SettingItem("flectonepulse.module.command.chatsetting.command_me", Permission.Type.TRUE));
        settings.put(MessageType.COMMAND_POLL.name(), new Permission.Command.Chatsetting.SettingItem("flectonepulse.module.command.chatsetting.command_poll", Permission.Type.TRUE));
        settings.put(MessageType.COMMAND_ROCKPAPERSCISSORS.name(), new Permission.Command.Chatsetting.SettingItem("flectonepulse.module.command.chatsetting.command_rockpaperscissors", Permission.Type.TRUE));
        settings.put(MessageType.COMMAND_STREAM.name(), new Permission.Command.Chatsetting.SettingItem("flectonepulse.module.command.chatsetting.command_stream", Permission.Type.TRUE));
        settings.put(MessageType.COMMAND_TELL.name(), new Permission.Command.Chatsetting.SettingItem("flectonepulse.module.command.chatsetting.command_tell", Permission.Type.TRUE));
        settings.put(MessageType.COMMAND_TICTACTOE.name(), new Permission.Command.Chatsetting.SettingItem("flectonepulse.module.command.chatsetting.command_tictactoe", Permission.Type.TRUE));
        settings.put(MessageType.COMMAND_TRY.name(), new Permission.Command.Chatsetting.SettingItem("flectonepulse.module.command.chatsetting.command_try", Permission.Type.TRUE));
        settings.put(MessageType.DEATH.name(), new Permission.Command.Chatsetting.SettingItem("flectonepulse.module.command.chatsetting.death", Permission.Type.TRUE));
        settings.put(MessageType.FROM_DISCORD_TO_MINECRAFT.name(), new Permission.Command.Chatsetting.SettingItem("flectonepulse.module.command.chatsetting.from_discord_to_minecraft", Permission.Type.TRUE));
        settings.put(MessageType.FROM_TELEGRAM_TO_MINECRAFT.name(), new Permission.Command.Chatsetting.SettingItem("flectonepulse.module.command.chatsetting.from_telegram_to_minecraft", Permission.Type.TRUE));
        settings.put(MessageType.FROM_TWITCH_TO_MINECRAFT.name(), new Permission.Command.Chatsetting.SettingItem("flectonepulse.module.command.chatsetting.from_twitch_to_minecraft", Permission.Type.TRUE));
        settings.put(MessageType.JOIN.name(), new Permission.Command.Chatsetting.SettingItem("flectonepulse.module.command.chatsetting.join", Permission.Type.TRUE));
        settings.put(MessageType.QUIT.name(), new Permission.Command.Chatsetting.SettingItem("flectonepulse.module.command.chatsetting.quit", Permission.Type.TRUE));
        settings.put(MessageType.SLEEP.name(), new Permission.Command.Chatsetting.SettingItem("flectonepulse.module.command.chatsetting.sleep", Permission.Type.TRUE));

        permission.save();

        reloadLanguages();

        localizationMap.values().forEach(localization -> {
            Map<String, String> localizationTypes = localization.getCommand().getChatsetting().getCheckbox().getTypes();
            localizationTypes.clear();

            if (localization.getLanguage().equals("ru_ru")) {
                localizationTypes.put(MessageType.AFK.name(), "<status_color>Афк");
                localizationTypes.put(MessageType.ADVANCEMENT.name(), "<status_color>Достижения");
                localizationTypes.put(MessageType.CHAT.name(), "<status_color>Сообщения чата");
                localizationTypes.put(MessageType.COMMAND_ANON.name(), "<status_color>Команда /anon");
                localizationTypes.put(MessageType.COMMAND_BALL.name(), "<status_color>Команда /ball");
                localizationTypes.put(MessageType.COMMAND_BROADCAST.name(), "<status_color>Команда /broadcast");
                localizationTypes.put(MessageType.COMMAND_COIN.name(), "<status_color>Команда /coin");
                localizationTypes.put(MessageType.COMMAND_DICE.name(), "<status_color>Команда /dice");
                localizationTypes.put(MessageType.COMMAND_DO.name(), "<status_color>Команда /do");
                localizationTypes.put(MessageType.COMMAND_MAIL.name(), "<status_color>Команда /mail");
                localizationTypes.put(MessageType.COMMAND_ME.name(), "<status_color>Команда /me");
                localizationTypes.put(MessageType.COMMAND_POLL.name(), "<status_color>Команда /poll");
                localizationTypes.put(MessageType.COMMAND_ROCKPAPERSCISSORS.name(), "<status_color>Команда /rockpaperscissors");
                localizationTypes.put(MessageType.COMMAND_STREAM.name(), "<status_color>Команда /stream");
                localizationTypes.put(MessageType.COMMAND_TELL.name(), "<status_color>Команда /tell");
                localizationTypes.put(MessageType.COMMAND_TICTACTOE.name(), "<status_color>Команда /tictactoe");
                localizationTypes.put(MessageType.COMMAND_TRY.name(), "<status_color>Команда /try");
                localizationTypes.put(MessageType.DEATH.name(), "<status_color>Смерти");
                localizationTypes.put(MessageType.FROM_DISCORD_TO_MINECRAFT.name(), "<status_color>Сообщения из Discord");
                localizationTypes.put(MessageType.FROM_TELEGRAM_TO_MINECRAFT.name(), "<status_color>Сообщения из Telegram");
                localizationTypes.put(MessageType.FROM_TWITCH_TO_MINECRAFT.name(), "<status_color>Сообщения из Twitch");
                localizationTypes.put(MessageType.JOIN.name(), "<status_color>Вход на сервер");
                localizationTypes.put(MessageType.QUIT.name(), "<status_color>Выход с сервера");
                localizationTypes.put(MessageType.SLEEP.name(), "<status_color>Сон");
            } else {
                localizationTypes.put(MessageType.AFK.name(), "<status_color>Afk");
                localizationTypes.put(MessageType.ADVANCEMENT.name(), "<status_color>Advancement");
                localizationTypes.put(MessageType.CHAT.name(), "<status_color>Chat messages");
                localizationTypes.put(MessageType.COMMAND_ANON.name(), "<status_color>Command /anon");
                localizationTypes.put(MessageType.COMMAND_BALL.name(), "<status_color>Command /ball");
                localizationTypes.put(MessageType.COMMAND_BROADCAST.name(), "<status_color>Command /broadcast");
                localizationTypes.put(MessageType.COMMAND_COIN.name(), "<status_color>Command /coin");
                localizationTypes.put(MessageType.COMMAND_DICE.name(), "<status_color>Command /dice");
                localizationTypes.put(MessageType.COMMAND_DO.name(), "<status_color>Command /do");
                localizationTypes.put(MessageType.COMMAND_MAIL.name(), "<status_color>Command /mail");
                localizationTypes.put(MessageType.COMMAND_ME.name(), "<status_color>Command /me");
                localizationTypes.put(MessageType.COMMAND_POLL.name(), "<status_color>Command /poll");
                localizationTypes.put(MessageType.COMMAND_ROCKPAPERSCISSORS.name(), "<status_color>Command /rockpaperscissors");
                localizationTypes.put(MessageType.COMMAND_STREAM.name(), "<status_color>Command /stream");
                localizationTypes.put(MessageType.COMMAND_TELL.name(), "<status_color>Command /tell");
                localizationTypes.put(MessageType.COMMAND_TICTACTOE.name(), "<status_color>Command /tictactoe");
                localizationTypes.put(MessageType.COMMAND_TRY.name(), "<status_color>Command /try");
                localizationTypes.put(MessageType.DEATH.name(), "<status_color>Death");
                localizationTypes.put(MessageType.FROM_DISCORD_TO_MINECRAFT.name(), "<status_color>Messages from Discord");
                localizationTypes.put(MessageType.FROM_TELEGRAM_TO_MINECRAFT.name(), "<status_color>Messages from Telegram");
                localizationTypes.put(MessageType.FROM_TWITCH_TO_MINECRAFT.name(), "<status_color>Messages from Twitch");
                localizationTypes.put(MessageType.JOIN.name(), "<status_color>Join");
                localizationTypes.put(MessageType.QUIT.name(), "<status_color>Quit");
                localizationTypes.put(MessageType.SLEEP.name(), "<status_color>Sleep");
            }

            localization.save();
        });
    }
}
