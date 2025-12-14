package net.flectone.pulse.processing.resolver;


import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import lombok.Getter;
import net.flectone.pulse.BuildConfig;
import net.flectone.pulse.config.*;
import net.flectone.pulse.config.localization.Localization;
import net.flectone.pulse.model.FColor;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.processing.processor.YamlFileProcessor;
import net.flectone.pulse.util.constant.MessageType;
import net.flectone.pulse.util.constant.SettingText;
import net.flectone.pulse.util.creator.BackupCreator;
import net.flectone.pulse.util.logging.FLogger;
import org.apache.commons.lang3.Strings;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Stream;


@Getter
@Singleton
public class FileResolver {

    private final Map<String, Localization> localizationMap = new HashMap<>();

    private final Path projectPath;
    private final Command command;
    private final Config config;
    private final Integration integration;
    private final Message message;
    private final Permission permission;
    private final FLogger fLogger;
    private final YamlFileProcessor yamlFileProcessor;
    private final BackupCreator backupCreator;

    private String preInitVersion;
    private Localization defaultLocalization;

    @Inject
    public FileResolver(@Named("projectPath") Path projectPath,
                        FLogger fLogger,
                        YamlFileProcessor yamlFileProcessor,
                        BackupCreator backupCreator) {
        this.projectPath = projectPath;
        this.command = new Command(projectPath);
        this.config = new Config(projectPath);
        this.integration = new Integration(projectPath);
        this.message = new Message(projectPath);
        this.permission = new Permission(projectPath);
        this.fLogger = fLogger;
        this.yamlFileProcessor = yamlFileProcessor;
        this.backupCreator = backupCreator;
    }

    public Localization getLocalization() {
        return getLocalization(FPlayer.UNKNOWN);
    }

    public Localization getLocalization(FEntity sender) {
        if (!config.getLanguage().isByPlayer()) return defaultLocalization;
        if (!(sender instanceof FPlayer fPlayer)) return defaultLocalization;

        return localizationMap.getOrDefault(fPlayer.getSetting(SettingText.LOCALE), defaultLocalization);
    }

    public void reload() throws IOException {
        // this is to check FlectonePulse version
        // mb in the future we should put version in a separate file, but I think it's not so important
        yamlFileProcessor.reload(config);

        // init localization file names
        loadLanguages();

        // check version
        preInitVersion = config.getVersion();
        boolean versionChanged = !preInitVersion.equals(BuildConfig.PROJECT_VERSION);

        // backup if version changed
        if (versionChanged) {
            backupConfiguration();
        }

        reloadConfiguration();

        if (versionChanged) {
            // fix update permission name
            if (isVersionOlderThan(preInitVersion, "1.4.3")) {
                migration_1_4_3();
            }

            if (isVersionOlderThan(preInitVersion, "1.5.0")) {
                migration_1_5_0();
            }

            if (isVersionOlderThan(preInitVersion, "1.6.0")) {
                migration_1_6_0();
            }

            if (isVersionOlderThan(preInitVersion, "1.7.0")) {
                migration_1_7_0();
            }
        }

        if (versionChanged) {
            config.setVersion(BuildConfig.PROJECT_VERSION);
            yamlFileProcessor.save(config);
        }
    }

    private void backupConfiguration() {
        backupCreator.setPreInitVersion(preInitVersion);

        // we can't backup config.yml because it has already been reloaded
        backupCreator.backup(command);
        backupCreator.backup(integration);
        backupCreator.backup(message);
        backupCreator.backup(permission);

        for (Localization localization : localizationMap.values()) {
            backupCreator.backup(localization);
        }
    }

    private void reloadConfiguration() throws IOException {
        yamlFileProcessor.reload(command);
        yamlFileProcessor.reload(integration);
        yamlFileProcessor.reload(message);
        yamlFileProcessor.reload(permission);

        for (Localization localization : localizationMap.values()) {
            yamlFileProcessor.reload(localization);
        }

        defaultLocalization = localizationMap.get(config.getLanguage().getType());
    }

    public void save() throws IOException {
        yamlFileProcessor.save(command);
        yamlFileProcessor.save(config);
        yamlFileProcessor.save(integration);
        yamlFileProcessor.save(message);
        yamlFileProcessor.save(permission);

        for (Localization localization : localizationMap.values()) {
            yamlFileProcessor.save(localization);
        }
    }

    private void loadLanguages() {
        Set<String> newLanguages = new HashSet<>(Set.of("ru_ru", "en_us"));
        newLanguages.add(config.getLanguage().getType());

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

        for (String language : newLanguages) {
            loadLanguage(language);
        }
    }

    private void loadLanguage(String language) {
        Localization localization = new Localization(projectPath, language);
        localizationMap.put(language, localization);
    }

    public boolean isSnapshot(String version) {
        return version.endsWith("-SNAPSHOT");
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

        return isSnapshot(first) && !isSnapshot(second);
    }

    private String[] parseVersionNumbers(String string) {
        int endIndex = string.indexOf('-');
        return (endIndex == -1 ? string : string.substring(0, endIndex)).split("\\.");
    }

    private void migration_1_4_3() throws IOException {
        Permission.Message.Update update = permission.getMessage().getUpdate();
        if (update.getName().equals("flectonepulse.module.message.op")) {
            update.setName("flectonepulse.module.message.update");
            update.setSound(new Permission.PermissionEntry("flectonepulse.module.message.update.sound", Permission.Type.TRUE));
            yamlFileProcessor.save(permission);
        }
    }

    private void migration_1_5_0() throws IOException {
        String oldChatKey = "CHAT";
        String newChatKey = "CHAT_GLOBAL";

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

        yamlFileProcessor.save(integration);

        for (Localization localization : localizationMap.values()) {
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

            yamlFileProcessor.save(localization);
        }
    }

    private void migration_1_6_0() throws IOException {
        List<Command.Chatsetting.Menu.Color.Type> colorTypes = command.getChatsetting().getMenu().getSee().getTypes();
        for (Command.Chatsetting.Menu.Color.Type colorType : colorTypes) {
            if (colorType.getName().equals("default")) {
                colorType.getColors().put(1, "");
                colorType.getColors().put(2, "");
            }
        }

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

        yamlFileProcessor.save(command);

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

        yamlFileProcessor.save(permission);

        for (Localization localization : localizationMap.values()) {
            Map<String, String> localizationTypes = localization.getCommand().getChatsetting().getCheckbox().getTypes();
            localizationTypes.clear();

            boolean isRussian = localization.getLanguage().toLowerCase().contains("ru");

            if (isRussian) {
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

                Localization defaultRussianLocalization = new Localization(projectPath, "ru_ru");
                yamlFileProcessor.initLocalization(defaultRussianLocalization);

                localization.getCommand().setClearmail(defaultRussianLocalization.getCommand().getClearmail());
                localization.getCommand().setMail(defaultRussianLocalization.getCommand().getMail());
                localization.getCommand().setTell(defaultRussianLocalization.getCommand().getTell());
                localization.getCommand().setTictactoe(defaultRussianLocalization.getCommand().getTictactoe());
                localization.getCommand().setToponline(defaultRussianLocalization.getCommand().getToponline());
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

                Localization defaultEnglishLocalization = new Localization(projectPath, "en_us");
                yamlFileProcessor.initLocalization(defaultEnglishLocalization);

                localization.getCommand().setClearmail(defaultEnglishLocalization.getCommand().getClearmail());
                localization.getCommand().setMail(defaultEnglishLocalization.getCommand().getMail());
                localization.getCommand().setTell(defaultEnglishLocalization.getCommand().getTell());
                localization.getCommand().setTictactoe(defaultEnglishLocalization.getCommand().getTictactoe());
                localization.getCommand().setToponline(defaultEnglishLocalization.getCommand().getToponline());
            }

            yamlFileProcessor.save(localization);
        }
    }

    private void migration_1_7_0() throws IOException {
        Map<String, String> triggers = message.getFormat().getReplacement().getTriggers();

        Map<String, String> updates = new HashMap<>();
        updates.put("smile", ":-?\\)");
        updates.put("big_smile", ":-?D");
        updates.put("sad", ":-?\\(");
        updates.put("ok_hand", "(?i):ok:");
        updates.put("thumbs_up", ":\\+1:");
        updates.put("thumbs_down", ":-1:");
        updates.put("cool_smile", "(?i):cool:");
        updates.put("cool_glasses", "B-\\)");
        updates.put("clown", "(?i):clown:");
        updates.put("heart", "<3");
        updates.put("laughing", "(?i)xd");
        updates.put("confused", "%-\\)");
        updates.put("happy", "=D");
        updates.put("angry", ">:-?\\(");
        updates.put("ascii_idk", "(?i):idk:");
        updates.put("ascii_angry", "(?i):angry:");
        updates.put("ascii_happy", "(?i):happy:");
        updates.put("ping", "%ping%");
        updates.put("tps", "%tps%");
        updates.put("online", "%online%");
        updates.put("coords", "%coords%");
        updates.put("stats", "%stats%");
        updates.put("skin", "%skin%");
        updates.put("item", "%item%");
        updates.put("spoiler", "\\|\\|");
        updates.put("bold", "\\*\\*");
        updates.put("italic", "\\*");
        updates.put("underline", "__");
        updates.put("obfuscated", "\\?\\?");
        updates.put("strikethrough", "~~");

        String boundaryPattern = "(?<!\\\\)(?<!\\S)%s(?!\\S)";
        String formatTemplate = "(?<!\\S)%1$s([^%1$s\\n]+)%1$s(?!\\S)";

        updates.forEach((key, value) -> {
            if (triggers.containsKey(key)) {
                String pattern = key.equals("spoiler") || key.equals("bold") || key.equals("italic") || key.equals("underline") || key.equals("obfuscated") || key.equals("strikethrough")
                        ? formatTemplate : boundaryPattern;
                triggers.put(key, String.format(pattern, value));
            }
        });

        if (message.getBubble().getElevation() == 1) {
            message.getBubble().setElevation(0.4f);
        }

        yamlFileProcessor.save(message);

        for (Localization localization : localizationMap.values()) {
            Localization.Integration localizationIntegration = localization.getIntegration();

            localizationIntegration.getDiscord().getMessageChannel().put(MessageType.FROM_DISCORD_TO_MINECRAFT.name(), new Localization.Integration.Discord.ChannelEmbed());
            localizationIntegration.getDiscord().getMessageChannel().get(MessageType.FROM_DISCORD_TO_MINECRAFT.name()).setContent("<fcolor:2><global_name> <fcolor:1>» <fcolor:4><message>");
            localizationIntegration.getTelegram().getMessageChannel().put(MessageType.FROM_TELEGRAM_TO_MINECRAFT.name(), "<fcolor:2><user_name> <fcolor:1>» <fcolor:4><message>");
            localizationIntegration.getTwitch().getMessageChannel().put(MessageType.FROM_TWITCH_TO_MINECRAFT.name(), "<fcolor:2><name> <fcolor:1>» <fcolor:4><message>");

            localizationIntegration.getDiscord().getInfoChannel().forEach((key, value) ->
                    localizationIntegration.getDiscord().getInfoChannel().put(key, Strings.CS.replace(value, "<tps>", "<replacement:tps>")));

            localizationIntegration.getTelegram().getInfoChannel().forEach((key, value) ->
                    localizationIntegration.getTelegram().getInfoChannel().put(key, Strings.CS.replace(value, "<tps>", "<replacement:tps>")));

            Localization.Message.Vanilla localizationVanilla = localization.getMessage().getVanilla();
            for (Map.Entry<String, String> entry : localizationVanilla.getTypes().entrySet()) {
                localizationVanilla.getTypes().put(entry.getKey(), Strings.CS.replace(entry.getValue(), "<arg_", "<argument:"));
            }

            boolean isRussian = localization.getLanguage().equalsIgnoreCase("ru_ru");

            if (isRussian) {
                localizationVanilla.getTypes().put("commands.list.players", "<fcolor:1>\uD83D\uDC65 На сервере <fcolor:2><argument:0><fcolor:1> из <fcolor:2><argument:1><fcolor:1> игроков: <argument:2>");
                localizationVanilla.getTypes().put("death.attack.spear", "<fcolor:1>☠ <argument:0> был проткнут <argument:1>");
                localizationVanilla.getTypes().put("death.attack.spear.item", "<fcolor:1>☠ <argument:0> был проткнут <argument:1> с помощью <argument:2>");
            } else {
                localizationVanilla.getTypes().put("commands.list.players", "<fcolor:1>\uD83D\uDC65 There are <fcolor:2><argument:0><fcolor:1> of a max of <fcolor:2><argument:1><fcolor:1> players online: <argument:2>");
                localizationVanilla.getTypes().put("death.attack.spear", "<fcolor:1>☠ <argument:0> was speared by <argument:1>");
                localizationVanilla.getTypes().put("death.attack.spear.item", "<fcolor:1>☠ <argument:0> was speared by <argument:1> using <argument:2>");
            }

            Localization.Command.Symbol localizationSymbol = localization.getCommand().getSymbol();
            String oldSymbolFormat = localizationSymbol.getFormat();
            localizationSymbol.setFormat(Strings.CS.replace(oldSymbolFormat, "<click:suggest_command:\"<message>\">", "<click:suggest_command:\"<input>\">"));

            Localization.Command.Ping localizationPing = localization.getCommand().getPing();
            String oldPingFormat = localizationPing.getFormat();
            localization.getCommand().getPing().setFormat(Strings.CS.replace(oldPingFormat, "<ping>", "<replacement:ping>"));

            String[] oldTags = new String[]{"ping", "tps", "online"};

            Localization.Message.Format.Replacement localizationReplacement = localization.getMessage().getFormat().getReplacement();
            for (String key : oldTags) {
                String value = localizationReplacement.getValues().get(key);
                if (value != null) {
                    localizationReplacement.getValues().put(key, Strings.CS.replace(value, "<" + key + ">", "<value>"));
                }
            }

            if (isRussian && localizationReplacement.getValues().containsKey("url")) {
                localizationReplacement.getValues().put("url", "<click:open_url:\"<message_1>\"><hover:show_text:\"<fcolor:2>Открыть ссылку <br><u><message_1>\"><fcolor:2><u>🗗 Ссылка</u></hover></click>");
            }

            Consumer<List<String>> stringsWithOldTagsConsumer = strings -> {
                for (int i = 0; i < strings.size(); i++) {
                    String string = strings.get(i);
                    for (String oldTag : oldTags) {
                        if (string.contains(oldTag)) {
                            string = Strings.CS.replace(string, "<" + oldTag + ">", "<replacement:" + oldTag + ">");
                            strings.set(i, string);
                        }
                    }
                }
            };
            localization.getMessage().getSidebar().getValues().forEach(stringsWithOldTagsConsumer);
            localization.getMessage().getTab().getFooter().getLists().forEach(stringsWithOldTagsConsumer);
            localization.getMessage().getTab().getHeader().getLists().forEach(stringsWithOldTagsConsumer);

            Localization.Message.Update localizationUpdate = localization.getMessage().getUpdate();
            String oldUpdateFormat = localizationUpdate.getFormatPlayer();
            localizationUpdate.setFormatPlayer(Strings.CS.replace(oldUpdateFormat, "<url:", "<replacement:url:"));

            yamlFileProcessor.save(localization);
        }
    }
}
