package net.flectone.pulse.config;

import com.fasterxml.jackson.annotation.*;
import lombok.Builder;
import lombok.With;
import lombok.extern.jackson.Jacksonized;
import net.flectone.pulse.config.setting.LocalizationSetting;
import org.apache.commons.lang3.StringUtils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@With
@Builder(toBuilder = true)
@Jacksonized
public record Localization(

        @JsonIgnore
        String language,

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/message/")
        String cooldown,

        Time time,

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/command/")
        Command command,

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/integration/")
        Integration integration,

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/message/")
        Message message
) {

    public static final String FOLDER_NAME = "localizations";

    @With
    @Builder(toBuilder = true)
    @Jacksonized
    public record Time(
            String format,
            String permanent,
            String zero
    ) {}

    @With
    @Builder(toBuilder = true)
    @Jacksonized
    public record Command(
            Exception exception,
            Prompt prompt,

            @JsonPropertyDescription(" https://flectone.net/pulse/docs/command/anon/")
            Anon anon,

            @JsonPropertyDescription(" https://flectone.net/pulse/docs/command/ball/")
            Ball ball,

            @JsonPropertyDescription(" https://flectone.net/pulse/docs/command/ban/")
            Ban ban,

            @JsonPropertyDescription(" https://flectone.net/pulse/docs/command/banlist/")
            Banlist banlist,

            @JsonPropertyDescription(" https://flectone.net/pulse/docs/command/broadcast/")
            Broadcast broadcast,

            @JsonPropertyDescription(" https://flectone.net/pulse/docs/command/chatcolor/")
            Chatcolor chatcolor,

            @JsonPropertyDescription(" https://flectone.net/pulse/docs/command/chatsetting/")
            Chatsetting chatsetting,

            @JsonPropertyDescription(" https://flectone.net/pulse/docs/command/clearchat/")
            Clearchat clearchat,

            @JsonPropertyDescription(" https://flectone.net/pulse/docs/command/clearmail/")
            Clearmail clearmail,

            @JsonPropertyDescription(" https://flectone.net/pulse/docs/command/coin/")
            Coin coin,

            @JsonPropertyDescription(" https://flectone.net/pulse/docs/command/deletemessage/")
            Deletemessage deletemessage,

            @JsonPropertyDescription(" https://flectone.net/pulse/docs/command/dice/")
            Dice dice,

            @JsonPropertyDescription(" https://flectone.net/pulse/docs/command/do/")
            @JsonProperty("do")
            CommandDo commandDo,

            @JsonPropertyDescription(" https://flectone.net/pulse/docs/command/emit/")
            Emit emit,

            @JsonPropertyDescription(" https://flectone.net/pulse/docs/command/flectonepulse/")
            Flectonepulse flectonepulse,

            @JsonPropertyDescription(" https://flectone.net/pulse/docs/command/geolocate/")
            Geolocate geolocate,

            @JsonPropertyDescription(" https://flectone.net/pulse/docs/command/helper/")
            Helper helper,

            @JsonPropertyDescription(" https://flectone.net/pulse/docs/command/ignore/")
            Ignore ignore,

            @JsonPropertyDescription(" https://flectone.net/pulse/docs/command/ignorelist/")
            Ignorelist ignorelist,

            @JsonPropertyDescription(" https://flectone.net/pulse/docs/command/kick/")
            Kick kick,

            @JsonPropertyDescription(" https://flectone.net/pulse/docs/command/mail/")
            Mail mail,

            @JsonPropertyDescription(" https://flectone.net/pulse/docs/command/maintenance/")
            Maintenance maintenance,

            @JsonPropertyDescription(" https://flectone.net/pulse/docs/command/me/")
            Me me,

            @JsonPropertyDescription(" https://flectone.net/pulse/docs/command/mute/")
            Mute mute,

            @JsonPropertyDescription(" https://flectone.net/pulse/docs/command/mutelist/")
            Mutelist mutelist,

            @JsonPropertyDescription(" https://flectone.net/pulse/docs/command/online/")
            Online online,

            @JsonPropertyDescription(" https://flectone.net/pulse/docs/command/ping/")
            Ping ping,

            @JsonPropertyDescription(" https://flectone.net/pulse/docs/command/poll/")
            Poll poll,

            @JsonPropertyDescription(" https://flectone.net/pulse/docs/command/reply/")
            Reply reply,

            @JsonPropertyDescription(" https://flectone.net/pulse/docs/command/rockpaperscissors/")
            Rockpaperscissors rockpaperscissors,

            @JsonPropertyDescription(" https://flectone.net/pulse/docs/command/spy/")
            Spy spy,

            @JsonPropertyDescription(" https://flectone.net/pulse/docs/command/stream/")
            Stream stream,

            @JsonPropertyDescription(" https://flectone.net/pulse/docs/command/symbol/")
            Symbol symbol,

            @JsonPropertyDescription(" https://flectone.net/pulse/docs/command/tell/")
            Tell tell,

            @JsonPropertyDescription(" https://flectone.net/pulse/docs/command/tictactoe/")
            Tictactoe tictactoe,

            @JsonPropertyDescription(" https://flectone.net/pulse/docs/command/toponline/")
            Toponline toponline,

            @JsonPropertyDescription(" https://flectone.net/pulse/docs/command/translateto/")
            Translateto translateto,

            @JsonPropertyDescription(" https://flectone.net/pulse/docs/command/try/")
            @JsonProperty("try")
            CommandTry commandTry,

            @JsonPropertyDescription(" https://flectone.net/pulse/docs/command/unban/")
            Unban unban,

            @JsonPropertyDescription(" https://flectone.net/pulse/docs/command/unmute/")
            Unmute unmute,

            @JsonPropertyDescription(" https://flectone.net/pulse/docs/command/unwarn/")
            Unwarn unwarn,

            @JsonPropertyDescription(" https://flectone.net/pulse/docs/command/warn/")
            Warn warn,

            @JsonPropertyDescription(" https://flectone.net/pulse/docs/command/warnlist/")
            Warnlist warnlist
    ) implements LocalizationSetting {

        @With
        @Builder(toBuilder = true)
        @Jacksonized
        public record Exception(
                String execution,
                String parseUnknown,
                String parseBoolean,
                String parseNumber,
                String parseString,
                String permission,
                String syntax
        ) {}

        @With
        @Builder(toBuilder = true)
        @Jacksonized
        public record Prompt(
                String message,
                String hard,
                String accept,
                String turn,
                String type,
                String reason,
                String category,
                String id,
                String time,
                String repeatTime,
                String multipleVote,
                String player,
                String number,
                String color,
                String language,
                String url,
                String move,
                String value
        ) {}

        @With
        @Builder(toBuilder = true)
        @Jacksonized
        public record Anon(String format) implements LocalizationSetting {}

        @With
        @Builder(toBuilder = true)
        @Jacksonized
        public record Ball(
                String format,
                List<String> answers
        ) implements LocalizationSetting {}

        @With
        @Builder(toBuilder = true)
        @Jacksonized
        public record Ban(
                String nullPlayer,
                String nullTime,
                String lowerWeightGroup,
                ReasonMap reasons,
                String server,
                String person,
                String connectionAttempt
        ) implements LocalizationSetting {

            @With
            @Builder(toBuilder = true)
            @Jacksonized
            public record Type(
                    String connectionAttempt,
                    ReasonMap reasons
            ) {}
        }

        @With
        @Builder(toBuilder = true)
        @Jacksonized
        public record Banlist(
                String empty,
                String nullPage,
                String nullPlayer,
                ListTypeMessage global,
                ListTypeMessage player
        ) implements LocalizationSetting {}

        @With
        @Builder(toBuilder = true)
        @Jacksonized
        public record Broadcast(String format) implements LocalizationSetting {}

        @With
        @Builder(toBuilder = true)
        @Jacksonized
        public record Chatcolor(
                String nullPlayer,
                String nullType,
                String nullColor,
                String format
        ) implements LocalizationSetting {}

        @With
        @Builder(toBuilder = true)
        @Jacksonized
        public record Chatsetting(
                String noPermission,
                String disabledSelf,
                String disabledOther,
                String inventory,
                Checkbox checkbox,
                Menu menu
        ) implements LocalizationSetting {

            @With
            @Builder(toBuilder = true)
            @Jacksonized
            public record Checkbox(
                    String enabledColor,
                    String enabledHover,
                    String disabledColor,
                    String disabledHover,
                    Map<String, String> types
            ) {}

            @With
            @Builder(toBuilder = true)
            @Jacksonized
            public record Menu(
                    SubMenu chat,
                    SubMenu see,
                    SubMenu out
            ) {

                @With
                @Builder(toBuilder = true)
                @Jacksonized
                public record SubMenu(
                        String item,
                        String inventory,
                        Map<String, String> types
                ) {}
            }
        }

        @With
        @Builder(toBuilder = true)
        @Jacksonized
        public record Clearchat(
                String nullPlayer,
                String format
        ) implements LocalizationSetting {}

        @With
        @Builder(toBuilder = true)
        @Jacksonized
        public record Clearmail(
                String nullMail,
                String format
        ) implements LocalizationSetting {}

        @With
        @Builder(toBuilder = true)
        @Jacksonized
        public record Coin(
                String head,
                String tail,
                String format,
                String formatDraw
        ) implements LocalizationSetting {}

        @With
        @Builder(toBuilder = true)
        @Jacksonized
        public record Deletemessage(
                String nullMessage,
                String format
        ) implements LocalizationSetting {}

        @With
        @Builder(toBuilder = true)
        @Jacksonized
        public record Dice(
                Map<Integer, String> symbols,
                String format
        ) implements LocalizationSetting {}

        @With
        @Builder(toBuilder = true)
        @Jacksonized
        public record CommandDo(String format) implements LocalizationSetting {}

        @With
        @Builder(toBuilder = true)
        @Jacksonized
        public record Emit(
                String nullPlayer,
                String format
        ) implements LocalizationSetting {}

        @With
        @Builder(toBuilder = true)
        @Jacksonized
        public record Flectonepulse(
                String nullHostEditor,
                String nullPortEditor,
                String formatFalse,
                String formatTrue,
                String formatWebStarting,
                String formatEditor
        ) implements LocalizationSetting {}

        @With
        @Builder(toBuilder = true)
        @Jacksonized
        public record Geolocate(
                String nullPlayer,
                String nullOrError,
                String format
        ) implements LocalizationSetting {}

        @With
        @Builder(toBuilder = true)
        @Jacksonized
        public record Helper(
                String nullHelper,
                String global,
                String player
        ) implements LocalizationSetting {}

        @With
        @Builder(toBuilder = true)
        @Jacksonized
        public record Ignore(
                String nullPlayer,
                String myself,
                String he,
                String you,
                String formatTrue,
                String formatFalse
        ) implements LocalizationSetting {}

        @With
        @Builder(toBuilder = true)
        @Jacksonized
        public record Ignorelist(
                String empty,
                String nullPage,
                String header,
                String line,
                String footer
        ) implements LocalizationSetting {}

        @With
        @Builder(toBuilder = true)
        @Jacksonized
        public record Kick(
                String nullPlayer,
                ReasonMap reasons,
                String server,
                String person
        ) implements LocalizationSetting {}

        @With
        @Builder(toBuilder = true)
        @Jacksonized
        public record Mail(
                String nullPlayer,
                String onlinePlayer,
                String sender,
                String receiver
        ) implements LocalizationSetting {}

        @With
        @Builder(toBuilder = true)
        @Jacksonized
        public record Maintenance(
                String serverDescription,
                String serverVersion,
                String kick,
                String formatTrue,
                String formatFalse
        ) implements LocalizationSetting {}

        @With
        @Builder(toBuilder = true)
        @Jacksonized
        public record Me(String format) implements LocalizationSetting {}

        @With
        @Builder(toBuilder = true)
        @Jacksonized
        public record Mute(
                String nullPlayer,
                String nullTime,
                String lowerWeightGroup,
                String suffix,
                ReasonMap reasons,
                String server,
                String person
        ) implements LocalizationSetting {}

        @With
        @Builder(toBuilder = true)
        @Jacksonized
        public record Mutelist(
                String empty,
                String nullPage,
                String nullPlayer,
                ListTypeMessage global,
                ListTypeMessage player
        ) implements LocalizationSetting {}

        @With
        @Builder(toBuilder = true)
        @Jacksonized
        public record Online(
                String nullPlayer,
                String formatCurrent,
                String formatFirst,
                String formatLast,
                String formatTotal
        ) implements LocalizationSetting {}

        @With
        @Builder(toBuilder = true)
        @Jacksonized
        public record Ping(
                String nullPlayer,
                String format
        ) implements LocalizationSetting {}

        @With
        @Builder(toBuilder = true)
        @Jacksonized
        public record Poll(
                String nullPoll,
                String expired,
                String already,
                String voteTrue,
                String voteFalse,
                String format,
                String answerTemplate,
                Status status,
                Modern modern
        ) implements LocalizationSetting {

            @With
            @Builder(toBuilder = true)
            @Jacksonized
            public record Status(
                    String start,
                    String run,
                    String end
            ) {}

            @With
            @Builder(toBuilder = true)
            @Jacksonized
            public record Modern(
                    String header,
                    String inputName,
                    String inputInitial,
                    String multipleName,
                    String endTimeName,
                    String repeatTimeName,
                    String newAnswerButtonName,
                    String removeAnswerButtonName,
                    String inputAnswerName,
                    String inputAnswersInitial,
                    String createButtonName
            ) {}
        }

        @With
        @Builder(toBuilder = true)
        @Jacksonized
        public record Reply(String nullReceiver) implements LocalizationSetting {}

        @With
        @Builder(toBuilder = true)
        @Jacksonized
        public record Rockpaperscissors(
                String nullPlayer,
                String nullGame,
                String wrongMove,
                String already,
                String myself,
                String sender,
                String receiver,
                String formatMove,
                String formatWin,
                String formatDraw,
                Map<String, String> strategies
        ) implements LocalizationSetting {}

        @With
        @Builder(toBuilder = true)
        @Jacksonized
        public record Spy(
                String formatTrue,
                String formatFalse,
                String formatLog
        ) implements LocalizationSetting {}

        @With
        @Builder(toBuilder = true)
        @Jacksonized
        public record Stream(
                String already,
                String not,
                String prefixTrue,
                String prefixFalse,
                String urlTemplate,
                String formatStart,
                String formatEnd
        ) implements LocalizationSetting {}

        @With
        @Builder(toBuilder = true)
        @Jacksonized
        public record Symbol(String format) implements LocalizationSetting {}

        @With
        @Builder(toBuilder = true)
        @Jacksonized
        public record Tell(
                String nullPlayer,
                String sender,
                String receiver,
                String myself
        ) implements LocalizationSetting {}

        @With
        @Builder(toBuilder = true)
        @Jacksonized
        public record Tictactoe(
                String nullPlayer,
                String myself,
                String wrongGame,
                String wrongMove,
                String wrongByPlayer,
                Symbol symbol,
                String field,
                String currentMove,
                String lastMove,
                String formatMove,
                String formatWin,
                String formatDraw,
                String sender,
                String receiver
        ) implements LocalizationSetting {

            @With
            @Builder(toBuilder = true)
            @Jacksonized
            public record Symbol(
                    String blank,
                    String first,
                    String firstRemove,
                    String firstWin,
                    String second,
                    String secondRemove,
                    String secondWin
            ) {}
        }

        @With
        @Builder(toBuilder = true)
        @Jacksonized
        public record Toponline(
                String nullPage,
                String header,
                String line,
                String footer
        ) implements LocalizationSetting {}

        @With
        @Builder(toBuilder = true)
        @Jacksonized
        public record Translateto(
                String nullOrError,
                String format
        ) implements LocalizationSetting {}

        @With
        @Builder(toBuilder = true)
        @Jacksonized
        public record CommandTry(
                String formatTrue,
                String formatFalse
        ) implements LocalizationSetting {}

        @With
        @Builder(toBuilder = true)
        @Jacksonized
        public record Unban(
                String nullPlayer,
                String notBanned,
                String lowerWeightGroup,
                String format
        ) implements LocalizationSetting {}

        @With
        @Builder(toBuilder = true)
        @Jacksonized
        public record Unmute(
                String nullPlayer,
                String notMuted,
                String lowerWeightGroup,
                String format
        ) implements LocalizationSetting {}

        @With
        @Builder(toBuilder = true)
        @Jacksonized
        public record Unwarn(
                String nullPlayer,
                String notWarned,
                String lowerWeightGroup,
                String format
        ) implements LocalizationSetting {}

        @With
        @Builder(toBuilder = true)
        @Jacksonized
        public record Warn(
                String nullPlayer,
                String nullTime,
                String lowerWeightGroup,
                ReasonMap reasons,
                String server,
                String person
        ) implements LocalizationSetting {}

        @With
        @Builder(toBuilder = true)
        @Jacksonized
        public record Warnlist(
                String empty,
                String nullPage,
                String nullPlayer,
                ListTypeMessage global,
                ListTypeMessage player
        ) implements LocalizationSetting {}
    }

    @With
    @Builder(toBuilder = true)
    @Jacksonized
    public record Integration(
            @JsonPropertyDescription(" https://flectone.net/pulse/docs/integration/discord/")
            Discord discord,

            @JsonPropertyDescription(" https://flectone.net/pulse/docs/integration/telegram/")
            Telegram telegram,

            @JsonPropertyDescription(" https://flectone.net/pulse/docs/integration/twitch/")
            Twitch twitch
    ) implements LocalizationSetting {

        @With
        @Builder(toBuilder = true)
        @Jacksonized
        public record Discord(
                String nullPlayer,
                String formatReply,
                Map<String, ChannelEmbed> customCommand,
                Map<String, String> infoChannel,
                Map<String, ChannelEmbed> messageChannel
        ) implements LocalizationSetting {

            @With
            @Builder(toBuilder = true)
            @Jacksonized
            public record ChannelEmbed(
                    String content,
                    String webhookAvatar,
                    Embed embed
            ) {}

            @With
            @Builder(toBuilder = true)
            @Jacksonized
            public record Embed(
                    String color,
                    String title,
                    String url,
                    Author author,
                    String description,
                    String thumbnail,
                    List<Field> fields,
                    String image,
                    Boolean timestamp,
                    Footer footer
            ) {

                @With
                @Builder(toBuilder = true)
                @Jacksonized
                public record Author(
                        String name,
                        String url,
                        String iconUrl
                ) {}

                @With
                @Builder(toBuilder = true)
                @Jacksonized
                public record Footer(
                        String text,
                        String iconUrl
                ) {}

                @With
                @Builder(toBuilder = true)
                @Jacksonized
                public record Field(
                        String name,
                        String value,
                        Boolean inline
                ) {}
            }
        }

        @With
        @Builder(toBuilder = true)
        @Jacksonized
        public record Telegram(
                String nullPlayer,
                String formatReply,
                Map<String, String> customCommand,
                Map<String, String> infoChannel,
                Map<String, String> messageChannel
        ) implements LocalizationSetting {}

        @With
        @Builder(toBuilder = true)
        @Jacksonized
        public record Twitch(
                String nullPlayer,
                String formatReply,
                Map<String, String> customCommand,
                Map<String, String> messageChannel
        ) implements LocalizationSetting {}
    }

    @With
    @Builder(toBuilder = true)
    @Jacksonized
    public record Message(
            @JsonPropertyDescription(" https://flectone.net/pulse/docs/message/afk/")
            Afk afk,

            @JsonPropertyDescription(" https://flectone.net/pulse/docs/message/auto/")
            Auto auto,

            @JsonPropertyDescription(" https://flectone.net/pulse/docs/message/bossbar/")
            Bossbar bossbar,

            @JsonPropertyDescription(" https://flectone.net/pulse/docs/message/brand/")
            Brand brand,

            @JsonPropertyDescription(" https://flectone.net/pulse/docs/message/bubble/")
            Bubble bubble,

            @JsonPropertyDescription(" https://flectone.net/pulse/docs/message/chat/")
            Chat chat,

            @JsonPropertyDescription(" https://flectone.net/pulse/docs/message/format/")
            Format format,

            @JsonPropertyDescription(" https://flectone.net/pulse/docs/message/greeting/")
            Greeting greeting,

            @JsonPropertyDescription(" https://flectone.net/pulse/docs/message/join/")
            Join join,

            @JsonPropertyDescription(" https://flectone.net/pulse/docs/message/objective/")
            Objective objective,

            @JsonPropertyDescription(" https://flectone.net/pulse/docs/message/quit/")
            Quit quit,

            @JsonPropertyDescription(" https://flectone.net/pulse/docs/message/rightclick/")
            Rightclick rightclick,

            @JsonPropertyDescription(" https://flectone.net/pulse/docs/message/sidebar/")
            Sidebar sidebar,

            @JsonPropertyDescription(" https://flectone.net/pulse/docs/message/status/")
            Status status,

            @JsonPropertyDescription(" https://flectone.net/pulse/docs/message/tab/")
            Tab tab,

            @JsonPropertyDescription(" https://flectone.net/pulse/docs/message/update/")
            Update update,

            @JsonPropertyDescription(" https://flectone.net/pulse/docs/message/vanilla/")
            Vanilla vanilla
    ) implements LocalizationSetting {

        @With
        @Builder(toBuilder = true)
        @Jacksonized
        public record Afk(
                String suffix,
                Format formatTrue,
                Format formatFalse
        ) implements LocalizationSetting {

            @With
            @Builder(toBuilder = true)
            @Jacksonized
            public record Format(
                    String global,
                    String local
            ) {}
        }

        @With
        @Builder(toBuilder = true)
        @Jacksonized
        public record Auto(
                Map<String, List<String>> types
        ) implements LocalizationSetting {}

        @With
        @Builder(toBuilder = true)
        @Jacksonized
        public record Bossbar(
                Map<String, String> announce,
                Map<String, String> types
        ) implements LocalizationSetting {}

        @With
        @Builder(toBuilder = true)
        @Jacksonized
        public record Brand(
                List<String> values
        ) implements LocalizationSetting {}

        @With
        @Builder(toBuilder = true)
        @Jacksonized
        public record Bubble(String format) implements LocalizationSetting {}

        @With
        @Builder(toBuilder = true)
        @Jacksonized
        public record Chat(
                String nullChat,
                String nullReceiver,
                Map<String, String> types
        ) implements LocalizationSetting {}

        @With
        @Builder(toBuilder = true)
        @Jacksonized
        public record Format(
                @JsonPropertyDescription(" https://flectone.net/pulse/docs/message/format/object/")
                Object object,

                @JsonPropertyDescription(" https://flectone.net/pulse/docs/message/format/replacement/")
                Replacement replacement,

                @JsonPropertyDescription(" https://flectone.net/pulse/docs/message/format/mention/")
                Mention mention,

                @JsonPropertyDescription(" https://flectone.net/pulse/docs/message/format/moderation/")
                Moderation moderation,

                @JsonPropertyDescription(" https://flectone.net/pulse/docs/message/format/names/")
                Names names,

                @JsonPropertyDescription(" https://flectone.net/pulse/docs/message/format/questionanswer/")
                QuestionAnswer questionAnswer,

                @JsonPropertyDescription(" https://flectone.net/pulse/docs/message/format/translate/")
                Translate translate
        ) implements LocalizationSetting {

            @With
            @Builder(toBuilder = true)
            @Jacksonized
            public record Object(
                    String defaultSymbol
            ) implements LocalizationSetting {}

            @With
            @Builder(toBuilder = true)
            @Jacksonized
            public record Replacement(
                    String spoilerSymbol,
                    Map<String, String> values
            ) implements LocalizationSetting {}

            @With
            @Builder(toBuilder = true)
            @Jacksonized
            public record Mention(
                    String person,
                    String format
            ) implements LocalizationSetting {}

            @With
            @Builder(toBuilder = true)
            @Jacksonized
            public record Moderation(
                    @JsonPropertyDescription(" https://flectone.net/pulse/docs/message/format/moderation/delete/")
                    Delete delete,

                    @JsonPropertyDescription(" https://flectone.net/pulse/docs/message/format/moderation/newbie/")
                    Newbie newbie,

                    @JsonPropertyDescription(" https://flectone.net/pulse/docs/message/format/moderation/swear/")
                    Swear swear
            ) implements LocalizationSetting {

                @With
                @Builder(toBuilder = true)
                @Jacksonized
                public record Delete(
                        String placeholder,
                        String format
                ) implements LocalizationSetting {}

                @With
                @Builder(toBuilder = true)
                @Jacksonized
                public record Newbie(String reason) implements LocalizationSetting {}

                @With
                @Builder(toBuilder = true)
                @Jacksonized
                public record Swear(String symbol) implements LocalizationSetting {}
            }

            @With
            @Builder(toBuilder = true)
            @Jacksonized
            public record Names(
                    String constant,
                    String display,
                    String entity,
                    String unknown,
                    String invisible
            ) implements LocalizationSetting {}

            @With
            @Builder(toBuilder = true)
            @Jacksonized
            public record QuestionAnswer(
                    Map<String, String> questions
            ) implements LocalizationSetting {}

            @With
            @Builder(toBuilder = true)
            @Jacksonized
            public record Translate(String action) implements LocalizationSetting {}
        }

        @With
        @Builder(toBuilder = true)
        @Jacksonized
        public record Greeting(String format) implements LocalizationSetting {}

        @With
        @Builder(toBuilder = true)
        @Jacksonized
        public record Join(
                String format,
                String formatFirstTime
        ) implements LocalizationSetting {}

        @With
        @Builder(toBuilder = true)
        @Jacksonized
        public record Objective(
                @JsonPropertyDescription(" https://flectone.net/pulse/docs/message/objective/belowname/")
                Belowname belowname
        ) implements LocalizationSetting {

            @With
            @Builder(toBuilder = true)
            @Jacksonized
            public record Belowname(String format) implements LocalizationSetting {}
        }

        @With
        @Builder(toBuilder = true)
        @Jacksonized
        public record Quit(String format) implements LocalizationSetting {}

        @With
        @Builder(toBuilder = true)
        @Jacksonized
        public record Rightclick(String format) implements LocalizationSetting {}

        @With
        @Builder(toBuilder = true)
        @Jacksonized
        public record Sidebar(
                List<List<String>> values
        ) implements LocalizationSetting {}

        @With
        @Builder(toBuilder = true)
        @Jacksonized
        public record Status(
                @JsonPropertyDescription(" https://flectone.net/pulse/docs/message/status/motd/")
                MOTD motd,

                @JsonPropertyDescription(" https://flectone.net/pulse/docs/message/status/players/")
                Players players,

                @JsonPropertyDescription(" https://flectone.net/pulse/docs/message/status/version/")
                Version version
        ) implements LocalizationSetting {

            @With
            @Builder(toBuilder = true)
            @Jacksonized
            public record MOTD(
                    List<String> values
            ) implements LocalizationSetting {}

            @With
            @Builder(toBuilder = true)
            @Jacksonized
            public record Players(
                    List<Sample> samples,
                    String full
            ) implements LocalizationSetting {

                @With
                @Builder(toBuilder = true)
                @Jacksonized
                public record Sample(
                        String name,
                        String id
                ) {}
            }

            @With
            @Builder(toBuilder = true)
            @Jacksonized
            public record Version(String name) implements LocalizationSetting {}
        }

        @With
        @Builder(toBuilder = true)
        @Jacksonized
        public record Tab(
                @JsonPropertyDescription(" https://flectone.net/pulse/docs/message/tab/header/")
                Header header,

                @JsonPropertyDescription(" https://flectone.net/pulse/docs/message/tab/footer/")
                Footer footer,

                @JsonPropertyDescription(" https://flectone.net/pulse/docs/message/tab/playerlistname/")
                Playerlistname playerlistname
        ) implements LocalizationSetting {

            @With
            @Builder(toBuilder = true)
            @Jacksonized
            public record Footer(
                    List<List<String>> lists
            ) implements LocalizationSetting {}

            @With
            @Builder(toBuilder = true)
            @Jacksonized
            public record Header(
                    List<List<String>> lists
            ) implements LocalizationSetting {}

            @With
            @Builder(toBuilder = true)
            @Jacksonized
            public record Playerlistname(String format) implements LocalizationSetting {}
        }

        @With
        @Builder(toBuilder = true)
        @Jacksonized
        public record Update(
                String formatPlayer,
                String formatConsole
        ) implements LocalizationSetting {}

        @With
        @Builder(toBuilder = true)
        @Jacksonized
        public record Vanilla(
                String formatPlayer,
                String formatEntity,
                Map<String, String> types
        ) implements LocalizationSetting {}
    }

    @With
    @Builder(toBuilder = true)
    @Jacksonized
    public record ListTypeMessage(
            String header,
            String line,
            String footer
    ) {}

    public static class ReasonMap extends LinkedHashMap<String, String> {

        public ReasonMap() {
            super(new LinkedHashMap<>());
        }

        public ReasonMap(Map<String, String> map) {
            super(map);
        }

        public String getConstant(String reason) {
            if (StringUtils.isEmpty(reason)) {
                return super.getOrDefault("default", "UNKNOWN");
            }

            return super.getOrDefault(reason, reason);
        }

    }
}