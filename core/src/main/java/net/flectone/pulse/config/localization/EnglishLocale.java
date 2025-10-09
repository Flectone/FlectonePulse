package net.flectone.pulse.config.localization;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.util.constant.MessageType;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

@Singleton
public class EnglishLocale implements Locale {

    @Inject
    public EnglishLocale() {
    }

    @Override
    public void init(Localization localization) {
        localization.cooldown = "<color:#ff7171><b>⁉</b> Too fast, you'll be able to use it in <time>";

        localization.time.format = "dd'd' HH'h' mm'm' ss.SSS's'";
        localization.time.permanent = "PERMANENT";
        localization.time.zero = "0s";

        localization.command.anon.format = "<fcolor:1>\uD83D\uDC7B <fcolor:2>Anon <fcolor:1><message>";

        localization.command.ball.format = "<color:#9370DB>❓ <display_name> asked: <message><reset> <color:#9370DB><br>\uD83D\uDD2E Ball answered: <u><answer></u>";
        localization.command.ball.answers = new LinkedList<>() {{
            add("Undeniably");
            add("No doubt about it");
            add("Definitely yes");
            add("That's the base");
            add("You can be sure of it");
            add("Most likely");
            add("Good prospects");
            add("Yes");
            add("It's not clear yet, try again");
            add("Ask later");
            add("It's better not to tell");
            add("Can't predict now");
            add("Concentrate and ask again");
            add("Don't even think about it");
            add("No.");
            add("The prospects are not good");
            add("Very doubtful");
        }};

        localization.command.ban.nullPlayer = "<color:#ff7171><b>⁉</b> This player does not exist";
        localization.command.ban.nullTime = "<color:#ff7171><b>⁉</b> Incorrect time";
        localization.command.ban.reasons = new Localization.ReasonMap() {{
            put("default", "You have been banned from this server");
        }};
        localization.command.ban.server = "<color:#ff7171>🔒 <fcolor:2><moderator> <fcolor:1>has banned <fcolor:2><player> <fcolor:1><hover:show_text:\"<fcolor:1>ID: <id><br>Date: <date><br>Time: <time><br>Remaining time: <time_left><br>Moderator: <moderator><br>Reason: <reason>\">[INFO]</hover>";
        localization.command.ban.person = "<color:#ff7171>🔒 BAN 🔒<fcolor:1><br><br>Date: <date><br><br>Time: <time><br><br>Remaining time: <time_left><br><br>Moderator: <moderator><br><br>Reason: <reason>";
        localization.command.ban.connectionAttempt = "<color:#ff7171>🔒 Banned <fcolor:2><player> <fcolor:1>tried to log in <hover:show_text:\"<fcolor:1>ID: <id><br>Date: <date><br>Time: <time><br>Remaining time: <time_left><br>Moderator: <moderator><br>Reason: <reason>\">[INFO]</hover>";

        localization.command.banlist.empty = "<color:#98FB98>☺ No bans found";
        localization.command.banlist.nullPage = "<color:#ff7171><b>⁉</b> This page doesn't exist";
        localization.command.banlist.nullPlayer = "<color:#ff7171><b>⁉</b> This player does not exist";
        localization.command.banlist.global.header = "<fcolor:2>▋ Bans: <count> <br>";
        localization.command.banlist.global.line = "<hover:show_text:\"<fcolor:1>Click to unban <display_name>\"><click:run_command:\"<command>\"><color:#ff7171>☒ <display_name></click></hover> <fcolor:1><hover:show_text:\"<fcolor:1>ID: <id><br>Date: <date><br>Time: <time><br>Moderator: <moderator><br>Reason: <reason>\">[INFO]</hover>";
        localization.command.banlist.global.footer = "<br><fcolor:2>▋ <fcolor:2><click:run_command:\"<command> <prev_page>\">←</click> <fcolor:1>Page: <current_page>/<last_page> <fcolor:2><click:run_command:\"<command> <next_page>\">→";
        localization.command.banlist.player.header = "<fcolor:2>▋ All bans: <count> <br>";
        localization.command.banlist.player.line = "<hover:show_text:\"<fcolor:1>Click to unban <display_name>\"><click:run_command:\"<command>\"><color:#ff7171>☒ <display_name></click></hover> <fcolor:1><hover:show_text:\"<fcolor:1>ID: <id><br>Date: <date><br>Time: <time><br>Moderator: <moderator><br>Reason: <reason>\">[INFO]</hover>";
        localization.command.banlist.player.footer = "<br><fcolor:2>▋ <click:run_command:\"<command> <prev_page>\">←</click> <fcolor:1>Page: <current_page>/<last_page> <fcolor:2><click:run_command:\"<command> <next_page>\">→";

        localization.command.broadcast.format = "<br><color:#ffd500>│ \uD83D\uDD6B Message for everyone <br>│<br>│ Author <display_name> <br>│<br>│ <fcolor:1><message> <br>";

        localization.command.chatcolor.nullPlayer = "<color:#ff7171><b>⁉</b> This player does not exist";
        localization.command.chatcolor.nullType = "<color:#ff7171><b>⁉</b> Incorrect type entered";
        localization.command.chatcolor.nullColor = "<color:#ff7171><b>⁉</b> Incorrect colors entered";
        localization.command.chatcolor.format = "<br><color:#98FB98>│ Your colors: <br><color:#98FB98>│ <fcolor:1>(local) <fcolor:2><player><fcolor:3>: hello world! <br><color:#98FB98>│ <fcolor:1>(global) <fcolor:2><player><fcolor:4>: hello world! <br>";

        localization.command.chatsetting.noPermission = "<color:#ff7171><b>⁉</b> No permission to change this setting";
        localization.command.chatsetting.disabledSelf = "<color:#ff7171><b>⁉</b> This feature is disabled via /chatsetting";
        localization.command.chatsetting.disabledOther = "<color:#ff7171><b>⁉</b> He disabled this feature via /chatsetting";
        localization.command.chatsetting.inventory = "Chat Settings";
        localization.command.chatsetting.checkbox.enabledColor = "<color:#98FB98>";
        localization.command.chatsetting.checkbox.enabledHover = "<color:#98FB98>Display enabled";
        localization.command.chatsetting.checkbox.disabledColor = "<color:#ff7171>";
        localization.command.chatsetting.checkbox.disabledHover = "<color:#ff7171>Display disabled";
        localization.command.chatsetting.checkbox.types = new LinkedHashMap<>() {{
            put(MessageType.AFK.name(), "<status_color>Afk");
            put(MessageType.ADVANCEMENT.name(), "<status_color>Advancement");
            put(MessageType.CHAT.name(), "<status_color>Chat messages");
            put(MessageType.COMMAND_ANON.name(), "<status_color>Command /anon");
            put(MessageType.COMMAND_BALL.name(), "<status_color>Command /ball");
            put(MessageType.COMMAND_BROADCAST.name(), "<status_color>Command /broadcast");
            put(MessageType.COMMAND_COIN.name(), "<status_color>Command /coin");
            put(MessageType.COMMAND_DICE.name(), "<status_color>Command /dice");
            put(MessageType.COMMAND_DO.name(), "<status_color>Command /do");
            put(MessageType.COMMAND_MAIL.name(), "<status_color>Command /mail");
            put(MessageType.COMMAND_ME.name(), "<status_color>Command /me");
            put(MessageType.COMMAND_POLL.name(), "<status_color>Command /poll");
            put(MessageType.COMMAND_ROCKPAPERSCISSORS.name(), "<status_color>Command /rockpaperscissors");
            put(MessageType.COMMAND_STREAM.name(), "<status_color>Command /stream");
            put(MessageType.COMMAND_TELL.name(), "<status_color>Command /tell");
            put(MessageType.COMMAND_TICTACTOE.name(), "<status_color>Command /tictactoe");
            put(MessageType.COMMAND_TRY.name(), "<status_color>Command /try");
            put(MessageType.DEATH.name(), "<status_color>Death");
            put(MessageType.FROM_DISCORD_TO_MINECRAFT.name(), "<status_color>Messages from Discord");
            put(MessageType.FROM_TELEGRAM_TO_MINECRAFT.name(), "<status_color>Messages from Telegram");
            put(MessageType.FROM_TWITCH_TO_MINECRAFT.name(), "<status_color>Messages from Twitch");
            put(MessageType.JOIN.name(), "<status_color>Join");
            put(MessageType.QUIT.name(), "<status_color>Quit");
            put(MessageType.SLEEP.name(), "<status_color>Sleep");
        }};
        localization.command.chatsetting.menu.chat.item = "<fcolor:2>Chat type <br><fcolor:1>Chat for viewing and sending messages <br><br><fcolor:1>Your chat is <fcolor:2><chat>";
        localization.command.chatsetting.menu.chat.inventory = "Chats";
        localization.command.chatsetting.menu.chat.types = new LinkedHashMap<>() {{
            put("default", "<fcolor:2>Default chat<br><fcolor:1>You can see <fcolor:2>all <fcolor:1>chats and write to any chat");
            put("local", "<fcolor:2>Local chat<br><fcolor:1>You can write to <fcolor:2>any <fcolor:1>chats");
            put("global", "<fcolor:2>Global chat<br><fcolor:1>You can only write to <fcolor:2>global <fcolor:1>chat");
        }};
        localization.command.chatsetting.menu.see.item = "<fcolor:2>Colors \"see\" <br><fcolor:1>Colors for /chatcolor see <br><br><fcolor:1>(local) <fcolor:2><player><fcolor:3>: hello world! <br><fcolor:1>(global) <fcolor:2><player><fcolor:4>: hello world! <br><br><u><color:#ff7171>Only you see it in all messages";
        localization.command.chatsetting.menu.see.inventory = "Colors";
        localization.command.chatsetting.menu.see.types = new LinkedHashMap<>() {{
            put("default", "<gradient:#70C7EF:#37B1F2>Default colors<br><fcolor:1>(local) <fcolor:2><player><fcolor:3>: hello world! <br><fcolor:1>(global) <fcolor:2><player><fcolor:4>: hello world!");
            put("white", "<gradient:#D4E4FF:#B8D2FF>White colors<br><fcolor:1>(local) <fcolor:2><player><fcolor:3>: hello world! <br><fcolor:1>(global) <fcolor:2><player><fcolor:4>: hello world!");
            put("light_gray", "<gradient:#B5B9BD:#9DA2A6>Light gray colors<br><fcolor:1>(local) <fcolor:2><player><fcolor:3>: hello world! <br><fcolor:1>(global) <fcolor:2><player><fcolor:4>: hello world!");
            put("gray", "<gradient:#4A5054:#3A3F42>Gray colors<br><fcolor:1>(local) <fcolor:2><player><fcolor:3>: hello world! <br><fcolor:1>(global) <fcolor:2><player><fcolor:4>: hello world!");
            put("black", "<gradient:#17191A:#0D0E0F>Black colors<br><fcolor:1>(local) <fcolor:2><player><fcolor:3>: hello world! <br><fcolor:1>(global) <fcolor:2><player><fcolor:4>: hello world!");
            put("brown", "<gradient:#7A5A40:#634A34>Brown colors<br><fcolor:1>(local) <fcolor:2><player><fcolor:3>: hello world! <br><fcolor:1>(global) <fcolor:2><player><fcolor:4>: hello world!");
            put("red", "<gradient:#D63E3E:#C12B2B>Red colors<br><fcolor:1>(local) <fcolor:2><player><fcolor:3>: hello world! <br><fcolor:1>(global) <fcolor:2><player><fcolor:4>: hello world!");
            put("orange", "<gradient:#FF8C00:#E67E00>Orange colors<br><fcolor:1>(local) <fcolor:2><player><fcolor:3>: hello world! <br><fcolor:1>(global) <fcolor:2><player><fcolor:4>: hello world!");
            put("yellow", "<gradient:#FFE83D:#FFD900>Yellow colors<br><fcolor:1>(local) <fcolor:2><player><fcolor:3>: hello world! <br><fcolor:1>(global) <fcolor:2><player><fcolor:4>: hello world!");
            put("lime", "<gradient:#8EE53F:#7ACC29>Lime colors<br><fcolor:1>(local) <fcolor:2><player><fcolor:3>: hello world! <br><fcolor:1>(global) <fcolor:2><player><fcolor:4>: hello world!");
            put("green", "<gradient:#4BB54B:#3AA33A>Green colors<br><fcolor:1>(local) <fcolor:2><player><fcolor:3>: hello world! <br><fcolor:1>(global) <fcolor:2><player><fcolor:4>: hello world!");
            put("cyan", "<gradient:#3ECFDF:#2AB7C9>Cyan colors<br><fcolor:1>(local) <fcolor:2><player><fcolor:3>: hello world! <br><fcolor:1>(global) <fcolor:2><player><fcolor:4>: hello world!");
            put("light_blue", "<gradient:#6BB6FF:#4DA6FF>Light blue colors<br><fcolor:1>(local) <fcolor:2><player><fcolor:3>: hello world! <br><fcolor:1>(global) <fcolor:2><player><fcolor:4>: hello world!");
            put("blue", "<gradient:#3A75FF:#1F5AFF>Blue colors<br><fcolor:1>(local) <fcolor:2><player><fcolor:3>: hello world! <br><fcolor:1>(global) <fcolor:2><player><fcolor:4>: hello world!");
            put("purple", "<gradient:#A368C7:#8A4DBF>Purple colors<br><fcolor:1>(local) <fcolor:2><player><fcolor:3>: hello world! <br><fcolor:1>(global) <fcolor:2><player><fcolor:4>: hello world!");
            put("magenta", "<gradient:#FF5CD9:#FF3DCF>Magenta colors<br><fcolor:1>(local) <fcolor:2><player><fcolor:3>: hello world! <br><fcolor:1>(global) <fcolor:2><player><fcolor:4>: hello world!");
            put("pink", "<gradient:#FF80B7:#FF66A6>Pink colors<br><fcolor:1>(local) <fcolor:2><player><fcolor:3>: hello world! <br><fcolor:1>(global) <fcolor:2><player><fcolor:4>: hello world!");
        }};
        localization.command.chatsetting.menu.out.item = "<fcolor:2>Colors \"out\" <br><fcolor:1>Colors for /chatcolor out <br><br><fcolor:1>(local) <fcolor:2><player><fcolor:3>: hello world! <br><fcolor:1>(global) <fcolor:2><player><fcolor:4>: hello world! <br><br><u><color:#ff7171>Everyone sees it in your messages";
        localization.command.chatsetting.menu.out.inventory = "Colors";
        localization.command.chatsetting.menu.out.types = new LinkedHashMap<>() {{
            put("default", "<gradient:#70C7EF:#37B1F2>Default colors<br><fcolor:1>(local) <fcolor:2><player><fcolor:3>: hello world! <br><fcolor:1>(global) <fcolor:2><player><fcolor:4>: hello world!");
            put("white", "<gradient:#D4E4FF:#B8D2FF>White colors<br><fcolor:1>(local) <fcolor:2><player><fcolor:3>: hello world! <br><fcolor:1>(global) <fcolor:2><player><fcolor:4>: hello world!");
            put("light_gray", "<gradient:#B5B9BD:#9DA2A6>Light gray colors<br><fcolor:1>(local) <fcolor:2><player><fcolor:3>: hello world! <br><fcolor:1>(global) <fcolor:2><player><fcolor:4>: hello world!");
            put("gray", "<gradient:#4A5054:#3A3F42>Gray colors<br><fcolor:1>(local) <fcolor:2><player><fcolor:3>: hello world! <br><fcolor:1>(global) <fcolor:2><player><fcolor:4>: hello world!");
            put("black", "<gradient:#17191A:#0D0E0F>Black colors<br><fcolor:1>(local) <fcolor:2><player><fcolor:3>: hello world! <br><fcolor:1>(global) <fcolor:2><player><fcolor:4>: hello world!");
            put("brown", "<gradient:#7A5A40:#634A34>Brown colors<br><fcolor:1>(local) <fcolor:2><player><fcolor:3>: hello world! <br><fcolor:1>(global) <fcolor:2><player><fcolor:4>: hello world!");
            put("red", "<gradient:#D63E3E:#C12B2B>Red colors<br><fcolor:1>(local) <fcolor:2><player><fcolor:3>: hello world! <br><fcolor:1>(global) <fcolor:2><player><fcolor:4>: hello world!");
            put("orange", "<gradient:#FF8C00:#E67E00>Orange colors<br><fcolor:1>(local) <fcolor:2><player><fcolor:3>: hello world! <br><fcolor:1>(global) <fcolor:2><player><fcolor:4>: hello world!");
            put("yellow", "<gradient:#FFE83D:#FFD900>Yellow colors<br><fcolor:1>(local) <fcolor:2><player><fcolor:3>: hello world! <br><fcolor:1>(global) <fcolor:2><player><fcolor:4>: hello world!");
            put("lime", "<gradient:#8EE53F:#7ACC29>Lime colors<br><fcolor:1>(local) <fcolor:2><player><fcolor:3>: hello world! <br><fcolor:1>(global) <fcolor:2><player><fcolor:4>: hello world!");
            put("green", "<gradient:#4BB54B:#3AA33A>Green colors<br><fcolor:1>(local) <fcolor:2><player><fcolor:3>: hello world! <br><fcolor:1>(global) <fcolor:2><player><fcolor:4>: hello world!");
            put("cyan", "<gradient:#3ECFDF:#2AB7C9>Cyan colors<br><fcolor:1>(local) <fcolor:2><player><fcolor:3>: hello world! <br><fcolor:1>(global) <fcolor:2><player><fcolor:4>: hello world!");
            put("light_blue", "<gradient:#6BB6FF:#4DA6FF>Light blue colors<br><fcolor:1>(local) <fcolor:2><player><fcolor:3>: hello world! <br><fcolor:1>(global) <fcolor:2><player><fcolor:4>: hello world!");
            put("blue", "<gradient:#3A75FF:#1F5AFF>Blue colors<br><fcolor:1>(local) <fcolor:2><player><fcolor:3>: hello world! <br><fcolor:1>(global) <fcolor:2><player><fcolor:4>: hello world!");
            put("purple", "<gradient:#A368C7:#8A4DBF>Purple colors<br><fcolor:1>(local) <fcolor:2><player><fcolor:3>: hello world! <br><fcolor:1>(global) <fcolor:2><player><fcolor:4>: hello world!");
            put("magenta", "<gradient:#FF5CD9:#FF3DCF>Magenta colors<br><fcolor:1>(local) <fcolor:2><player><fcolor:3>: hello world! <br><fcolor:1>(global) <fcolor:2><player><fcolor:4>: hello world!");
            put("pink", "<gradient:#FF80B7:#FF66A6>Pink colors<br><fcolor:1>(local) <fcolor:2><player><fcolor:3>: hello world! <br><fcolor:1>(global) <fcolor:2><player><fcolor:4>: hello world!");
        }};

        localization.command.clearchat.nullPlayer = "<color:#ff7171><b>⁉</b> This player does not exist";
        localization.command.clearchat.format = "<fcolor:1>💬 Chat is cleared";

        localization.command.clearmail.nullMail = "<color:#ff7171><b>⁉</b> This mail does not exist";
        localization.command.clearmail.format = "<fcolor:2>✉ [REMOVED] Mail #<id> for <target> » <fcolor:1><message>";

        localization.command.coin.head = "heads";
        localization.command.coin.tail = "tails";
        localization.command.coin.format = "<fcolor:1>✎ <display_name> player got <result>";
        localization.command.coin.formatDraw = "<fcolor:1>✎ <display_name> player got edge :)";

        localization.command.deletemessage.nullMessage = "<color:#ff7171><b>⁉</b> This message does not exist";
        localization.command.deletemessage.format = "<color:#98FB98>☒ Successfully deleted message";

        localization.command.dice.format = "<fcolor:1>✎ <display_name> roll <message><reset> <fcolor:1>(<sum>)";
        localization.command.dice.symbols = new LinkedHashMap<>() {{
            put(1, "⚀");
            put(2, "⚁");
            put(3, "⚂");
            put(4, "⚃");
            put(5, "⚄");
            put(6, "⚅");
        }};

        localization.command.Do.format = "<fcolor:1>✎ <message><reset> <fcolor:1>(<i><display_name></i>)";

        localization.command.exception.execution = "<color:#ff7171><b>⁉</b> An error occurred while executing the command <br><color:#ff7171><b>⁉</b> <exception>";
        localization.command.exception.syntax = "<hover:show_text:\"<fcolor:2>Use <br><fcolor:1>/<correct_syntax>\"><click:suggest_command:\"/<command> \"><fcolor:2>┌<br>│ Usage →<br>│ <fcolor:1>/<correct_syntax><br><fcolor:2>└";
        localization.command.exception.parseUnknown = "<color:#ff7171><b>⁉</b> Unknown argument error while parsing <br><input>";
        localization.command.exception.parseBoolean = "<color:#ff7171><b>⁉</b> A boolean argument was expected, but you entered <br><input>";
        localization.command.exception.parseNumber = "<color:#ff7171><b>⁉</b> A number argument was expected, but you entered <br><input>";
        localization.command.exception.parseString = "<color:#ff7171><b>⁉</b> A string argument was expected, but you entered <br><input>";
        localization.command.exception.permission = "<color:#ff7171><b>⁉</b> You don't have permission to use this command";

        localization.command.flectonepulse.nullHostEditor = "<color:#ff7171><b>⁉</b> The host parameter cannot be empty and must be configured in <u>config.yml";
        localization.command.flectonepulse.formatFalse = "<color:#ff7171>★ An has error occurred while reloading <br>Error: <message>";
        localization.command.flectonepulse.formatTrue = "<fcolor:2>★ <u>FlectonePulse</u> successfully reloaded! (<i><time></i>)";
        localization.command.flectonepulse.formatWebStarting = "<fcolor:2>★ Web server starting, please wait...";
        localization.command.flectonepulse.formatEditor = "<fcolor:2>★ Link for web editing <u><fcolor:2><click:open_url:\"<url>\"><hover:show_text:\"<fcolor:2><url>\"><url>";

        localization.command.geolocate.nullPlayer = "<color:#ff7171><b>⁉</b> This player does not exist";
        localization.command.geolocate.nullOrError = "<color:#ff7171><b>⁉</b> Problem receiving information, try again";
        localization.command.geolocate.format = "<fcolor:1>Geolocation for <display_name><br>Country: <fcolor:2><country><br><fcolor:1>Region: <fcolor:2><region_name><br><fcolor:1>City: <fcolor:2><city><br><fcolor:1>Timezone: <fcolor:2><timezone><br><fcolor:1>Mobile connection: <fcolor:2><mobile><br><fcolor:1>VPN: <fcolor:2><proxy><br><fcolor:1>Hosting: <fcolor:2><hosting><br><fcolor:1>IP: <fcolor:2><query>";

        localization.command.helper.nullHelper = "<color:#ff7171><b>⁉</b> There are no people who can help you";
        localization.command.helper.global = "<fcolor:2>👤 <display_name> needs help ⏩ <fcolor:1><message>";
        localization.command.helper.player = "<fcolor:2>👤 Request sent, awaiting reply";

        localization.command.ignore.myself = "<color:#ff7171><b>⁉</b> You can't ignore yourself";
        localization.command.ignore.nullPlayer = "<color:#ff7171><b>⁉</b> This player does not exist";
        localization.command.ignore.you = "<color:#ff7171><b>⁉</b> You can't write to him because you ignore him";
        localization.command.ignore.he = "<color:#ff7171><b>⁉</b> You can't write to him because he ignore you";
        localization.command.ignore.formatTrue = "<color:#ff7171>☹ You ignore <display_name>";
        localization.command.ignore.formatFalse = "<color:#98FB98>☺ You unignore <display_name>";

        localization.command.ignorelist.empty = "<color:#98FB98>☺ You don't ignore anyone";
        localization.command.ignorelist.nullPage = "<color:#ff7171><b>⁉</b> This page doesn't exist";
        localization.command.ignorelist.header = "<fcolor:2>▋ Ignores: <count><br>";
        localization.command.ignorelist.line = "<hover:show_text:\"<fcolor:1>Click to unignore <display_name>\"><click:run_command:\"<command>\"><color:#ff7171>☒ <display_name></click></hover> <fcolor:1>Date: <date>";
        localization.command.ignorelist.footer = "<br>▋ <fcolor:2><click:run_command:\"<command> <prev_page>\">←</click> <fcolor:1>Page: <current_page>/<last_page> <fcolor:2><click:run_command:\"<command> <next_page>\">→";

        localization.command.kick.nullPlayer = "<color:#ff7171><b>⁉</b> This player does not exist";
        localization.command.kick.reasons = new Localization.ReasonMap() {{
            put("default", "Kicked by an operator");
        }};
        localization.command.kick.server = "<color:#ff7171>🔒 <fcolor:2><moderator> <fcolor:1>kicked <fcolor:2><player> <fcolor:1><hover:show_text:\"<fcolor:1>ID: <id><br>Date: <date><br>Moderator: <moderator><br>Reason: <reason>\">[INFO]</hover>";
        localization.command.kick.person = "<color:#ff7171>🔒 KICK 🔒 <fcolor:1><br><br>ID: <id><br><br>Date: <date><br><br>Moderator: <moderator><br><br>Reason: <reason>";

        localization.command.maintenance.kick = "<color:#ff7171>★ The server is under maintenance";
        localization.command.maintenance.serverDescription = "<color:#ff7171>The server is under maintenance";
        localization.command.maintenance.serverVersion = "Maintenance";
        localization.command.maintenance.formatTrue = "<fcolor:1>★ You have <fcolor:2>enabled <fcolor:1>maintenance on the server";
        localization.command.maintenance.formatFalse = "<fcolor:1>★ You have <fcolor:2>disabled <fcolor:1>maintenance on the server";

        localization.command.mail.nullPlayer = "<color:#ff7171><b>⁉</b> This player does not exist";
        localization.command.mail.onlinePlayer = "<color:#ff7171><b>⁉</b> This player is online";
        localization.command.mail.sender = "<fcolor:2>✉ Mail #<id> for <target> » <fcolor:1><message>";
        localization.command.mail.receiver = "<fcolor:2>✉ Mail from <display_name> » <fcolor:1><message>";

        localization.command.me.format = "<fcolor:1>✎ <display_name> <fcolor:1><message>";

        localization.command.mute.nullPlayer = "<color:#ff7171><b>⁉</b> This player does not exist";
        localization.command.mute.nullTime = "<color:#ff7171><b>⁉</b> Incorrect time";
        localization.command.mute.reasons = new Localization.ReasonMap() {{
            put("default", "You have been muted on this server");
        }};
        localization.command.mute.server = "<color:#ff7171>🔒 <fcolor:2><moderator> <fcolor:1>has muted <fcolor:2><player> <fcolor:1><hover:show_text:\"<fcolor:1>ID: <id><br>Date: <date><br>Time: <time><br>Remaining time: <time_left><br>Moderator: <moderator><br>Reason: <reason>\">[INFO]</hover>";
        localization.command.mute.person = "<color:#ff7171>🔒 You are muted, <time_left> left";

        localization.command.mutelist.empty = "<color:#98FB98>☺ No mutes found";
        localization.command.mutelist.nullPage = "<color:#ff7171><b>⁉</b> This page doesn't exist";
        localization.command.mutelist.nullPlayer = "<color:#ff7171><b>⁉</b> This player does not exist";
        localization.command.mutelist.global.header = "<fcolor:2>▋ Mutes: <count> <br>";
        localization.command.mutelist.global.line = "<hover:show_text:\"<fcolor:1>Click to unmute <display_name>\"><click:run_localization.command:\"<localization.command>\"><color:#ff7171>☒ <display_name></click></hover> <fcolor:1><hover:show_text:\"<fcolor:1>ID: <id><br>Date: <date><br>Time: <time><br>Moderator: <moderator><br>Reason: <reason>\">[MORE]</hover>";
        localization.command.mutelist.global.footer = "<br><fcolor:2>▋ <fcolor:2><click:run_localization.command:\"<localization.command> <prev_page>\">←</click> <fcolor:1>Page: <current_page>/<last_page> <fcolor:2><click:run_localization.command:\"<localization.command> <next_page>\">→";
        localization.command.mutelist.player.header = "<fcolor:2>▋ All mutes: <count> <br>";
        localization.command.mutelist.player.line = "<hover:show_text:\"<fcolor:1>Click to unmute <display_name>\"><click:run_command:\"<command>\"><color:#ff7171>☒ <display_name></click></hover> <fcolor:1><hover:show_text:\"<fcolor:1>ID: <id><br>Date: <date><br>Time: <time><br>Moderator: <moderator><br>Reason: <reason>\">[MORE]</hover>";
        localization.command.mutelist.player.footer = "<br><fcolor:2>▋ <click:run_command:\"<command> <prev_page>\">←</click> <fcolor:1>Page: <current_page>/<last_page> <fcolor:2><click:run_command:\"<command> <next_page>\">→";

        localization.command.online.nullPlayer = "<color:#ff7171><b>⁉</b> This player does not exist";
        localization.command.online.formatCurrent = "<fcolor:1>⌛ <display_name> currently on server";
        localization.command.online.formatFirst = "<fcolor:1>⌛ <display_name> was first on server <time> ago";
        localization.command.online.formatLast = "<fcolor:1>⌛ <display_name> <fcolor:1>was last on server <time> ago";
        localization.command.online.formatTotal = "<fcolor:1>⌛ <display_name> <fcolor:1>has spent a total of <time> on server";

        localization.command.ping.nullPlayer = "<color:#ff7171><b>⁉</b> This player does not exist";
        localization.command.ping.format = "<fcolor:1>🖧 <display_name>'s ping is <ping>";

        localization.command.poll.nullPoll = "<color:#ff7171><b>⁉</b> Poll not found";
        localization.command.poll.expired = "<color:#ff7171><b>⁉</b> The poll has ended";
        localization.command.poll.already = "<color:#ff7171><b>⁉</b> You have already voted in this poll";
        localization.command.poll.voteTrue = "<color:#4eff52>👍 You voted for option <answer_id> in poll #<id>. There are <count> of you";
        localization.command.poll.voteFalse = "<color:#ff4e4e>🖓 You rejected option <answer_id> in poll #<id>. There are <count> without you";
        localization.command.poll.format = "<br><color:#fce303>│ <status> <br>│ <message><reset> <color:#fce303><br>├─────────────<br><answers>";
        localization.command.poll.answerTemplate = "<color:#fce303>│ <count> → <color:#4eff52><hover:show_text:\"<color:#4eff52>Vote for <bold><answer>\"><click:run_command:\"/pollvote <id> <number>\"><answer> [👍]<br>";
        localization.command.poll.status.start = "New poll #<b><id></b> has been created";
        localization.command.poll.status.run = "Poll #<b><id></b> is in progress";
        localization.command.poll.status.end = "Poll #<b><id></b> has ended";
        localization.command.poll.modern.header = "Poll";
        localization.command.poll.modern.inputName = "Name";
        localization.command.poll.modern.inputInitial = "";
        localization.command.poll.modern.multipleName = "Allow multiple answers";
        localization.command.poll.modern.endTimeName = "Duration (min)";
        localization.command.poll.modern.repeatTimeName = "Interval (min)";
        localization.command.poll.modern.newAnswerButtonName = "Add answer";
        localization.command.poll.modern.removeAnswerButtonName = "Remove answer";
        localization.command.poll.modern.inputAnswerName = "Answer <number>";
        localization.command.poll.modern.inputAnswersInitial = "";
        localization.command.poll.modern.createButtonName = "Create poll";

        localization.command.prompt.message = "message";
        localization.command.prompt.hard = "hard";
        localization.command.prompt.accept = "accept";
        localization.command.prompt.turn = "turn on";
        localization.command.prompt.type = "type";
        localization.command.prompt.category = "category";
        localization.command.prompt.reason = "reason";
        localization.command.prompt.id = "id";
        localization.command.prompt.time = "time";
        localization.command.prompt.repeatTime = "repeat time";
        localization.command.prompt.multipleVote = "multiple vote";
        localization.command.prompt.player = "player";
        localization.command.prompt.number = "number";
        localization.command.prompt.color = "color";
        localization.command.prompt.language = "language";
        localization.command.prompt.url = "url";
        localization.command.prompt.move = "move";
        localization.command.prompt.value = "value";

        localization.command.reply.nullReceiver = "<color:#ff7171><b>⁉</b> No one to answer";

        localization.command.rockpaperscissors.nullPlayer = "<color:#ff7171><b>⁉</b> This player does not exist";
        localization.command.rockpaperscissors.nullGame = "<color:#ff7171><b>⁉</b> This game does not exist";
        localization.command.rockpaperscissors.wrongMove = "<color:#ff7171><b>⁉</b> This move is not possible";
        localization.command.rockpaperscissors.already = "<color:#ff7171><b>⁉</b> You've already made your move";
        localization.command.rockpaperscissors.myself = "<color:#ff7171><b>⁉</b> You can't play with yourself";
        localization.command.rockpaperscissors.sender = "<fcolor:1>Now goes <target>";
        localization.command.rockpaperscissors.receiver = "<fcolor:2>✂ <display_name> <fcolor:1>suggested a game of rock-paper-scissors";
        localization.command.rockpaperscissors.formatMove = "<fcolor:2>✂ <fcolor:1>Choose your move <fcolor:2><click:run_command:\"/rps <target> rock <uuid>\">[🪨 rock]</click> <click:run_command:\"/rps <target> scissors <uuid>\">[✂ scissors]</click> <click:run_command:\"/rps <target> paper <uuid>\">[🧻 paper]</click>";
        localization.command.rockpaperscissors.formatWin = "<color:#98FB98>✂ Winning <display_name>! <b><sender_move></b> on <b><receiver_move></b>";
        localization.command.rockpaperscissors.formatDraw = "<color:#98FB98>✂ It's a draw! You both chose <b><move>";
        localization.command.rockpaperscissors.strategies = new LinkedHashMap<>() {{
            put("paper", "paper");
            put("rock", "rock");
            put("scissors", "scissors");
        }};

        localization.command.spy.formatTrue = "<fcolor:1>[👁] You <color:#98FB98>turned on <fcolor:1>spy mode";
        localization.command.spy.formatFalse = "<fcolor:1>[👁] You <color:#F08080>turned off <fcolor:1>spy mode";
        localization.command.spy.formatLog = "<fcolor:1>[👁] <display_name> <color:#98FB98><action> <fcolor:1>→ <fcolor:2><message>";

        localization.command.stream.already = "<color:#ff7171><b>⁉</b> You are already streaming";
        localization.command.stream.not = "<color:#ff7171><b>⁉</b> You don't stream";
        localization.command.stream.prefixTrue = "<color:#ff4e4e>⏻</color:#ff4e4e> ";
        localization.command.stream.prefixFalse = "";
        localization.command.stream.urlTemplate = "<color:#ff4e4e>│ <fcolor:2><click:open_url:\"<url>\"><hover:show_text:\"<fcolor:2><url>\"><url></hover></click>";
        localization.command.stream.formatStart = "<br><color:#ff4e4e>│ 🔔 <fcolor:1>Announcement <br><color:#ff4e4e>│<br><color:#ff4e4e>│ <fcolor:1><display_name> started streaming<br><color:#ff4e4e>│<br><urls><br>";
        localization.command.stream.formatEnd = "<fcolor:2>★ Thanks for streaming on our server!";

        localization.command.symbol.format = "<click:suggest_command:\"<message>\"><fcolor:2>🖥 Click for using: <fcolor:1><message>";

        localization.command.tell.nullPlayer = "<color:#ff7171><b>⁉</b> This player does not exist";
        localization.command.tell.sender = "<fcolor:2>✉ <display_name> → <target> » <fcolor:1><message>";
        localization.command.tell.receiver = "<fcolor:2>✉ <display_name> → <target> » <fcolor:1><message>";
        localization.command.tell.myself = "<fcolor:2>✉ [Note] <fcolor:1><message>";

        localization.command.tictactoe.nullPlayer = "<color:#ff7171><b>⁉</b> This player does not exist";
        localization.command.tictactoe.myself = "<color:#ff7171><b>⁉</b> You can't play with yourself";
        localization.command.tictactoe.wrongGame = "<color:#ff7171><b>⁉</b> This game does not exist";
        localization.command.tictactoe.wrongMove = "<color:#ff7171><b>⁉</b> This step is not possible";
        localization.command.tictactoe.wrongByPlayer = "<color:#ff7171><b>⁉</b> This game ended because player quit";
        localization.command.tictactoe.symbol.blank = "<fcolor:1><hover:show_text:\"<fcolor:1>Move <move>\"><click:run_command:\"/tictactoemove %d <move>\">☐</click></hover>";
        localization.command.tictactoe.symbol.first = "<fcolor:2>☑";
        localization.command.tictactoe.symbol.firstRemove = "<color:#ff7171>☑</color:#ff7171>";
        localization.command.tictactoe.symbol.firstWin = "<color:#98FB98>☑</color:#98FB98>";
        localization.command.tictactoe.symbol.second = "<fcolor:2>☒";
        localization.command.tictactoe.symbol.secondRemove = "<color:#ff7171>☒</color:#ff7171>";
        localization.command.tictactoe.symbol.secondWin = "<color:#98FB98>☒</color:#98FB98>";
        localization.command.tictactoe.field = "<fcolor:1><br>|[#][#][#]<fcolor:1>| <title> <current_move> <br><fcolor:1>|[#][#][#]<fcolor:1>| <br>|[#][#][#]<fcolor:1>| <last_move><br>";
        localization.command.tictactoe.currentMove = "<fcolor:2>☐ → <symbol>";
        localization.command.tictactoe.lastMove = "<fcolor:2>Last move (<move>)";
        localization.command.tictactoe.formatMove = "<fcolor:2><target>'s move";
        localization.command.tictactoe.formatWin = "<color:#98FB98><target> won this game</color:#98FB98>";
        localization.command.tictactoe.formatDraw = "<color:#98FB98>The game ended in a draw 👬</color:#98FB98>";
        localization.command.tictactoe.sender = "<fcolor:1>☐ An offer to play was sent to <target>";
        localization.command.tictactoe.receiver = "<click:run_command:\"/tictactoemove %d create\"><fcolor:1>☐ Received an invite to play tic-tac-toe with <display_name>, accept? [+]";

        localization.command.toponline.nullPage = "<color:#ff7171><b>⁉</b> This page doesn't exist";
        localization.command.toponline.header = "<fcolor:2>▋ Players: <count> <br>";
        localization.command.toponline.line = "<fcolor:2><time_player> <fcolor:1>played for <fcolor:2><time>";
        localization.command.toponline.footer = "<br><fcolor:2>▋ <click:run_command:\"<command> <prev_page>\">←</click> <fcolor:1>Page: <current_page>/<last_page> <fcolor:2><click:run_command:\"<command> <next_page>\">→";

        localization.command.translateto.nullOrError = "<color:#ff7171><b>⁉</b> Error, you may have specified an unsupported language";
        localization.command.translateto.format = "<fcolor:1>📖 Translation to [<language>] → <fcolor:2><message>";

        localization.command.Try.formatTrue = "<color:#98FB98>☺ <display_name> <message><reset> <color:#98FB98><percent>%";
        localization.command.Try.formatFalse = "<color:#F08080>☹ <display_name> <message><reset> <color:#F08080><percent>%";

        localization.command.unban.nullPlayer = "<color:#ff7171><b>⁉</b> This player does not exist";
        localization.command.unban.notBanned = "<color:#ff7171><b>⁉</b> This player is not banned";
        localization.command.unban.format = "<color:#98FB98>\uD83D\uDD13 <fcolor:2><moderator> <color:#98FB98>unbanned the player <fcolor:2><player>";

        localization.command.unmute.nullPlayer = "<color:#ff7171><b>⁉</b> This player does not exist";
        localization.command.unmute.notMuted = "<color:#ff7171><b>⁉</b> This player is not muted";
        localization.command.unmute.format = "<color:#98FB98>\uD83D\uDD13 <fcolor:2><moderator> <color:#98FB98>unmutted the player <fcolor:2><player>";

        localization.command.unwarn.nullPlayer = "<color:#ff7171><b>⁉</b> This player does not exist";
        localization.command.unwarn.notWarned = "<color:#ff7171><b>⁉</b> This player is not warned";
        localization.command.unwarn.format = "<color:#98FB98>\uD83D\uDD13 <fcolor:2><moderator> <color:#98FB98>unwarned the player <fcolor:2><player>";

        localization.command.warn.nullPlayer = "<color:#ff7171><b>⁉</b> This player does not exist";
        localization.command.warn.nullTime = "<color:#ff7171><b>⁉</b> Incorrect time";
        localization.command.warn.reasons = new Localization.ReasonMap() {{
            put("default", "You have been warned on this server");
        }};
        localization.command.warn.server = "<color:#ff7171>🔒 <fcolor:2><moderator> <fcolor:1>gave a warning to <fcolor:2><player> <fcolor:1><hover:show_text:\"<fcolor:1>ID: <id><br>Date: <date><br>Time: <time><br>Remaining time: <time_left><br>Moderator: <moderator><br>Reason: <reason>\">[INFO]</hover>";
        localization.command.warn.person = "<color:#ff7171>🔒 You are warned a <time>";

        localization.command.warnlist.empty = "<color:#98FB98>☺ No warns found";
        localization.command.warnlist.nullPage = "<color:#ff7171><b>⁉</b> This page doesn't exist";
        localization.command.warnlist.nullPlayer = "<color:#ff7171><b>⁉</b> This player does not exist";
        localization.command.warnlist.global.header = "<fcolor:2>▋ Warns: <count> <br>";
        localization.command.warnlist.global.line = "<hover:show_text:\"<fcolor:1>Click to unwarn <display_name>\"><click:run_command:\"<command>\"><color:#ff7171>☒ <display_name></click></hover> <fcolor:1><hover:show_text:\"<fcolor:1>ID: <id><br>Date: <date><br>Time: <time><br>Moderator: <moderator><br>Reason: <reason>\">[INFO]</hover>";
        localization.command.warnlist.global.footer = "<br><fcolor:2>▋ <click:run_command:\"<command> <prev_page>\">←</click> <fcolor:1>Page: <current_page>/<last_page> <fcolor:2><click:run_command:\"<command> <next_page>\">→";
        localization.command.warnlist.player.header = "<fcolor:2>▋ All warns: <count> <br>";
        localization.command.warnlist.player.line = "<hover:show_text:\"<fcolor:1>Click to unwarn <display_name>\"><click:run_command:\"<command>\"><color:#ff7171>☒ <display_name></click></hover> <fcolor:1><hover:show_text:\"<fcolor:1>ID: <id><br>Date: <date><br>Time: <time><br>Moderator: <moderator><br>Reason: <reason>\">[INFO]</hover>";
        localization.command.warnlist.player.footer = "<br><fcolor:2>▋ <click:run_command:\"<command> <prev_page>\">←</click> <fcolor:1>Page: <current_page>/<last_page> <fcolor:2><click:run_command:\"<command> <next_page>\">→";

        localization.integration.discord.forMinecraft = "<fcolor:2><name> <fcolor:1>» <fcolor:4><message>";
        localization.integration.discord.infoChannel = new LinkedHashMap<>() {{
            put("id", "TPS <tps>");
        }};
        localization.integration.discord.messageChannel = new LinkedHashMap<>() {{
            put("CHAT_GLOBAL", new Localization.Integration.Discord.ChannelEmbed());
        }};
        localization.integration.discord.messageChannel.get("CHAT_GLOBAL").content = "<final_message>";
        localization.integration.discord.messageChannel.get("CHAT_GLOBAL").webhook.enable = false;
        localization.integration.discord.messageChannel.get("CHAT_GLOBAL").webhook.avatar = "https://mc-heads.net/avatar/<skin>/32.png";
        localization.integration.discord.messageChannel.get("CHAT_GLOBAL").webhook.content = "";
        localization.integration.discord.messageChannel.get("CHAT_GLOBAL").embed.enable = false;
        localization.integration.discord.messageChannel.get("CHAT_GLOBAL").embed.color = "";
        localization.integration.discord.messageChannel.get("CHAT_GLOBAL").embed.title = "";
        localization.integration.discord.messageChannel.get("CHAT_GLOBAL").embed.url = "";
        localization.integration.discord.messageChannel.get("CHAT_GLOBAL").embed.author.name = "";
        localization.integration.discord.messageChannel.get("CHAT_GLOBAL").embed.author.url = "";
        localization.integration.discord.messageChannel.get("CHAT_GLOBAL").embed.author.iconUrl = "https://mc-heads.net/avatar/<skin>/16.png";
        localization.integration.discord.messageChannel.get("CHAT_GLOBAL").embed.description = "";
        localization.integration.discord.messageChannel.get("CHAT_GLOBAL").embed.thumbnail = "";
        localization.integration.discord.messageChannel.get("CHAT_GLOBAL").embed.fields = new ArrayList<>(List.of(new Localization.Integration.Discord.Embed.Field("", "", false)));
        localization.integration.discord.messageChannel.get("CHAT_GLOBAL").embed.image = "";
        localization.integration.discord.messageChannel.get("CHAT_GLOBAL").embed.timestamp = true;
        localization.integration.discord.messageChannel.get("CHAT_GLOBAL").embed.footer.text = "";
        localization.integration.discord.messageChannel.get("CHAT_GLOBAL").embed.footer.iconUrl = "https://mc-heads.net/avatar/<skin>/16.png";

        localization.integration.telegram.forMinecraft = "<fcolor:2><name> <fcolor:1>» <fcolor:4><message>";
        localization.integration.telegram.messageChannel = new LinkedHashMap<>() {{
            put("CHAT_GLOBAL", "<final_message>");
        }};

        localization.integration.twitch.forMinecraft = "<fcolor:2><name> <fcolor:1>» <fcolor:4><message>";
        localization.integration.twitch.messageChannel = new LinkedHashMap<>() {{
            put("CHAT_GLOBAL", "<final_message>");
        }};

        localization.message.advancement.formatTask = "<fcolor:1>🌠 <display_name> has made the advancement <advancement>";
        localization.message.advancement.formatGoal = "<fcolor:1>🌠 <display_name> has reached the goal <advancement>";
        localization.message.advancement.formatChallenge = "<fcolor:1>🌠 <display_name> has completed the challenge <advancement>";
        localization.message.advancement.formatTaken = "<fcolor:1>🌠 <display_name> has lost the achievement <advancement>";
        localization.message.advancement.tag.task = "<color:#4eff52>[<hover:show_text:\"<color:#4eff52><advancement>\"><advancement></hover>]";
        localization.message.advancement.tag.challenge = "<color:#834eff>[<hover:show_text:\"<color:#834eff><advancement>\"><advancement></hover>]";
        localization.message.advancement.revoke.manyToOne = "<fcolor:1>🌠 Revoked <fcolor:2><advancements> <fcolor:1>advancements from <target>";
        localization.message.advancement.revoke.oneToMany = "<fcolor:1>🌠 Revoked the advancement <advancement> from <fcolor:2><players> <fcolor:1>players";
        localization.message.advancement.revoke.manyToMany = "<fcolor:1>🌠 Revoked <fcolor:2><advancements> <fcolor:1>advancements from <fcolor:2><players> <fcolor:1>players";
        localization.message.advancement.revoke.oneToOne = "<fcolor:1>🌠 Revoked the advancement <advancement> <fcolor:1>from <target>";
        localization.message.advancement.revoke.criterionToMany = "<fcolor:1>🌠 Revoked criterion '<fcolor:2><criterion><fcolor:1>' of advancement <advancement> from <fcolor:2><players> <fcolor:1>players";
        localization.message.advancement.revoke.criterionToOne = "<fcolor:1>🌠 Revoked criterion '<fcolor:2><criterion><fcolor:1>' of advancement <advancement> from <target>";
        localization.message.advancement.grant.manyToOne = "<fcolor:1>🌠 Granted <fcolor:2><advancements> <fcolor:1>advancements to <target>";
        localization.message.advancement.grant.oneToMany = "<fcolor:1>🌠 Granted the advancement <advancement> to <fcolor:2><players> <fcolor:1>players";
        localization.message.advancement.grant.manyToMany = "<fcolor:1>🌠 Granted <fcolor:2><advancements> <fcolor:1>advancements to <fcolor:2><players> <fcolor:1>players";
        localization.message.advancement.grant.oneToOne = "<fcolor:1>🌠 Granted the advancement <advancement> to <target>";
        localization.message.advancement.grant.criterionToMany = "<fcolor:1>🌠 Granted criterion '<fcolor:2><criterion><fcolor:1>' of advancement <advancement> to <fcolor:2><players> <fcolor:1>players";
        localization.message.advancement.grant.criterionToOne = "<fcolor:1>🌠 Granted criterion '<fcolor:2><criterion><fcolor:1>' of advancement <advancement> to <target>";

        localization.message.afk.suffix = " <color:#FFFF00>⌚</color>";
        localization.message.afk.formatTrue.global = "<gradient:#ffd500:#FFFF00>⌚ <player> is now afk";
        localization.message.afk.formatTrue.local = "<gradient:#ffd500:#FFFF00>⌚ Now you're afk";
        localization.message.afk.formatFalse.global = "<gradient:#ffd500:#FFFF00>⌚ <player> isn't afk now";
        localization.message.afk.formatFalse.local = "<gradient:#ffd500:#FFFF00>⌚ Now you're not afk";

        localization.message.attribute.baseValue.get = "<fcolor:1>❤ Base value of attribute <fcolor:2><lang:'<attribute>'> <fcolor:1>for entity <target> is <fcolor:2><value>";
        localization.message.attribute.baseValue.reset = "<fcolor:1>❤ Base value for attribute <fcolor:2><lang:'<attribute>'> <fcolor:1>for entity <target> reset to default <fcolor:2><value>";
        localization.message.attribute.baseValue.set = "<fcolor:1>❤ Base value for attribute <fcolor:2><lang:'<attribute>'> <fcolor:1>for entity <target> set to <fcolor:2><value>";
        localization.message.attribute.modifier.add = "<fcolor:1>❤ Added modifier <fcolor:2><modifier> <fcolor:1>to attribute <fcolor:2><lang:'<attribute>'> <fcolor:1>for entity <target>";
        localization.message.attribute.modifier.remove = "<fcolor:1>❤ Removed modifier <fcolor:2><modifier> <fcolor:1>from attribute <fcolor:2><lang:'<attribute>'> <fcolor:1>for entity <target>";
        localization.message.attribute.modifier.valueGet = "<fcolor:1>❤ Value of modifier <fcolor:2><modifier> <fcolor:1>on attribute <fcolor:2><lang:'<attribute>'> <fcolor:1>for entity <target> is <fcolor:2><value>";
        localization.message.attribute.valueGet = "<fcolor:1>❤ Value of attribute <fcolor:2><lang:'<attribute>'> <fcolor:1>for entity <target> is <fcolor:2><value>";

        localization.message.auto.types = new LinkedHashMap<>() {{
            put("announcement", new LinkedList<>() {{
                add("<br><fcolor:1>◇ This server uses <click:open_url:\"https://flectone.net/pulse/\"><hover:show_text:\"<fcolor:2>https://flectone.net/pulse/\"><fcolor:2>FlectonePulse</hover></click> :)<br>");
                add("<br><fcolor:1>      ❝ Join our discord ❠ <br><fcolor:2>   <u><click:open_url:\"https://discord.flectone.net\"><hover:show_text:\"<fcolor:2>https://discord.flectone.net\">https://discord.flectone.net</hover></click></u><br>");
                add("<br><fcolor:1>⚡ Support <fcolor:2>FlectonePulse <fcolor:1>on Boosty <br><fcolor:1>⚡ <u><click:open_url:\"https://boosty.to/thefaser/\"><hover:show_text:\"<fcolor:2>https://boosty.to/thefaser/\">https://boosty.to/thefaser/</hover></click></u><br>");
                add("<br><fcolor:1>      ✉ Join our telegram ✉ <br><fcolor:2>    <u><click:open_url:\"https://t.me/flectone\"><hover:show_text:\"<fcolor:2>https://t.me/flectone\">https://t.me/flectone</hover></click></u><br>");
            }});
        }};

        localization.message.bed.noSleep = "<fcolor:1>\uD83D\uDECC You can sleep only at night or during thunderstorms";
        localization.message.bed.notSafe = "<fcolor:1>\uD83D\uDECC You may not rest now; there are monsters nearby";
        localization.message.bed.obstructed = "<fcolor:1>\uD83D\uDECC This bed is obstructed";
        localization.message.bed.occupied = "<fcolor:1>\uD83D\uDECC This bed is occupied";
        localization.message.bed.tooFarAway = "<fcolor:1>\uD83D\uDECC You may not rest now; the bed is too far away";

        localization.message.brand.values = new LinkedList<>() {{
            add("<white>Minecraft");
            add("<aqua>Minecraft");
        }};

        localization.message.bubble.format = "<fcolor:3><message>";

        localization.message.chat.nullChat = "<color:#ff7171><b>⁉</b> Chat is disabled on this server";
        localization.message.chat.nullReceiver = "<color:#ff7171><b>⁉</b> Nobody heard you";
        localization.message.chat.types = new LinkedHashMap<>() {{
            put("global", "<delete><display_name> <world_prefix>»<fcolor:4> <message><reset><translate>");
            put("local", "<delete><display_name><fcolor:3>: <message><reset><translate>");
        }};

        localization.message.clear.single = "<fcolor:1>\uD83C\uDF0A Removed <fcolor:2><items> <fcolor:1>item(s) from player <target>";
        localization.message.clear.multiple = "<fcolor:1>\uD83C\uDF0A Removed <fcolor:2><items> <fcolor:1>item(s) from <fcolor:2><players> <fcolor:1>players";

        localization.message.clone.format = "<fcolor:1>⏹ Successfully cloned <fcolor:2><blocks> <fcolor:1>block(s)";

        localization.message.commandblock.notEnabled = "<fcolor:1>\uD83E\uDD16 Command blocks are not enabled on this server";
        localization.message.commandblock.format = "<fcolor:1>\uD83E\uDD16 Command set: <fcolor:2><command>";

        localization.message.damage.format = "<fcolor:1>\uD83D\uDDE1 Applied <fcolor:2><amount> <fcolor:1>damage to <target>";

        localization.message.death.types = new LinkedHashMap<>() {{
            put("death.attack.anvil", "<fcolor:1>☠ <target> was squashed by a falling anvil");
            put("death.attack.anvil.player", "<fcolor:1>☠ <target> was squashed by a falling anvil while fighting <killer>");
            put("death.attack.arrow", "<fcolor:1>☠ <fcolor:1><target> was shot by <killer>");
            put("death.attack.arrow.item", "<fcolor:1>☠ <target> was shot by <killer> using <fcolor:2>[<killer_item>]<fcolor:1>");
            put("death.attack.badRespawnPoint.message", "<fcolor:1>☠ <target> was killed by <fcolor:2>[<click:open_url:\"https://www.youtube.com/watch?v=dQw4w9WgXcQ\"><hover:show_text:\"<fcolor:2>MCPE-28723\">Intentional Game Design</hover></click>]");
            put("death.attack.cactus", "<fcolor:1>☠ <target> was pricked to death");
            put("death.attack.cactus.player", "<fcolor:1>☠ <target> walked into a cactus while trying to escape <killer>");
            put("death.attack.cramming", "<fcolor:1>☠ <target> was squished too much");
            put("death.attack.cramming.player", "<fcolor:1>☠ <target> was squashed by <killer>");
            put("death.attack.dragonBreath", "<fcolor:1>☠ <target> was roasted in dragon's breath");
            put("death.attack.dragonBreath.player", "<fcolor:1>☠ <target> was roasted in dragon's breath by <killer>");
            put("death.attack.drown", "<fcolor:1>☠ <target> drowned");
            put("death.attack.drown.player", "<fcolor:1>☠ <target> drowned while trying to escape <killer>");
            put("death.attack.dryout", "<fcolor:1>☠ <target> died from dehydration");
            put("death.attack.dryout.player", "<fcolor:1>☠ <target> died from dehydration while trying to escape <killer>");
            put("death.attack.even_more_magic", "<fcolor:1>☠ <target> was killed by even more magic");
            put("death.attack.explosion", "<fcolor:1>☠ <target> blew up");
            put("death.attack.explosion.player", "<fcolor:1>☠ <target> was blown up by <killer>");
            put("death.attack.explosion.item", "<fcolor:1>☠ <target> was blown up by <killer> using <fcolor:2>[<killer_item>]<fcolor:1>");
            put("death.attack.explosion.player.item", "<fcolor:1>☠ <target> was blown up by <killer> using <fcolor:2>[<killer_item>]<fcolor:1>");
            put("death.attack.fall", "<fcolor:1>☠ <target> hit the ground too hard");
            put("death.attack.fall.player", "<fcolor:1>☠ <target> hit the ground too hard while trying to escape <killer>");
            put("death.attack.fallingBlock", "<fcolor:1>☠ <target> was squashed by a falling block");
            put("death.attack.fallingBlock.player", "<fcolor:1>☠ <target> was squashed by a falling block while fighting <killer>");
            put("death.attack.fallingStalactite", "<fcolor:1>☠ <target> was skewered by a falling stalactite");
            put("death.attack.fallingStalactite.player", "<fcolor:1>☠ <target> was skewered by a falling stalactite while fighting <killer>");
            put("death.attack.fireball", "<fcolor:1>☠ <target> was fireballed by <killer>");
            put("death.attack.fireball.item", "<fcolor:1>☠ <target> was fireballed by <killer> using <fcolor:2>[<killer_item>]<fcolor:1>");
            put("death.attack.fireworks", "<fcolor:1>☠ <target> went off with a bang");
            put("death.attack.fireworks.item", "<fcolor:1>☠ <target> went off with a bang due to a firework fired from <fcolor:2>[<killer_item>]<fcolor:1> by <killer>");
            put("death.attack.fireworks.player", "<fcolor:1>☠ <target> went off with a bang while fighting <killer>");
            put("death.attack.flyIntoWall", "<fcolor:1>☠ <target> experienced kinetic energy");
            put("death.attack.flyIntoWall.player", "<fcolor:1>☠ <target> experienced kinetic energy while trying to escape <killer>");
            put("death.attack.freeze", "<fcolor:1>☠ <target> froze to death");
            put("death.attack.freeze.player", "<fcolor:1>☠ <target> was frozen to death by <killer>");
            put("death.attack.generic", "<fcolor:1>☠ <target> died");
            put("death.attack.generic.player", "<fcolor:1>☠ <target> died because of <killer>");
            put("death.attack.genericKill", "<fcolor:1>☠ <target> was killed");
            put("death.attack.genericKill.player", "<fcolor:1>☠ <target> was killed while fighting <killer>");
            put("death.attack.hotFloor", "<fcolor:1>☠ <target> discovered the floor was lava");
            put("death.attack.hotFloor.player", "<fcolor:1>☠ <target> walked into the danger zone due to <killer>");
            put("death.attack.indirectMagic", "<fcolor:1>☠ <target> was killed by <killer> using magic");
            put("death.attack.indirectMagic.item", "<fcolor:1>☠ <target> was killed by <killer> using <fcolor:2>[<killer_item>]<fcolor:1>");
            put("death.attack.inFire", "<fcolor:1>☠ <target> went up in flames");
            put("death.attack.inFire.player", "<fcolor:1>☠ <target> walked into fire while fighting <killer>");
            put("death.attack.inWall", "<fcolor:1>☠ <target> suffocated in a wall");
            put("death.attack.inWall.player", "<fcolor:1>☠ <target> suffocated in a wall while fighting <killer>");
            put("death.attack.lava", "<fcolor:1>☠ <target> tried to swim in lava");
            put("death.attack.lava.player", "<fcolor:1>☠ <target> tried to swim in lava to escape <killer>");
            put("death.attack.lightningBolt", "<fcolor:1>☠ <target> was struck by lightning");
            put("death.attack.lightningBolt.player", "<fcolor:1☠ <target> was struck by lightning while fighting <killer>");
            put("death.attack.mace_smash", "<fcolor:1>☠ <target> was smashed by <killer>");
            put("death.attack.mace_smash.item", "<fcolor:1>☠ <target> was smashed by <killer> with <fcolor:2>[<killer_item>]<fcolor:1>");
            put("death.attack.magic", "<fcolor:1>☠ <target> was killed by magic");
            put("death.attack.magic.player", "<fcolor:1>☠ <target> was killed by magic while trying to escape <killer>");
            put("death.attack.mob", "<fcolor:1>☠ <target> was slain by <killer>");
            put("death.attack.mob.item", "<fcolor:1>☠ <target> was slain by <killer> using <fcolor:2>[<killer_item>]<fcolor:1>");
            put("death.attack.onFire", "<fcolor:1>☠ <target> burned to death");
            put("death.attack.onFire.item", "<fcolor:1>☠ <target> was burned to a crisp while fighting <killer> wielding <fcolor:2>[<killer_item>]<fcolor:1>");
            put("death.attack.onFire.player", "<fcolor:1>☠ <target> was burned to a crisp while fighting <killer>");
            put("death.attack.outOfWorld", "<fcolor:1>☠ <target> fell out of the world");
            put("death.attack.outOfWorld.player", "<fcolor:1>☠ <target> didn't want to live in the same world as <killer>");
            put("death.attack.outsideBorder", "<fcolor:1>☠ <target> left the confines of this world");
            put("death.attack.outsideBorder.player", "<fcolor:1>☠ <target> left the confines of this world while fighting <killer>");
            put("death.attack.player", "<fcolor:1>☠ <target> was slain by <killer>");
            put("death.attack.player.item", "<fcolor:1>☠ <target> was slain by <killer> using <fcolor:2>[<killer_item>]<fcolor:1>");
            put("death.attack.sonic_boom", "<fcolor:1>☠ <target> was obliterated by a sonically-charged shriek");
            put("death.attack.sonic_boom.item", "<fcolor:1>☠ <target> was obliterated by a sonically-charged shriek while trying to escape <killer> wielding <fcolor:2>[<killer_item>]<fcolor:1>");
            put("death.attack.sonic_boom.player", "<fcolor:1>☠ <target> was obliterated by a sonically-charged shriek while trying to escape <killer>");
            put("death.attack.stalagmite", "<fcolor:1>☠ <target> was impaled on a stalagmite");
            put("death.attack.stalagmite.player", "<fcolor:1>☠ <target> was impaled on a stalagmite while fighting <killer>");
            put("death.attack.starve", "<fcolor:1>☠ <target> starved to death");
            put("death.attack.starve.player", "<fcolor:1>☠ <target> starved to death while fighting <killer>");
            put("death.attack.sting", "<fcolor:1>☠ <target> was stung to death");
            put("death.attack.sting.item", "<fcolor:1>☠ <target> was stung to death by <killer> using <fcolor:2>[<killer_item>]<fcolor:1>");
            put("death.attack.sting.player", "<fcolor:1>☠ <target> was stung to death by <killer>");
            put("death.attack.sweetBerryBush", "<fcolor:1>☠ <target> was poked to death by a sweet berry bush");
            put("death.attack.sweetBerryBush.player", "<fcolor:1>☠ <target> was poked to death by a sweet berry bush while trying to escape <killer>");
            put("death.attack.thorns", "<fcolor:1>☠ <target> was killed while trying to hurt <killer>");
            put("death.attack.thorns.item", "<fcolor:1>☠ <target> was killed by <fcolor:2>[<killer_item>]<fcolor:1> while trying to hurt <killer>");
            put("death.attack.thrown", "<fcolor:1>☠ <target> was pummeled by <killer>");
            put("death.attack.thrown.item", "<fcolor:1>☠ <target> was pummeled by <killer> using <fcolor:2>[<killer_item>]<fcolor:1>");
            put("death.attack.trident", "<fcolor:1>☠ <target> was impaled by <killer>");
            put("death.attack.trident.item", "<fcolor:1>☠ <target> was impaled by <killer> with <fcolor:2>[<killer_item>]<fcolor:1>");
            put("death.attack.wither", "<fcolor:1>☠ <target> withered away");
            put("death.attack.wither.player", "<fcolor:1>☠ <target> withered away while fighting <killer>");
            put("death.attack.witherSkull", "<fcolor:1>☠ <target> was shot by a skull from <killer>");
            put("death.attack.witherSkull.item", "<fcolor:1>☠ <target> was shot by a skull from <killer> using <fcolor:2>[<killer_item>]<fcolor:1>");
            put("death.fell.accident.generic", "<fcolor:1>☠ <target> fell from a high place");
            put("death.fell.accident.ladder", "<fcolor:1>☠ <target> fell off a ladder");
            put("death.fell.accident.other_climbable", "<fcolor:1>☠ <target> fell while climbing");
            put("death.fell.accident.scaffolding", "<fcolor:1>☠ <target> fell off scaffolding");
            put("death.fell.accident.twisting_vines", "<fcolor:1>☠ <target> fell off some twisting vines");
            put("death.fell.accident.vines", "<fcolor:1>☠ <target> fell off some vines");
            put("death.fell.accident.weeping_vines", "<fcolor:1>☠ <target> fell off some weeping vines");
            put("death.fell.assist", "<fcolor:1>☠ <target> was doomed to fall by <killer>");
            put("death.fell.assist.item", "<fcolor:1>☠ <target> was doomed to fall by <killer> using <fcolor:2>[<killer_item>]<fcolor:1>");
            put("death.fell.finish", "<fcolor:1>☠ <target> fell too far and was finished by <killer>");
            put("death.fell.finish.item", "<fcolor:1>☠ <target> fell too far and was finished by <killer> using <fcolor:2>[<killer_item>]<fcolor:1>");
            put("death.fell.killer", "<fcolor:1>☠ <target> was doomed to fall");
        }};

        localization.message.debugstick.empty = "<fcolor:1>\uD83D\uDD27 <fcolor:2><property> <fcolor:1>has no properties";
        localization.message.debugstick.select = "<fcolor:1>\uD83D\uDD27 selected \"<fcolor:2><property><fcolor:1>\" (<fcolor:2><value><fcolor:1>)";
        localization.message.debugstick.update = "<fcolor:1>\uD83D\uDD27 \"<fcolor:2><property><fcolor:1>\" to <fcolor:2><value>";

        localization.message.deop.format = "<fcolor:1>\uD83E\uDD16 Made <target> no longer a server operator";

        localization.message.dialog.clear.single = "<fcolor:1>\uD83D\uDDD4 Cleared dialog for <target>";
        localization.message.dialog.clear.multiple = "<fcolor:1>\uD83D\uDDD4 Cleared dialog for <fcolor:2><players> <fcolor:1>players";
        localization.message.dialog.show.single = "<fcolor:1>\uD83D\uDDD4 Displayed dialog to <target>";
        localization.message.dialog.show.multiple = "<fcolor:1>\uD83D\uDDD4 Displayed dialog to <fcolor:2><players> <fcolor:1>players";

        localization.message.difficulty.query = "<fcolor:1>⚔ The difficulty is <fcolor:2><lang:'<difficulty>'>";
        localization.message.difficulty.success = "<fcolor:1>⚔ The difficulty has been set to <fcolor:2><lang:'<difficulty>'>";

        localization.message.effect.clear.everything.single = "<fcolor:1>⚗ Removed every effect from <target>";
        localization.message.effect.clear.everything.multiple = "<fcolor:1>⚗ Removed every effect from <fcolor:2><players> <fcolor:1>targets";
        localization.message.effect.clear.specific.single = "<fcolor:1>⚗ Removed effect <fcolor:2><lang:'<effect>'> <fcolor:1>from <target>";
        localization.message.effect.clear.specific.multiple = "<fcolor:1>⚗ Removed effect <fcolor:2><lang:'<effect>'> <fcolor:1>from <fcolor:2><players> <fcolor:1>targets";
        localization.message.effect.give.single = "<fcolor:1>⚗ Applied effect <fcolor:2><lang:'<effect>'> <fcolor:1>to <target>";
        localization.message.effect.give.multiple = "<fcolor:1>⚗ Applied effect <fcolor:2><lang:'<effect>'> <fcolor:1>to <fcolor:2><players> <fcolor:1>targets";

        localization.message.enchant.single = "<fcolor:1>\uD83D\uDCD6 Applied enchantment <fcolor:2><enchantment><fcolor:1> to <target>'s item";
        localization.message.enchant.multiple = "<fcolor:1>\uD83D\uDCD6 Applied enchantment <fcolor:2><enchantment><fcolor:1> to <fcolor:2><players><fcolor:1> entities";

        localization.message.execute.pass = "<fcolor:1>⚡ Test passed";
        localization.message.execute.passCount = "<fcolor:1>⚡ Test passed, count: <fcolor:2><count>";

        localization.message.experience.add.levels.single = "<fcolor:1>⏺ Gave <fcolor:2><amount> <fcolor:1>experience levels to <target>";
        localization.message.experience.add.levels.multiple = "<fcolor:1>⏺ Gave <fcolor:2><amount> <fcolor:1>experience levels to <fcolor:2><players> <fcolor:1>players";
        localization.message.experience.add.points.single = "<fcolor:1>⏺ Gave <fcolor:2><amount> <fcolor:1>experience points to <target>";
        localization.message.experience.add.points.multiple = "<fcolor:1>⏺ Gave <fcolor:2><amount> <fcolor:1>experience points to <fcolor:2><players> <fcolor:1>players";
        localization.message.experience.query.levels = "<fcolor:1>⏺ <target> has <fcolor:2><amount> <fcolor:1>experience levels";
        localization.message.experience.query.points = "<fcolor:1>⏺ <target> has <fcolor:2><amount> <fcolor:1>experience points";
        localization.message.experience.set.levels.single = "<fcolor:1>⏺ Set <fcolor:2><amount> <fcolor:1>experience levels to <target>";
        localization.message.experience.set.levels.multiple = "<fcolor:1>⏺ Set <fcolor:2><amount> <fcolor:1>experience levels to <fcolor:2><players> <fcolor:1>players";
        localization.message.experience.set.points.single = "<fcolor:1>⏺ Set <fcolor:2><amount> <fcolor:1>experience points to <target>";
        localization.message.experience.set.points.multiple = "<fcolor:1>⏺ Set <fcolor:2><amount> <fcolor:1>experience points to <fcolor:2><players> <fcolor:1>players";
        localization.message.experience.taken = "<fcolor:1>⏺ Taken <fcolor:2><amount> <fcolor:1>levels from <target>";

        localization.message.fill.format = "<fcolor:1>⏹ Successfully filled <fcolor:2><blocks> <fcolor:1>block(s)";

        localization.message.fillbiome.format = "<fcolor:1>⏹ Biomes set between <fcolor:2><x1><fcolor:1>, <fcolor:2><y1><fcolor:1>, <fcolor:2><z1> <fcolor:1>and <fcolor:2><x2><fcolor:1>, <fcolor:2><y2><fcolor:1>, <fcolor:2><z2>";
        localization.message.fillbiome.formatCount = "<fcolor:1>⏹ <fcolor:2><blocks> <fcolor:1>biome entry/entries set between <fcolor:2><x1><fcolor:1>, <fcolor:2><y1><fcolor:1>, <fcolor:2><z1> <fcolor:1>and <fcolor:2><x2><fcolor:1>, <fcolor:2><y2><fcolor:1>, <fcolor:2><z2>";

        localization.message.format.replacement.spoilerSymbol = "█";
        localization.message.format.replacement.values = new LinkedHashMap<>() {{
            put("smile", "<click:suggest_command:\":)\"><hover:show_text:\":)\">☺</hover></click>");
            put("big_smile", "<click:suggest_command:\":D\"><hover:show_text:\":D\">☻</hover></click>");
            put("sad", "<click:suggest_command:\":(\"><hover:show_text:\":(\">☹</hover></click>");
            put("ok_hand", "<click:suggest_command:\":ok:\"><hover:show_text:\":ok:\">\uD83D\uDD92</hover></click>");
            put("thumbs_up", "<click:suggest_command:\":+1:\"><hover:show_text:\":+1:\">\uD83D\uDD92</hover></click>");
            put("thumbs_down", "<click:suggest_command:\":-1:\"><hover:show_text:\":-1:\">\uD83D\uDD93</hover></click>");
            put("cool_smile", "<click:suggest_command:\":cool:\"><hover:show_text:\":cool:\">\uD83D\uDE0E</hover></click>");
            put("cool_glasses", "<click:suggest_command:\"B)\"><hover:show_text:\"B)\">\uD83D\uDE0E</hover></click>");
            put("clown", "<click:suggest_command:\":clown:\"><hover:show_text:\":clown:\">\uD83E\uDD21</hover></click>");
            put("heart", "<click:suggest_command:\"<3\"><hover:show_text:\"<3\">❤</hover></click>");
            put("laughing", "<click:suggest_command:\"XD\"><hover:show_text:\"XD\">\uD83D\uDE06</hover></click>");
            put("confused", "<click:suggest_command:\"%)\"><hover:show_text:\"%)\">\uD83D\uDE35</hover></click>");
            put("happy", "<click:suggest_command:\"=D\"><hover:show_text:\"=D\">\uD83D\uDE03</hover></click>");
            put("angry", "<click:suggest_command:\">:(\"><hover:show_text:\">:(\">\uD83D\uDE21</hover></click>");
            put("ascii_idk", "<click:suggest_command:\":idk:\"><hover:show_text:\":idk:\">¯\\_(ツ)_/¯</hover></click>");
            put("ascii_angry", "<click:suggest_command:\":angry:\"><hover:show_text:\":angry:\">(╯°□°)╯︵ ┻━┻</hover></click>");
            put("ascii_happy", "<click:suggest_command:\":happy:\"><hover:show_text:\":happy:\">＼(＾O＾)／</hover></click>");
            put("ping", "<fcolor:2><ping>");
            put("tps", "<fcolor:2><tps>");
            put("online", "<fcolor:2><online>");
            put("coords", "<fcolor:2><x> <y> <z>");
            put("stats", "<color:#ff7171><hp>♥</color> <color:#3de0d8><armor>🛡 <color:#e33059><attack>🗡 <color:#4eff52><exp>⏺ <color:#f0a01f><food>🍖");
            put("skin", "<click:open_url:\"<message_1>\"><hover:show_text:\"<fcolor:2><pixels>\"><fcolor:2><u>👨 Skin</u></hover></click>");
            put("item", "<fcolor:2>[<message_1>]");
            put("url", "<click:open_url:\"<message_1>\"><hover:show_text:\"<fcolor:2>Open url <br><u><message_1>\"><fcolor:2><u>🗗 Url</u></hover></click>");
            put("image", "<click:open_url:\"<message_1>\"><hover:show_text:\"<fcolor:2><pixels>\"><fcolor:2><u>🖃 Image</u></hover></click>");
            put("spoiler", "<hover:show_text:\"<fcolor:2><message_1>\"><fcolor:2><symbols></hover>");
            put("bold", "<b><message_1></b>");
            put("italic", "<i><message_1></i>");
            put("underline", "<u><message_1></u>");
            put("obfuscated", "<obf><message_1></obf>");
            put("strikethrough", "<st><message_1></st>");
        }};

        localization.message.format.mention.person = "<fcolor:2>You were mentioned!";
        localization.message.format.mention.format = "<fcolor:2>@<target>";

        localization.message.format.moderation.delete.placeholder = "<color:#ff7171><hover:show_text:\"<color:#ff7171>Click to delete message\"><click:run_command:\"/deletemessage <uuid>\">[x] ";
        localization.message.format.moderation.delete.format = "<fcolor:3><i>Message deleted</i>";

        localization.message.format.moderation.newbie.reason = "You're still too new";

        localization.message.format.moderation.swear.symbol = "❤";

        localization.message.format.names.constant = "";
        localization.message.format.names.display = "<click:suggest_command:\"/msg <player> \"><hover:show_text:\"<fcolor:2>Write to <player_head><player>\"<player_head><vault_prefix><stream_prefix><fcolor:2><player><afk_suffix><vault_suffix></hover></click>";
        localization.message.format.names.entity = "<fcolor:2><hover:show_text:\"<fcolor:2><name> <br><fcolor:1>Type <fcolor:2><lang:'<type>'> <br><fcolor:1>ID <fcolor:2><uuid>\"><sprite:gui:icon/accessibility><name></hover>";
        localization.message.format.names.unknown = "<fcolor:2><name>";
        localization.message.format.names.invisible = "<fcolor:2>\uD83D\uDC7B Invisible";

        localization.message.format.questionAnswer.questions = new LinkedHashMap<>() {{
            put("server", "<fcolor:2>[Answer] @<player><fcolor:1>, this is a vanilla server in minecraft!");
            put("flectone", "<fcolor:2>[Answer] @<player><fcolor:1>, this is a brand and projects created by TheFaser");
        }};

        localization.message.format.translate.action = " <click:run_command:\"/translateto <language> <language> <message>\"><hover:show_text:\"<fcolor:2>Translate message\"><fcolor:1>⇄";

        localization.message.gamemode.setDefault = "<fcolor:1>\uD83D\uDDD8 The default game mode is now <fcolor:2><lang:'<gamemode>'>";
        localization.message.gamemode.self = "<fcolor:1>\uD83D\uDDD8 Set own game mode to <fcolor:2><lang:'<gamemode>'>";
        localization.message.gamemode.other = "<fcolor:1>\uD83D\uDDD8 Set <target>'s game mode to <fcolor:2><lang:'<gamemode>'>";

        localization.message.gamerule.query = "<fcolor:1>\uD83D\uDDD0 Gamerule <fcolor:2><gamerule> <fcolor:1>is currently set to: <fcolor:2><value>";
        localization.message.gamerule.set = "<fcolor:1>\uD83D\uDDD0 Gamerule <fcolor:2><gamerule> <fcolor:1>is now set to: <fcolor:2><value>";

        localization.message.give.single = "<fcolor:1>⛏ Gave <fcolor:2><items> <fcolor:1>[<fcolor:2><hover:show_text:\"<fcolor:2><give_item>\"><give_item></hover><fcolor:1>] to <target>";
        localization.message.give.multiple = "<fcolor:1>⛏ Gave <fcolor:2><items> <fcolor:1>[<fcolor:2><hover:show_text:\"<fcolor:2><give_item>\"><give_item></hover><fcolor:1>] to <players> players";

        localization.message.greeting.format = "<br>[#][#][#][#][#][#][#][#]<br>[#][#][#][#][#][#][#][#]<br>[#][#][#][#][#][#][#][#]<br>[#][#][#][#][#][#][#][#]<br>[#][#][#][#][#][#][#][#]  <fcolor:1>Hello,<br>[#][#][#][#][#][#][#][#]  <player><br>[#][#][#][#][#][#][#][#]<br>[#][#][#][#][#][#][#][#]<br>";

        localization.message.join.format = "<color:#4eff52>→ <display_name>";
        localization.message.join.formatFirstTime = "<color:#4eff52>→ <display_name> <fcolor:1>welcome!";

        localization.message.kill.single = "<color:#778899>☠ <fcolor:1>Killed <target>";
        localization.message.kill.multiple = "<color:#778899>☠ <fcolor:1>Killed <fcolor:2><entities> <fcolor:1>entities";

        localization.message.locate.biome = "<fcolor:1>\uD83D\uDDFA The nearest <fcolor:2><value> <fcolor:1>is at <fcolor:2><hover:show_text:\"<fcolor:2>Click to teleport\"><click:suggest_command:\"/tp @p <x> <y> <z>\">[<x>, <y>, <z>]</click></hover> <fcolor:1>(<fcolor:2><blocks> <fcolor:1>blocks away)";
        localization.message.locate.poi = "<fcolor:1>\uD83D\uDDFA The nearest <fcolor:2><value> <fcolor:1>is at <fcolor:2><hover:show_text:\"<fcolor:2>Click to teleport\"><click:suggest_command:\"/tp @p <x> <y> <z>\">[<x>, <y>, <z>]</click></hover> <fcolor:1>(<fcolor:2><blocks> <fcolor:1>blocks away)";
        localization.message.locate.structure = "<fcolor:1>\uD83D\uDDFA The nearest <fcolor:2><value> <fcolor:1>is at <fcolor:2><hover:show_text:\"<fcolor:2>Click to teleport\"><click:suggest_command:\"/tp @p <x> <y> <z>\">[<x>, <y>, <z>]</click></hover> <fcolor:1>(<fcolor:2><blocks> <fcolor:1>blocks away)";

        localization.message.objective.belowname.format = "<fcolor:1>ms";

        localization.message.op.format = "<fcolor:1>\uD83E\uDD16 Made <target> a server operator";

        localization.message.particle.format = "<fcolor:1>❄ Displaying particle <fcolor:2><lang:'<particle>'>";

        localization.message.quit.format = "<color:#ff4e4e>← <display_name>";

        localization.message.recipe.give.single = "<fcolor:1>\uD83D\uDCA1 Unlocked <fcolor:2><recipes> <fcolor:1>recipe(s) for <target>";
        localization.message.recipe.give.multiple = "<fcolor:1>\uD83D\uDCA1 Unlocked <fcolor:2><recipes> <fcolor:1>recipe(s) for <fcolor:2><players> <fcolor:1>players";
        localization.message.recipe.take.single = "<fcolor:1>\uD83D\uDCA1 Took <fcolor:2><recipes> <fcolor:1>recipe(s) from <target>";
        localization.message.recipe.take.multiple = "<fcolor:1>\uD83D\uDCA1 Took <fcolor:2><recipes> <fcolor:1>recipe(s) from <fcolor:2><players> <fcolor:1>players";

        localization.message.reload.format = "<fcolor:1>\uD83D\uDEC8 Reloading!";

        localization.message.ride.dismount = "<fcolor:1>\uD83C\uDFC7 <target> stopped riding <second_target>";
        localization.message.ride.mount = "<fcolor:1>\uD83C\uDFC7 <target> started riding <second_target>";

        localization.message.rightclick.format = "<fcolor:1>◁ <display_name> ▷";

        localization.message.rotate.format = "<fcolor:1>\uD83E\uDD38 Rotated <target>";

        localization.message.save.disabled = "<fcolor:1>\uD83D\uDEC8 Automatic saving is now disabled";
        localization.message.save.enabled = "<fcolor:1>\uD83D\uDEC8 Automatic saving is now enabled";
        localization.message.save.saving = "<fcolor:1>\uD83D\uDEC8 Saving the game (this may take a moment!)";
        localization.message.save.success = "<fcolor:1>\uD83D\uDEC8 Saved the game";

        localization.message.seed.format = "<fcolor:1>\uD83D\uDD11 Seed: [<fcolor:2><hover:show_text:'<fcolor:2>Click to Copy to Clipboard'><click:copy_to_clipboard:<seed>><seed></click></hover><fcolor:1>]";

        localization.message.setblock.format = "<fcolor:1>⏹ Changed the block at <fcolor:2><x><fcolor:1>, <fcolor:2><y><fcolor:1>, <fcolor:2><z><fcolor:1>";

        localization.message.sidebar.values = new LinkedList<>() {{
            add(new LinkedList<>() {{
                add(" ");
                add("<fcolor:1>Ping <ping>");
                add(" ");
                add("<fcolor:1>FlectonePulse");
            }});
            add(new LinkedList<>() {{
                add(" ");
                add("<fcolor:2>TPS <tps>");
                add(" ");
                add("<fcolor:2>FlectonePulse");
            }});
        }};

        localization.message.sleep.notPossible = "<fcolor:1>\uD83D\uDECC No amount of rest can pass this night";
        localization.message.sleep.playersSleeping = "<fcolor:1>\uD83D\uDECC <fcolor:2><players_sleeping><fcolor:1>/<fcolor:2><players><fcolor:1> players sleeping";
        localization.message.sleep.skippingNight = "<fcolor:1>\uD83D\uDECC Sleeping through this night";

        localization.message.sound.play.multiple = "<fcolor:1>\uD83D\uDD0A Played sound <fcolor:2><sound> <fcolor:1>to <fcolor:2><players> <fcolor:1>players";
        localization.message.sound.play.single = "<fcolor:1>\uD83D\uDD0A Played sound <fcolor:2><sound> <fcolor:1>to <target>";
        localization.message.sound.stop.sourceAny = "<fcolor:1>\uD83D\uDD07 Stopped all '<fcolor:2><source><fcolor:1>' sounds";
        localization.message.sound.stop.sourceSound = "<fcolor:1>\uD83D\uDD07 Stopped sound '<fcolor:2><sound><fcolor:1>' on source '<fcolor:2><source><fcolor:1>'";
        localization.message.sound.stop.sourcelessAny = "<fcolor:1>\uD83D\uDD07 Stopped all sounds";
        localization.message.sound.stop.sourcelessSound = "<fcolor:1>\uD83D\uDD07 Stopped sound '<fcolor:2><sound><fcolor:1>'";

        localization.message.spawn.notValid = "<fcolor:1>\uD83D\uDECC You have no home bed or charged respawn anchor, or it was obstructed";
        localization.message.spawn.set = "<fcolor:1>\uD83D\uDECC Respawn point set";
        localization.message.spawn.setWorld = "<fcolor:1>\uD83D\uDECC Set the world spawn point to <fcolor:2><x><fcolor:1>, <fcolor:2><y><fcolor:1>, <fcolor:2><z><fcolor:1> [<fcolor:2><angle><fcolor:1>, <fcolor:2><yaw><fcolor:1>]";
        localization.message.spawn.single = "<fcolor:1>\uD83D\uDECC Set spawn point to <fcolor:2><x><fcolor:1>, <fcolor:2><y><fcolor:1>, <fcolor:2><z><fcolor:1> [<fcolor:2><angle><fcolor:1>, <fcolor:2><yaw><fcolor:1>] in <fcolor:2><world><fcolor:1> for <target>";
        localization.message.spawn.multiple = "<fcolor:1>\uD83D\uDECC Set spawn point to <fcolor:2><x><fcolor:1>, <fcolor:2><y><fcolor:1>, <fcolor:2><z><fcolor:1> [<fcolor:2><angle><fcolor:1>, <fcolor:2><yaw><fcolor:1>] in <fcolor:2><world><fcolor:1> for <fcolor:2><players><fcolor:1> players";

        localization.message.status.motd.values = new LinkedList<>() {{
            add("<fcolor:1>Welcome to our server!");
            add("<fcolor:1>Join us and enjoy a unique game experience!");
            add("<fcolor:1>We have a friendly community - be polite and respect each other!");
            add("<fcolor:1>Enjoy the game! If you have any questions, feel free to contact the administration");
        }};
        localization.message.status.players.full = "<color:#ff7171>The server is full";
        localization.message.status.players.samples = new LinkedList<>(List.of(new Localization.Message.Status.Players.Sample()));
        localization.message.status.version.name = "Minecraft server";

        localization.message.stop.format = "<fcolor:1>\uD83D\uDEC8 Stopping the server";

        localization.message.summon.format = "<fcolor:1>\uD83D\uDC3A Summoned new <target>";

        localization.message.tab.footer.lists = new LinkedList<>() {{
            add(new LinkedList<>() {{
                add(" ");
                add("<fcolor:1>Hello <fcolor:2><player><fcolor:1>!");
                add(" ");
            }});
            add(new LinkedList<>() {{
                add(" ");
                add("<fcolor:1>TPS <tps>, Online <online>");
                add(" ");
            }});
        }};
        localization.message.tab.header.lists = new LinkedList<>() {{
            add(new LinkedList<>() {{
                add(" ");
                add("<fcolor:1>❤");
                add(" ");
            }});
            add(new LinkedList<>() {{
                add(" ");
                add("<fcolor:1>\uD83D\uDC7E");
                add(" ");
            }});
        }};
        localization.message.tab.playerlistname.format = "<world_prefix>▋ <reset><vault_prefix><stream_prefix><fcolor:2><player><afk_suffix><vault_suffix>";

        localization.message.teleport.entity.single = "<fcolor:1>\uD83C\uDF00 Teleported <target> to <second_target>";
        localization.message.teleport.entity.multiple = "<fcolor:1>\uD83C\uDF00 Teleported <fcolor:2><entities> <fcolor:1>entities to <second_target>";
        localization.message.teleport.location.single = "<fcolor:1>\uD83C\uDF00 Teleported <target> to <fcolor:2><x><fcolor:1>, <fcolor:2><y><fcolor:1>, <fcolor:2><z><fcolor:1>";
        localization.message.teleport.location.multiple = "<fcolor:1>\uD83C\uDF00 Teleported <fcolor:2><entities> <fcolor:1>to <fcolor:2><x><fcolor:1>, <fcolor:2><y><fcolor:1>, <fcolor:2><z><fcolor:1>";

        localization.message.time.query = "<fcolor:1>☽ The time is <fcolor:2><time>";
        localization.message.time.set = "<fcolor:1>☽ Set the time to <fcolor:2><time>";

        localization.message.update.formatPlayer = "<fcolor:1><fcolor:2>(FlectonePulse)<fcolor:1> Your version <fcolor:2><current_version><fcolor:1> is outdated! Update to <fcolor:2><latest_version><fcolor:1> at <url:https://modrinth.com/plugin/flectonepulse>, to get new opportunities!";
        localization.message.update.formatConsole = "<fcolor:1>Your version <fcolor:2><current_version><fcolor:1> is outdated! Update to <fcolor:2><latest_version><fcolor:1> at <click:open_url:https://modrinth.com/plugin/flectonepulse>https://modrinth.com/plugin/flectonepulse";

        localization.message.weather.clear = "<fcolor:1>☀ Set the weather to <fcolor:2>clear";
        localization.message.weather.rain = "<fcolor:1>\uD83C\uDF27 Set the weather to <fcolor:2>rain";
        localization.message.weather.thunder = "<fcolor:1>⛈ Set the weather to <fcolor:2>rain & thunder";

        localization.message.worldborder.center = "<fcolor:1>\uD83D\uDEAB Set the center of the world border to <fcolor:2><value><fcolor:1>, <fcolor:2><second_value>";
        localization.message.worldborder.damage.amount = "<fcolor:1>\uD83D\uDEAB Set the world border damage to <fcolor:2><value> <fcolor:1>per block each second";
        localization.message.worldborder.damage.buffer = "<fcolor:1>\uD83D\uDEAB Set the world border damage buffer to <fcolor:2><value> <fcolor:1>block(s)";
        localization.message.worldborder.get = "<fcolor:1>\uD83D\uDEAB The world border is currently <fcolor:2><value> <fcolor:1>block(s) wide";
        localization.message.worldborder.set.grow = "<fcolor:1>\uD83D\uDEAB Growing the world border to <fcolor:2><value> <fcolor:1>blocks wide over <fcolor:2><second_value> <fcolor:1>seconds";
        localization.message.worldborder.set.immediate = "<fcolor:1>\uD83D\uDEAB Set the world border to <fcolor:2><value> <fcolor:1>block(s) wide";
        localization.message.worldborder.set.shrink = "<fcolor:1>\uD83D\uDEAB Shrinking the world border to <fcolor:2><value> <fcolor:1>block(s) wide over <fcolor:2><second_value> <fcolor:1>second(s)";
        localization.message.worldborder.warning.distance = "<fcolor:1>\uD83D\uDEAB Set the world border warning distance to <fcolor:2><value> <fcolor:1>block(s)";
        localization.message.worldborder.warning.time = "<fcolor:1>\uD83D\uDEAB Set the world border warning time to <fcolor:2><value> <fcolor:1>second(s)";
    }

}
