package net.flectone.pulse.util.file;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.*;
import net.flectone.pulse.model.FColor;
import net.flectone.pulse.model.file.FilePack;
import net.flectone.pulse.platform.adapter.PlatformServerAdapter;
import net.flectone.pulse.util.constant.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class FileMigrator {

    private final Provider<FileLoader> fileLoaderProvider;
    private final Provider<PlatformServerAdapter> platformServerAdapterProvider;

    public FilePack migration_1_4_3(FilePack files) {
        Permission.Message.Update update = files.permission().message().update();
        if (update.name().equals("flectonepulse.module.message.op")) {
            files = files.withPermission(files.permission().withMessage(files.permission().message().withUpdate(files.permission().message().update()
                    .withName("flectonepulse.module.message.update.sound")
                    .withSound(new Permission.PermissionEntry("flectonepulse.module.message.update.sound", Permission.Type.TRUE))))
            );
        }

        return files;
    }

    public FilePack migration_1_5_0(FilePack files) {
        String oldChatKey = "CHAT";
        String newChatKey = "CHAT_GLOBAL";

        Integration.Discord discord = files.integration().discord();
        if (discord.messageChannel().containsKey(oldChatKey)) {
            discord.messageChannel().put(newChatKey, discord.messageChannel().remove(oldChatKey));
        }

        Integration.Telegram telegram = files.integration().telegram();
        if (telegram.messageChannel().containsKey(oldChatKey)) {
            telegram.messageChannel().put(newChatKey, telegram.messageChannel().remove(oldChatKey));
        }

        Integration.Twitch twitch = files.integration().twitch();
        if (twitch.messageChannel().containsKey(oldChatKey)) {
            twitch.messageChannel().put(newChatKey, twitch.messageChannel().remove(oldChatKey));
        }

        for (Localization localization : files.localizations().values()) {
            Localization.Integration.Discord localizationDiscord = localization.integration().discord();
            if (localizationDiscord.messageChannel().containsKey(oldChatKey)) {
                localizationDiscord.messageChannel().put(newChatKey, localizationDiscord.messageChannel().remove(oldChatKey));
            }

            Localization.Integration.Twitch localizationTwitch = localization.integration().twitch();
            if (localizationTwitch.messageChannel().containsKey(oldChatKey)) {
                localizationTwitch.messageChannel().put(newChatKey, localizationTwitch.messageChannel().remove(oldChatKey));
            }

            Localization.Integration.Telegram localizationTelegram = localization.integration().telegram();
            if (localizationTelegram.messageChannel().containsKey(oldChatKey)) {
                localizationTelegram.messageChannel().put(newChatKey, localizationTelegram.messageChannel().remove(oldChatKey));
            }
        }

        return files;
    }

    public FilePack migration_1_6_0(FilePack files) {
        List<Command.Chatsetting.Menu.Color.Type> colorTypes = files.command().chatsetting().menu().see().types();
        for (Command.Chatsetting.Menu.Color.Type colorType : colorTypes) {
            if (colorType.name().equals("default")) {
                colorType.colors().put(1, "");
                colorType.colors().put(2, "");
            }
        }

        Map<String, Integer> types = files.command().chatsetting().checkbox().types();
        Map<String, Integer> oldTypes = new HashMap<>(types);
        types.clear();

        if (oldTypes.containsKey("AFK")) {
            types.put(ModuleName.MESSAGE_AFK.name(), 9);
        }

        if (oldTypes.containsKey("ADVANCEMENT")) {
            types.put("ADVANCEMENT", 10);
        }

        types.put("MESSAGE_CHAT", 11);

        if (oldTypes.containsKey("ANON")) {
            types.put(ModuleName.COMMAND_ANON.name(), 12);
        }

        if (oldTypes.containsKey("BALL")) {
            types.put(ModuleName.COMMAND_BALL.name(), 13);
        }

        if (oldTypes.containsKey("BROADCAST")) {
            types.put(ModuleName.COMMAND_BROADCAST.name(), 14);
        }

        if (oldTypes.containsKey("COIN")) {
            types.put(ModuleName.COMMAND_COIN.name(), 15);
        }

        if (oldTypes.containsKey("DICE")) {
            types.put(ModuleName.COMMAND_DICE.name(), 16);
        }

        if (oldTypes.containsKey("DO")) {
            types.put(ModuleName.COMMAND_DO.name(), 17);
        }

        if (oldTypes.containsKey("MAIL")) {
            types.put(ModuleName.COMMAND_MAIL.name(), 18);
        }

        if (oldTypes.containsKey("ME")) {
            types.put(ModuleName.COMMAND_ME.name(), 19);
        }

        if (oldTypes.containsKey("POLL")) {
            types.put(ModuleName.COMMAND_POLL.name(), 20);
        }

        if (oldTypes.containsKey("ROCKPAPERSCISSORS")) {
            types.put(ModuleName.COMMAND_ROCKPAPERSCISSORS.name(), 21);
        }

        types.put(ModuleName.COMMAND_STREAM.name(), 22);

        if (oldTypes.containsKey("TELL")) {
            types.put(ModuleName.COMMAND_TELL.name(), 23);
        }

        if (oldTypes.containsKey("TICTACTOE")) {
            types.put(ModuleName.COMMAND_TICTACTOE.name(), 24);
        }

        if (oldTypes.containsKey("TRY")) {
            types.put(ModuleName.COMMAND_TRY.name(), 25);
        }

        if (oldTypes.containsKey("DEATH")) {
            types.put("DEATH", 26);
        }

        if (oldTypes.containsKey("DISCORD")) {
            types.put(ModuleName.INTEGRATION_DISCORD.name(), 27);
        }

        if (oldTypes.containsKey("TELEGRAM")) {
            types.put(ModuleName.INTEGRATION_TELEGRAM.name(), 28);
        }

        if (oldTypes.containsKey("TWITCH")) {
            types.put(ModuleName.INTEGRATION_TWITCH.name(), 29);
        }

        if (oldTypes.containsKey("JOIN")) {
            types.put(ModuleName.MESSAGE_JOIN.name(), 30);
        }

        if (oldTypes.containsKey("QUIT")) {
            types.put(ModuleName.MESSAGE_QUIT.name(), 31);
        }

        types.put("SLEEP", 32);

        Map<String, Permission.Command.Chatsetting.SettingItem> settings = files.permission().command().chatsetting().settings();
        settings.clear();

        settings.put(SettingText.CHAT_NAME.name(), new Permission.Command.Chatsetting.SettingItem("flectonepulse.module.command.chatsetting.chat_name", Permission.Type.TRUE));
        settings.put("FCOLOR_" + FColor.Type.SEE.name(), new Permission.Command.Chatsetting.SettingItem("flectonepulse.module.command.chatsetting.fcolor_see", Permission.Type.TRUE));
        settings.put("FCOLOR_" + FColor.Type.OUT.name(), new Permission.Command.Chatsetting.SettingItem("flectonepulse.module.command.chatsetting.fcolor_out", Permission.Type.OP));
        settings.put(ModuleName.MESSAGE_AFK.name(), new Permission.Command.Chatsetting.SettingItem("flectonepulse.module.command.chatsetting.afk", Permission.Type.TRUE));
        settings.put("ADVANCEMENT", new Permission.Command.Chatsetting.SettingItem("flectonepulse.module.command.chatsetting.advancement", Permission.Type.TRUE));
        settings.put(ModuleName.MESSAGE_CHAT.name(), new Permission.Command.Chatsetting.SettingItem("flectonepulse.module.command.chatsetting.chat", Permission.Type.TRUE));
        settings.put(ModuleName.COMMAND_ANON.name(), new Permission.Command.Chatsetting.SettingItem("flectonepulse.module.command.chatsetting.command_anon", Permission.Type.TRUE));
        settings.put(ModuleName.COMMAND_BALL.name(), new Permission.Command.Chatsetting.SettingItem("flectonepulse.module.command.chatsetting.command_ball", Permission.Type.TRUE));
        settings.put(ModuleName.COMMAND_BROADCAST.name(), new Permission.Command.Chatsetting.SettingItem("flectonepulse.module.command.chatsetting.command_broadcast", Permission.Type.TRUE));
        settings.put(ModuleName.COMMAND_COIN.name(), new Permission.Command.Chatsetting.SettingItem("flectonepulse.module.command.chatsetting.command_coin", Permission.Type.TRUE));
        settings.put(ModuleName.COMMAND_DICE.name(), new Permission.Command.Chatsetting.SettingItem("flectonepulse.module.command.chatsetting.command_dice", Permission.Type.TRUE));
        settings.put(ModuleName.COMMAND_DO.name(), new Permission.Command.Chatsetting.SettingItem("flectonepulse.module.command.chatsetting.command_do", Permission.Type.TRUE));
        settings.put(ModuleName.COMMAND_MAIL.name(), new Permission.Command.Chatsetting.SettingItem("flectonepulse.module.command.chatsetting.command_mail", Permission.Type.TRUE));
        settings.put(ModuleName.COMMAND_ME.name(), new Permission.Command.Chatsetting.SettingItem("flectonepulse.module.command.chatsetting.command_me", Permission.Type.TRUE));
        settings.put(ModuleName.COMMAND_POLL.name(), new Permission.Command.Chatsetting.SettingItem("flectonepulse.module.command.chatsetting.command_poll", Permission.Type.TRUE));
        settings.put(ModuleName.COMMAND_ROCKPAPERSCISSORS.name(), new Permission.Command.Chatsetting.SettingItem("flectonepulse.module.command.chatsetting.command_rockpaperscissors", Permission.Type.TRUE));
        settings.put(ModuleName.COMMAND_STREAM.name(), new Permission.Command.Chatsetting.SettingItem("flectonepulse.module.command.chatsetting.command_stream", Permission.Type.TRUE));
        settings.put(ModuleName.COMMAND_TELL.name(), new Permission.Command.Chatsetting.SettingItem("flectonepulse.module.command.chatsetting.command_tell", Permission.Type.TRUE));
        settings.put(ModuleName.COMMAND_TICTACTOE.name(), new Permission.Command.Chatsetting.SettingItem("flectonepulse.module.command.chatsetting.command_tictactoe", Permission.Type.TRUE));
        settings.put(ModuleName.COMMAND_TRY.name(), new Permission.Command.Chatsetting.SettingItem("flectonepulse.module.command.chatsetting.command_try", Permission.Type.TRUE));
        settings.put("DEATH", new Permission.Command.Chatsetting.SettingItem("flectonepulse.module.command.chatsetting.death", Permission.Type.TRUE));
        settings.put(ModuleName.INTEGRATION_DISCORD.name(), new Permission.Command.Chatsetting.SettingItem("flectonepulse.module.command.chatsetting.from_discord_to_minecraft", Permission.Type.TRUE));
        settings.put(ModuleName.INTEGRATION_TELEGRAM.name(), new Permission.Command.Chatsetting.SettingItem("flectonepulse.module.command.chatsetting.from_telegram_to_minecraft", Permission.Type.TRUE));
        settings.put(ModuleName.INTEGRATION_TWITCH.name(), new Permission.Command.Chatsetting.SettingItem("flectonepulse.module.command.chatsetting.from_twitch_to_minecraft", Permission.Type.TRUE));
        settings.put(ModuleName.MESSAGE_JOIN.name(), new Permission.Command.Chatsetting.SettingItem("flectonepulse.module.command.chatsetting.join", Permission.Type.TRUE));
        settings.put(ModuleName.MESSAGE_QUIT.name(), new Permission.Command.Chatsetting.SettingItem("flectonepulse.module.command.chatsetting.quit", Permission.Type.TRUE));
        settings.put("SLEEP", new Permission.Command.Chatsetting.SettingItem("flectonepulse.module.command.chatsetting.sleep", Permission.Type.TRUE));

        Map<String, Localization> newLocalizations = new Object2ObjectArrayMap<>();

        for (Localization localization : files.localizations().values()) {
            Map<String, String> localizationTypes = localization.command().chatsetting().checkbox().types();
            localizationTypes.clear();

            boolean isRussian = localization.language().toLowerCase().contains("ru");

            if (isRussian) {
                localizationTypes.put(ModuleName.MESSAGE_AFK.name(), "<status_color>Афк");
                localizationTypes.put("ADVANCEMENT", "<status_color>Достижения");
                localizationTypes.put(ModuleName.MESSAGE_CHAT.name(), "<status_color>Сообщения чата");
                localizationTypes.put(ModuleName.COMMAND_ANON.name(), "<status_color>Команда /anon");
                localizationTypes.put(ModuleName.COMMAND_BALL.name(), "<status_color>Команда /ball");
                localizationTypes.put(ModuleName.COMMAND_BROADCAST.name(), "<status_color>Команда /broadcast");
                localizationTypes.put(ModuleName.COMMAND_COIN.name(), "<status_color>Команда /coin");
                localizationTypes.put(ModuleName.COMMAND_DICE.name(), "<status_color>Команда /dice");
                localizationTypes.put(ModuleName.COMMAND_DO.name(), "<status_color>Команда /do");
                localizationTypes.put(ModuleName.COMMAND_MAIL.name(), "<status_color>Команда /mail");
                localizationTypes.put(ModuleName.COMMAND_ME.name(), "<status_color>Команда /me");
                localizationTypes.put(ModuleName.COMMAND_POLL.name(), "<status_color>Команда /poll");
                localizationTypes.put(ModuleName.COMMAND_ROCKPAPERSCISSORS.name(), "<status_color>Команда /rockpaperscissors");
                localizationTypes.put(ModuleName.COMMAND_STREAM.name(), "<status_color>Команда /stream");
                localizationTypes.put(ModuleName.COMMAND_TELL.name(), "<status_color>Команда /tell");
                localizationTypes.put(ModuleName.COMMAND_TICTACTOE.name(), "<status_color>Команда /tictactoe");
                localizationTypes.put(ModuleName.COMMAND_TRY.name(), "<status_color>Команда /try");
                localizationTypes.put("DEATH", "<status_color>Смерти");
                localizationTypes.put(ModuleName.INTEGRATION_DISCORD.name(), "<status_color>Сообщения из Discord");
                localizationTypes.put(ModuleName.INTEGRATION_TELEGRAM.name(), "<status_color>Сообщения из Telegram");
                localizationTypes.put(ModuleName.INTEGRATION_TWITCH.name(), "<status_color>Сообщения из Twitch");
                localizationTypes.put(ModuleName.MESSAGE_JOIN.name(), "<status_color>Вход на сервер");
                localizationTypes.put(ModuleName.MESSAGE_QUIT.name(), "<status_color>Выход с сервера");
                localizationTypes.put("SLEEP", "<status_color>Сон");

                Localization defaultRussianLocalization = fileLoaderProvider.get().getDefaultFiles().localizations().get(DefaultLocalization.RUSSIAN.getName());

                newLocalizations.put(localization.language(), localization.withCommand(localization.command()
                                .withClearmail(defaultRussianLocalization.command().clearmail())
                                .withMail(defaultRussianLocalization.command().mail())
                                .withTell(defaultRussianLocalization.command().tell())
                                .withTictactoe(defaultRussianLocalization.command().tictactoe())
                                .withToponline(defaultRussianLocalization.command().toponline())
                        )
                );

            } else {
                localizationTypes.put(ModuleName.MESSAGE_AFK.name(), "<status_color>Afk");
                localizationTypes.put("ADVANCEMENT", "<status_color>Advancement");
                localizationTypes.put(ModuleName.MESSAGE_CHAT.name(), "<status_color>Chat messages");
                localizationTypes.put(ModuleName.COMMAND_ANON.name(), "<status_color>Command /anon");
                localizationTypes.put(ModuleName.COMMAND_BALL.name(), "<status_color>Command /ball");
                localizationTypes.put(ModuleName.COMMAND_BROADCAST.name(), "<status_color>Command /broadcast");
                localizationTypes.put(ModuleName.COMMAND_COIN.name(), "<status_color>Command /coin");
                localizationTypes.put(ModuleName.COMMAND_DICE.name(), "<status_color>Command /dice");
                localizationTypes.put(ModuleName.COMMAND_DO.name(), "<status_color>Command /do");
                localizationTypes.put(ModuleName.COMMAND_MAIL.name(), "<status_color>Command /mail");
                localizationTypes.put(ModuleName.COMMAND_ME.name(), "<status_color>Command /me");
                localizationTypes.put(ModuleName.COMMAND_POLL.name(), "<status_color>Command /poll");
                localizationTypes.put(ModuleName.COMMAND_ROCKPAPERSCISSORS.name(), "<status_color>Command /rockpaperscissors");
                localizationTypes.put(ModuleName.COMMAND_STREAM.name(), "<status_color>Command /stream");
                localizationTypes.put(ModuleName.COMMAND_TELL.name(), "<status_color>Command /tell");
                localizationTypes.put(ModuleName.COMMAND_TICTACTOE.name(), "<status_color>Command /tictactoe");
                localizationTypes.put(ModuleName.COMMAND_TRY.name(), "<status_color>Command /try");
                localizationTypes.put("DEATH", "<status_color>Death");
                localizationTypes.put(ModuleName.INTEGRATION_DISCORD.name(), "<status_color>Messages from Discord");
                localizationTypes.put(ModuleName.INTEGRATION_TELEGRAM.name(), "<status_color>Messages from Telegram");
                localizationTypes.put(ModuleName.INTEGRATION_TWITCH.name(), "<status_color>Messages from Twitch");
                localizationTypes.put(ModuleName.MESSAGE_JOIN.name(), "<status_color>Join");
                localizationTypes.put(ModuleName.MESSAGE_QUIT.name(), "<status_color>Quit");
                localizationTypes.put("SLEEP", "<status_color>Sleep");

                Localization defaultEnglishLocalization = fileLoaderProvider.get().getDefaultFiles().localizations().get(DefaultLocalization.ENGLISH.getName());

                newLocalizations.put(localization.language(), localization.withCommand(localization.command()
                                .withClearmail(defaultEnglishLocalization.command().clearmail())
                                .withMail(defaultEnglishLocalization.command().mail())
                                .withTell(defaultEnglishLocalization.command().tell())
                                .withTictactoe(defaultEnglishLocalization.command().tictactoe())
                                .withToponline(defaultEnglishLocalization.command().toponline())
                        )
                );
            }
        }

        return files.withLocalizations(newLocalizations);
    }

    public FilePack migration_1_7_0(FilePack files) {
        Map<String, List<String>> messageChannel = files.integration().discord().messageChannel();
        messageChannel.put(ModuleName.INTEGRATION_DISCORD.name(), List.of("123456"));
        messageChannel.put("CHAT_GLOBAL", List.of("123456"));
        files = files.withIntegration(files.integration().withDiscord(files.integration().discord().withMessageChannel(messageChannel)));

        Map<String, String> triggers = files.message().format().replacement().triggers();

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

        if (files.message().bubble().elevation() == 1) {
            files = files.withMessage(files.message().withBubble(files.message().bubble().withElevation(0.4f)));
        }

        Map<String, Localization> newLocalizations = new Object2ObjectArrayMap<>();

        for (Localization localization : files.localizations().values()) {
            Localization.Integration localizationIntegration = localization.integration();

            localizationIntegration.discord().messageChannel().put(ModuleName.INTEGRATION_DISCORD.name(), Localization.Integration.Discord.ChannelEmbed.builder().content("<fcolor:2><global_name> <fcolor:1>» <fcolor:4><message>").build());
            localizationIntegration.telegram().messageChannel().put(ModuleName.INTEGRATION_TELEGRAM.name(), "<fcolor:2><user_name> <fcolor:1>» <fcolor:4><message>");
            localizationIntegration.twitch().messageChannel().put(ModuleName.INTEGRATION_TWITCH.name(), "<fcolor:2><name> <fcolor:1>» <fcolor:4><message>");

            localizationIntegration.discord().infoChannel().forEach((key, value) ->
                    localizationIntegration.discord().infoChannel().put(key, Strings.CS.replace(value, "<tps>", "<replacement:tps>")));

            localizationIntegration.telegram().infoChannel().forEach((key, value) ->
                    localizationIntegration.telegram().infoChannel().put(key, Strings.CS.replace(value, "<tps>", "<replacement:tps>")));

            Localization.Message.Vanilla localizationVanilla = localization.message().vanilla();
            for (Map.Entry<String, String> entry : localizationVanilla.types().entrySet()) {
                localizationVanilla.types().put(entry.getKey(), Strings.CS.replace(entry.getValue(), "<arg_", "<argument:"));
            }

            boolean isRussian = localization.language().equalsIgnoreCase("ru_ru");

            if (isRussian) {
                localizationVanilla.types().put("commands.list.players", "<fcolor:1>\uD83D\uDC65 На сервере <fcolor:2><argument:0><fcolor:1> из <fcolor:2><argument:1><fcolor:1> игроков: <argument:2>");
                localizationVanilla.types().put("death.attack.spear", "<fcolor:1>☠ <argument:0> был проткнут <argument:1>");
                localizationVanilla.types().put("death.attack.spear.item", "<fcolor:1>☠ <argument:0> был проткнут <argument:1> с помощью <argument:2>");
            } else {
                localizationVanilla.types().put("commands.list.players", "<fcolor:1>\uD83D\uDC65 There are <fcolor:2><argument:0><fcolor:1> of a max of <fcolor:2><argument:1><fcolor:1> players online: <argument:2>");
                localizationVanilla.types().put("death.attack.spear", "<fcolor:1>☠ <argument:0> was speared by <argument:1>");
                localizationVanilla.types().put("death.attack.spear.item", "<fcolor:1>☠ <argument:0> was speared by <argument:1> using <argument:2>");
            }

            Localization.Command.Ping localizationPing = localization.command().ping();
            String oldPingFormat = localizationPing.format();

            Localization.Message.Update localizationUpdate = localization.message().update();
            String oldUpdateFormat = localizationUpdate.formatPlayer();

            localization = localization
                    .withCommand(localization.command()
                            .withPing(localization.command().ping().withFormat(Strings.CS.replace(oldPingFormat, "<ping>", "<replacement:ping>")))
                    )
                    .withMessage(localization.message()
                            .withUpdate(localization.message().update().withFormatPlayer(Strings.CS.replace(oldUpdateFormat, "<url:", "<replacement:url:")))
                    );

            String[] oldTags = new String[]{"ping", "tps", "online"};

            Localization.Message.Format.Replacement localizationReplacement = localization.message().format().replacement();
            for (String key : oldTags) {
                String value = localizationReplacement.values().get(key);
                if (value != null) {
                    localizationReplacement.values().put(key, Strings.CS.replace(value, "<" + key + ">", "<value>"));
                }
            }

            if (isRussian && localizationReplacement.values().containsKey("url")) {
                localizationReplacement.values().put("url", "<click:open_url:\"<message_1>\"><hover:show_text:\"<fcolor:2>Открыть ссылку <br><u><message_1>\"><fcolor:2><u>🗗 Ссылка</u></hover></click>");
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
            localization.message().sidebar().values().forEach(stringsWithOldTagsConsumer);
            localization.message().tab().footer().lists().forEach(stringsWithOldTagsConsumer);
            localization.message().tab().header().lists().forEach(stringsWithOldTagsConsumer);

            newLocalizations.put(localization.language(), localization);
        }

        return files.withLocalizations(newLocalizations);
    }

    public FilePack migration_1_7_1(FilePack files) {
        List<Message.Vanilla.VanillaMessage> vanillaMessages = new ArrayList<>();

        for (Message.Vanilla.VanillaMessage vanillaMessage : files.message().vanilla().types()) {
            if (!vanillaMessage.name().equals("DEATH")) {
                vanillaMessages.add(vanillaMessage);
                continue;
            }

            List<String> translationKeys = new ArrayList<>(vanillaMessage.translationKeys());
            if (!translationKeys.contains("death.attack.spear")) {
                translationKeys.add("death.attack.spear");
            }

            if (!translationKeys.contains("death.attack.spear.item")) {
                translationKeys.add("death.attack.spear.item");
            }

            vanillaMessages.add(vanillaMessage.withTranslationKeys(translationKeys));
        }

        Map<CacheName, Config.Cache.CacheSetting> types = new EnumMap<>(files.config().cache().types());
        types.put(CacheName.COOLDOWN, new Config.Cache.CacheSetting(false, 5, TimeUnit.HOURS, 5000));

        return files
                .withConfig(files.config().withCache(files.config().cache().withTypes(types)))
                .withMessage(files.message().withVanilla(files.message().vanilla().withTypes(vanillaMessages)));
    }

    public FilePack migration_1_7_2(FilePack files) {
        Map<String, Localization> newLocalizations = new Object2ObjectArrayMap<>();

        for (Localization localization : files.localizations().values()) {

            String newDisplay = Strings.CS.replace(localization.message().format().names().display().getFirst(), "<player_head>", "<white><player_head></white>");
            String newPlayerlistname = Strings.CS.replace(localization.message().tab().playerlistname().format(), "<player_head>", "<white><player_head></white>");

            newLocalizations.put(localization.language(),
                    localization.withMessage(localization.message()
                            .withFormat(localization.message().format().withNames(localization.message().format().names().withDisplay(List.of(newDisplay))))
                            .withTab(localization.message().tab().withPlayerlistname(localization.message().tab().playerlistname().withFormat(newPlayerlistname)))
                    )
            );
        }

        return files.withLocalizations(newLocalizations);
    }

    public FilePack migration_1_7_4(FilePack files) {
        Command.Chatsetting.Menu menu = files.command().chatsetting().menu();

        UnaryOperator<List<Command.Chatsetting.Menu.Color.Type>> migrateTypeOperator = types -> {
            List<Command.Chatsetting.Menu.Color.Type> newTypes = new LinkedList<>();

            for (Command.Chatsetting.Menu.Color.Type type : types) {
                if ("default".equals(type.name())) {
                    newTypes.add(type);
                    continue;
                }

                newTypes.add(Command.Chatsetting.Menu.Color.Type.builder()
                        .name(type.name())
                        .material(type.material())
                        .colors(type.colors().entrySet().stream()
                                .collect(Collectors.toMap(
                                                Map.Entry::getKey,
                                                entry -> StringUtils.isEmpty(entry.getValue()) ? "null" : entry.getValue(),
                                                (object, _) -> object,
                                                LinkedHashMap::new
                                        )
                                ))
                        .build()
                );

            }

            return newTypes;
        };

        return files.withCommand(
                files.command().withChatsetting(
                        files.command().chatsetting().withMenu(menu
                                .withSee(menu.see().withTypes(migrateTypeOperator.apply(menu.see().types())))
                                .withOut(menu.out().withTypes(migrateTypeOperator.apply(menu.out().types())))
                        )
                )
        );
    }

    public FilePack migration_1_7_5(FilePack files) {
        Map<String, Localization> newLocalizations = new Object2ObjectArrayMap<>();

        UnaryOperator<String> replaceDisplayName = string -> Strings.CS.replace(string, "<display_name>", "<target>");

        for (Localization localization : files.localizations().values()) {
            newLocalizations.put(localization.language(),
                    localization
                            .withCommand(
                                    localization.command()
                                            .withBanlist(localization.command().banlist()
                                                    .withGlobal(localization.command().banlist().global().withLine(replaceDisplayName.apply(localization.command().banlist().global().line())))
                                                    .withPlayer(localization.command().banlist().player().withLine(replaceDisplayName.apply(localization.command().banlist().player().line())))
                                            )
                                            .withGeolocate(localization.command().geolocate().withFormat(replaceDisplayName.apply(localization.command().geolocate().format())))
                                            .withIgnore(localization.command().ignore()
                                                    .withFormatFalse(replaceDisplayName.apply(localization.command().ignore().formatFalse()))
                                                    .withFormatTrue(replaceDisplayName.apply(localization.command().ignore().formatTrue()))
                                            )
                                            .withIgnorelist(localization.command().ignorelist().withLine(replaceDisplayName.apply(localization.command().ignorelist().line())))
                                            .withMutelist(localization.command().mutelist()
                                                    .withGlobal(localization.command().mutelist().global().withLine(replaceDisplayName.apply(localization.command().mutelist().global().line())))
                                                    .withPlayer(localization.command().mutelist().player().withLine(replaceDisplayName.apply(localization.command().mutelist().player().line())))
                                            )
                                            .withOnline(localization.command().online()
                                                    .withFormatCurrent(replaceDisplayName.apply(localization.command().online().formatCurrent()))
                                                    .withFormatFirst(replaceDisplayName.apply(localization.command().online().formatFirst()))
                                                    .withFormatLast(replaceDisplayName.apply(localization.command().online().formatLast()))
                                                    .withFormatTotal(replaceDisplayName.apply(localization.command().online().formatTotal()))
                                            )
                                            .withWarnlist(localization.command().warnlist()
                                                    .withGlobal(localization.command().warnlist().global().withLine(replaceDisplayName.apply(localization.command().warnlist().global().line())))
                                                    .withPlayer(localization.command().warnlist().player().withLine(replaceDisplayName.apply(localization.command().warnlist().player().line())))
                                            )
                            )
                            .withMessage(
                                    localization.message()
                                            .withRightclick(localization.message().rightclick().withFormat(replaceDisplayName.apply(localization.message().rightclick().format())))
                            )
            );
        }

        return files.withLocalizations(newLocalizations);
    }

    public FilePack migration_1_8_2(FilePack files) {
        Map<String, Localization> newLocalizations = new Object2ObjectArrayMap<>();

        UnaryOperator<String> replaceOldTags = string -> StringUtils.replaceEach(string,
                new String[]{"<afk_suffix>", "<world_prefix>", "<mute_suffix>", "<stream_prefix>", "<vault_suffix>", "<vault_prefix>", "<translate>"},
                new String[]{"<afk>", "<world>", "<mute>", "<stream>", "<suffix>", "<prefix>", "<translation>"}
        );

        boolean isHytale = platformServerAdapterProvider.get().getPlatformType() == PlatformType.HYTALE;

        for (Localization localization : files.localizations().values()) {

            Map<String, String> newChats = new HashMap<>(localization.message().chat().types());
            newChats.forEach((key, value) ->
                    newChats.put(key, replaceOldTags.apply(value))
            );

            List<String> newDisplays = new ArrayList<>(localization.message().format().names().display());
            newDisplays.replaceAll(replaceOldTags);

            String newPlayerListname = replaceOldTags.apply(localization.message().tab().playerlistname().format());

            localization = localization
                    .withMessage(localization.message()
                            .withChat(localization.message().chat()
                                    .withTypes(newChats)
                            )
                            .withFormat(localization.message().format()
                                    .withNames(localization.message().format().names()
                                            .withDisplay(newDisplays)
                                    )
                            )
                            .withTab(localization.message().tab()
                                    .withPlayerlistname(localization.message().tab().playerlistname()
                                            .withFormat(newPlayerListname)
                                    )
                            )
                    )
                    .withCommand(localization.command()
                            .withChatsetting(localization.command().chatsetting()
                                    .withCheckbox(localization.command().chatsetting().checkbox()
                                            .withTypes(
                                                    replaceOldMessageName(localization.command().chatsetting().checkbox().types())
                                            )
                                    )
                            )
                            .withFlectonepulse(localization.command().flectonepulse()
                                    .withFormatFalse(
                                            Strings.CS.replace(localization.command().flectonepulse().formatFalse(), "<message>", "<error>")
                                    )
                            )
                    )
                    .withIntegration(localization.integration()
                            .withDiscord(localization.integration().discord()
                                    .withMessageChannel(
                                            replaceOldMessageName(localization.integration().discord().messageChannel())
                                    )
                            )
                            .withTelegram(localization.integration().telegram()
                                    .withMessageChannel(
                                            replaceOldMessageName(localization.integration().telegram().messageChannel())
                                    )
                            )
                            .withTwitch(localization.integration().twitch()
                                    .withMessageChannel(
                                            replaceOldMessageName(localization.integration().twitch().messageChannel())
                                    )
                            )
                    );

            if (isHytale) {
                Map<String, String> types = new HashMap<>(localization.message().vanilla().types());
                types.put("server.assetModule.outOfDatePacks", "");
                types.put("server.pluginManager.outOfDatePlugins", "");

                localization = localization.withMessage(
                        localization.message().withVanilla(
                                localization.message().vanilla().withTypes(types)
                        )
                );
            }

            newLocalizations.put(localization.language(), localization);
        }

        return files
                .withLocalizations(newLocalizations)
                .withCommand(files.command()
                        .withChatsetting(files.command().chatsetting()
                                .withCheckbox(files.command().chatsetting().checkbox()
                                        .withTypes(
                                                replaceOldMessageName(files.command().chatsetting().checkbox().types())
                                        )
                                )
                        )
                )
                .withIntegration(files.integration()
                        .withDiscord(files.integration().discord()
                                .withMessageChannel(
                                        replaceOldMessageName(files.integration().discord().messageChannel())
                                )
                        )
                        .withTelegram(files.integration().telegram()
                                .withMessageChannel(
                                        replaceOldMessageName(files.integration().telegram().messageChannel())
                                )
                        )
                        .withTwitch(files.integration().twitch()
                                .withMessageChannel(
                                        replaceOldMessageName(files.integration().twitch().messageChannel())
                                )
                        )
                )
                .withMessage(files.message()
                        .withFormat(files.message().format()
                                .withScoreboard(files.message().format().scoreboard()
                                        .withPrefix(replaceOldTags.apply(files.message().format().scoreboard().prefix()))
                                        .withSuffix(replaceOldTags.apply(files.message().format().scoreboard().suffix()))
                                )
                        )
                        .withStatus(files.message().status()
                                .withPlayers(files.message().status().players()
                                        .withOnline("-69".equals(files.message().status().players().online()) ? "" : files.message().status().players().online())
                                )
                        )
                )
                .withPermission(files.permission()
                        .withCommand(files.permission().command()
                                .withChatsetting(files.permission().command().chatsetting()
                                        .withSettings(
                                                replaceOldMessageName(files.permission().command().chatsetting().settings())
                                        )
                                )
                        )
                );
    }

    private <T> Map<String, T> replaceOldMessageName(Map<String, T> oldMap) {
        Map<String, T> newMap = new HashMap<>(oldMap);

        oldMap.entrySet().stream()
                .filter(entry -> entry.getKey().startsWith("CHAT_"))
                .forEach(entry -> newMap.put("MESSAGE_" + entry.getKey(), newMap.remove(entry.getKey())));

        if (newMap.containsKey("AFK")) {
            newMap.put("MESSAGE_AFK", newMap.remove("AFK"));
        }

        if (newMap.containsKey("AUTO")) {
            newMap.put("MESSAGE_AUTO", newMap.remove("AUTO"));
        }

        if (newMap.containsKey("BOSSBAR")) {
            newMap.put("MESSAGE_BOSSBAR", newMap.remove("BOSSBAR"));
        }

        if (newMap.containsKey("BRAND")) {
            newMap.put("MESSAGE_BRAND", newMap.remove("BRAND"));
        }

        if (newMap.containsKey("CHAT")) {
            newMap.put("MESSAGE_CHAT", newMap.remove("CHAT"));
        }

        if (newMap.containsKey("DELETE")) {
            newMap.put("MESSAGE_FORMAT_MODERATION_DELETE", newMap.remove("DELETE"));
        }

        if (newMap.containsKey("FOOTER")) {
            newMap.put("MESSAGE_TAB_FOOTER", newMap.remove("FOOTER"));
        }

        if (newMap.containsKey("FORMAT")) {
            newMap.put("MESSAGE_FORMAT", newMap.remove("FORMAT"));
        }

        if (newMap.containsKey("FROM_DISCORD_TO_MINECRAFT")) {
            newMap.put("INTEGRATION_DISCORD", newMap.remove("FROM_DISCORD_TO_MINECRAFT"));
        }

        if (newMap.containsKey("FROM_TELEGRAM_TO_MINECRAFT")) {
            newMap.put("INTEGRATION_TELEGRAM", newMap.remove("FROM_TELEGRAM_TO_MINECRAFT"));
        }

        if (newMap.containsKey("FROM_TWITCH_TO_MINECRAFT")) {
            newMap.put("INTEGRATION_TWITCH", newMap.remove("FROM_TWITCH_TO_MINECRAFT"));
        }

        if (newMap.containsKey("GREETING")) {
            newMap.put("MESSAGE_GREETING", newMap.remove("GREETING"));
        }

        if (newMap.containsKey("HEADER")) {
            newMap.put("MESSAGE_TAB_HEADER", newMap.remove("HEADER"));
        }

        if (newMap.containsKey("JOIN")) {
            newMap.put("MESSAGE_JOIN", newMap.remove("JOIN"));
        }

        if (newMap.containsKey("MENTION")) {
            newMap.put("MESSAGE_FORMAT_MENTION", newMap.remove("MENTION"));
        }

        if (newMap.containsKey("MOTD")) {
            newMap.put("MESSAGE_STATUS_MOTD", newMap.remove("MOTD"));
        }

        if (newMap.containsKey("NAME")) {
            newMap.put("MESSAGE_FORMAT_NAMES", newMap.remove("NAME"));
        }

        if (newMap.containsKey("NEWBIE")) {
            newMap.put("MESSAGE_FORMAT_MODERATION_NEWBIE", newMap.remove("NEWBIE"));
        }

        if (newMap.containsKey("OBJECTIVE")) {
            newMap.put("MESSAGE_OBJECTIVE", newMap.remove("OBJECTIVE"));
        }

        if (newMap.containsKey("TABNAME")) {
            newMap.put("MESSAGE_TAB_PLAYERLISTNAME", newMap.remove("TABNAME"));
        }

        if (newMap.containsKey("PLAYERS")) {
            newMap.put("MESSAGE_STATUS_PLAYERS", newMap.remove("PLAYERS"));
        }

        if (newMap.containsKey("PLAYERLISTNAME")) {
            newMap.put("MESSAGE_TAB_PLAYERLISTNAME", newMap.remove("PLAYERLISTNAME"));
        }

        if (newMap.containsKey("QUESTION_ANSWER")) {
            newMap.put("MESSAGE_FORMAT_QUESTIONANSWER", newMap.remove("QUESTION_ANSWER"));
        }

        if (newMap.containsKey("QUIT")) {
            newMap.put("MESSAGE_QUIT", newMap.remove("QUIT"));
        }

        if (newMap.containsKey("OBJECT")) {
            newMap.put("MESSAGE_FORMAT_OBJECT", newMap.remove("OBJECT"));
        }

        if (newMap.containsKey("REPLACEMENT")) {
            newMap.put("MESSAGE_FORMAT_REPLACEMENT", newMap.remove("REPLACEMENT"));
        }

        if (newMap.containsKey("RIGHT_CLICK")) {
            newMap.put("MESSAGE_RIGHTCLICK", newMap.remove("RIGHT_CLICK"));
        }

        if (newMap.containsKey("SIDEBAR")) {
            newMap.put("MESSAGE_SIDEBAR", newMap.remove("SIDEBAR"));
        }

        if (newMap.containsKey("SWEAR")) {
            newMap.put("MESSAGE_FORMAT_MODERATION_SWEAR", newMap.remove("SWEAR"));
        }

        if (newMap.containsKey("TRANSLATE")) {
            newMap.put("MESSAGE_FORMAT_TRANSLATE", newMap.remove("TRANSLATE"));
        }

        if (newMap.containsKey("UPDATE")) {
            newMap.put("MESSAGE_UPDATE", newMap.remove("UPDATE"));
        }

        if (newMap.containsKey("VANILLA")) {
            newMap.put("MESSAGE_VANILLA", newMap.remove("VANILLA"));
        }

        if (newMap.containsKey("VERSION")) {
            newMap.put("MESSAGE_STATUS_VERSION", newMap.remove("VERSION"));
        }

        return newMap;
    }

    public FilePack migration_1_9_1(FilePack files) {
        Map<CacheName, Config.Cache.CacheSetting> types = new EnumMap<>(files.config().cache().types());
        types.put(CacheName.ICU_MESSAGE, new Config.Cache.CacheSetting(false, 10, TimeUnit.MINUTES, 100000));

        return files.withConfig(files.config().withCache(files.config().cache().withTypes(types)));
    }

}
