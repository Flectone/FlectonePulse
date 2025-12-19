package net.flectone.pulse.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.Builder;
import lombok.With;
import lombok.extern.jackson.Jacksonized;
import net.flectone.pulse.config.setting.CommandSetting;
import net.flectone.pulse.config.setting.EnableSetting;
import net.flectone.pulse.model.util.Cooldown;
import net.flectone.pulse.model.util.Destination;
import net.flectone.pulse.model.util.Range;
import net.flectone.pulse.model.util.Sound;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@With
@Builder(toBuilder = true)
@Jacksonized
public record Command(

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/command/")
        Boolean enable,

        Boolean suggestInvisiblePlayers,

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/command/afk/")
        Afk afk,

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

) implements EnableSetting {

    @With
    @Builder(toBuilder = true)
    @Jacksonized
    public record Anon(
            Boolean enable,
            Range range,
            List<String> aliases,
            Destination destination,
            Cooldown cooldown,
            Sound sound
    ) implements CommandSetting {}

    @With
    @Builder(toBuilder = true)
    @Jacksonized
    public record Afk(
            Boolean enable,
            List<String> aliases,
            Cooldown cooldown,
            Sound sound
    ) implements CommandSetting {}

    @With
    @Builder(toBuilder = true)
    @Jacksonized
    public record Ball(
            Boolean enable,
            Range range,
            List<String> aliases,
            Destination destination,
            Cooldown cooldown,
            Sound sound
    ) implements CommandSetting {}

    @With
    @Builder(toBuilder = true)
    @Jacksonized
    public record Ban(
            Boolean enable,
            Boolean suggestOfflinePlayers,
            Boolean showConnectionAttempts,
            Boolean checkGroupWeight,
            Range range,
            Map<Integer, Long> timeLimits,
            List<String> aliases,
            Destination destination,
            Cooldown cooldown,
            Sound sound
    ) implements CommandSetting {}

    @With
    @Builder(toBuilder = true)
    @Jacksonized
    public record Banlist(
            Boolean enable,
            Integer perPage,
            List<String> aliases,
            Cooldown cooldown,
            Sound sound
    ) implements CommandSetting {}

    @With
    @Builder(toBuilder = true)
    @Jacksonized
    public record Broadcast(
            Boolean enable,
            Range range,
            List<String> aliases,
            Destination destination,
            Cooldown cooldown,
            Sound sound
    ) implements CommandSetting {}

    @With
    @Builder(toBuilder = true)
    @Jacksonized
    public record Chatcolor(
            Boolean enable,
            List<String> aliases,
            Destination destination,
            Cooldown cooldown,
            Sound sound
    ) implements CommandSetting {}

    @With
    @Builder(toBuilder = true)
    @Jacksonized
    public record Deletemessage(
            Boolean enable,
            List<String> aliases,
            Destination destination,
            Cooldown cooldown,
            Sound sound
    ) implements CommandSetting {}

    @With
    @Builder(toBuilder = true)
    @Jacksonized
    public record Chatsetting(
            Boolean enable,
            List<String> aliases,
            Modern modern,
            Checkbox checkbox,
            Menu menu,
            Cooldown cooldown,
            Sound sound
    ) implements CommandSetting {

        @With
        @Builder(toBuilder = true)
        @Jacksonized
        public record Modern(
                Boolean enable,
                Integer buttonWidth,
                Integer columns
        ) {}

        @With
        @Builder(toBuilder = true)
        @Jacksonized
        public record Checkbox(
                String disabledMaterial,
                String enabledMaterial,
                Map<String, Integer> types
        ) {}

        @With
        @Builder(toBuilder = true)
        @Jacksonized
        public record Menu(
                String material,
                Chat chat,
                Color see,
                Color out
        ) {

            @With
            @Builder(toBuilder = true)
            @Jacksonized
            public record Chat(
                    Integer slot,
                    List<Type> types
            ) {

                @With
                @Builder(toBuilder = true)
                @Jacksonized
                public record Type(
                        String name,
                        String material
                ) {}
            }

            @With
            @Builder(toBuilder = true)
            @Jacksonized
            public record Color(
                    Integer slot,
                    List<Type> types
            ) {

                @With
                @Builder(toBuilder = true)
                @Jacksonized
                public record Type(
                        String name,
                        String material,
                        LinkedHashMap<Integer, String> colors
                ) {}
            }
        }
    }

    @With
    @Builder(toBuilder = true)
    @Jacksonized
    public record Clearchat(
            Boolean enable,
            List<String> aliases,
            Destination destination,
            Cooldown cooldown,
            Sound sound
    ) implements CommandSetting {}

    @With
    @Builder(toBuilder = true)
    @Jacksonized
    public record Clearmail(
            Boolean enable,
            List<String> aliases,
            Destination destination,
            Cooldown cooldown,
            Sound sound
    ) implements CommandSetting {}

    @With
    @Builder(toBuilder = true)
    @Jacksonized
    public record Coin(
            Boolean enable,
            Boolean draw,
            Range range,
            List<String> aliases,
            Destination destination,
            Cooldown cooldown,
            Sound sound
    ) implements CommandSetting {}

    @With
    @Builder(toBuilder = true)
    @Jacksonized
    public record Dice(
            Boolean enable,
            Range range,
            Integer min,
            Integer max,
            List<String> aliases,
            Destination destination,
            Cooldown cooldown,
            Sound sound
    ) implements CommandSetting {}

    @With
    @Builder(toBuilder = true)
    @Jacksonized
    public record CommandDo(
            Boolean enable,
            Range range,
            List<String> aliases,
            Destination destination,
            Cooldown cooldown,
            Sound sound
    ) implements CommandSetting {}

    @With
    @Builder(toBuilder = true)
    @Jacksonized
    public record Emit(
            Boolean enable,
            List<String> aliases,
            Cooldown cooldown,
            Sound sound
    ) implements CommandSetting {}

    @With
    @Builder(toBuilder = true)
    @Jacksonized
    public record Flectonepulse(
            Boolean enable,
            Boolean executeInMainThread,
            List<String> aliases,
            Destination destination,
            Cooldown cooldown,
            Sound sound
    ) implements CommandSetting {}

    @With
    @Builder(toBuilder = true)
    @Jacksonized
    public record Geolocate(
            Boolean enable,
            Boolean suggestOfflinePlayers,
            List<String> aliases,
            Destination destination,
            Cooldown cooldown,
            Sound sound
    ) implements CommandSetting {}

    @With
    @Builder(toBuilder = true)
    @Jacksonized
    public record Helper(
            Boolean enable,
            Boolean nullHelper,
            Range range,
            List<String> aliases,
            Destination destination,
            Cooldown cooldown,
            Sound sound
    ) implements CommandSetting {}

    @With
    @Builder(toBuilder = true)
    @Jacksonized
    public record Ignore(
            Boolean enable,
            Boolean suggestOfflinePlayers,
            List<String> aliases,
            Destination destination,
            Cooldown cooldown,
            Sound sound
    ) implements CommandSetting {}

    @With
    @Builder(toBuilder = true)
    @Jacksonized
    public record Ignorelist(
            Boolean enable,
            Integer perPage,
            List<String> aliases,
            Cooldown cooldown,
            Sound sound
    ) implements CommandSetting {}

    @With
    @Builder(toBuilder = true)
    @Jacksonized
    public record Kick(
            Boolean enable,
            Range range,
            List<String> aliases,
            Destination destination,
            Cooldown cooldown,
            Sound sound
    ) implements CommandSetting {}

    @With
    @Builder(toBuilder = true)
    @Jacksonized
    public record Mail(
            Boolean enable,
            List<String> aliases,
            Destination destination,
            Cooldown cooldown,
            Sound sound
    ) implements CommandSetting {}

    @With
    @Builder(toBuilder = true)
    @Jacksonized
    public record Maintenance(
            Boolean enable,
            Boolean turnedOn,
            List<String> aliases,
            Destination destination,
            Cooldown cooldown,
            Sound sound
    ) implements CommandSetting {}

    @With
    @Builder(toBuilder = true)
    @Jacksonized
    public record Me(
            Boolean enable,
            Range range,
            List<String> aliases,
            Destination destination,
            Cooldown cooldown,
            Sound sound
    ) implements CommandSetting {}

    @With
    @Builder(toBuilder = true)
    @Jacksonized
    public record Mute(
            Boolean enable,
            Boolean suggestOfflinePlayers,
            Boolean checkGroupWeight,
            Range range,
            Map<Integer, Long> timeLimits,
            List<String> aliases,
            Destination destination,
            Cooldown cooldown,
            Sound sound
    ) implements CommandSetting {}

    @With
    @Builder(toBuilder = true)
    @Jacksonized
    public record Mutelist(
            Boolean enable,
            Integer perPage,
            List<String> aliases,
            Cooldown cooldown,
            Sound sound
    ) implements CommandSetting {}

    @With
    @Builder(toBuilder = true)
    @Jacksonized
    public record Online(
            Boolean enable,
            Boolean suggestOfflinePlayers,
            Range range,
            List<String> aliases,
            Destination destination,
            Cooldown cooldown,
            Sound sound
    ) implements CommandSetting {}

    @With
    @Builder(toBuilder = true)
    @Jacksonized
    public record Ping(
            Boolean enable,
            List<String> aliases,
            Destination destination,
            Cooldown cooldown,
            Sound sound
    ) implements CommandSetting {}

    @With
    @Builder(toBuilder = true)
    @Jacksonized
    public record Poll(
            Boolean enable,
            Boolean enableGui,
            Integer lastId,
            Range range,
            List<String> aliases,
            Cooldown cooldown,
            Sound sound
    ) implements CommandSetting {}

    @With
    @Builder(toBuilder = true)
    @Jacksonized
    public record Reply(
            Boolean enable,
            List<String> aliases,
            Cooldown cooldown,
            Sound sound
    ) implements CommandSetting {}

    @With
    @Builder(toBuilder = true)
    @Jacksonized
    public record Rockpaperscissors(
            Boolean enable,
            List<String> aliases,
            Map<String, List<String>> strategies,
            Cooldown cooldown,
            Sound sound
    ) implements CommandSetting {}

    @With
    @Builder(toBuilder = true)
    @Jacksonized
    public record Spy(
            Boolean enable,
            Range range,
            List<String> aliases,
            Map<String, List<String>> categories,
            Destination destination,
            Cooldown cooldown,
            Sound sound
    ) implements CommandSetting {}

    @With
    @Builder(toBuilder = true)
    @Jacksonized
    public record Stream(
            Boolean enable,
            Range range,
            List<String> aliases,
            Destination destination,
            Cooldown cooldown,
            Sound sound
    ) implements CommandSetting {}

    @With
    @Builder(toBuilder = true)
    @Jacksonized
    public record Symbol(
            Boolean enable,
            List<String> aliases,
            Map<String, String> categories,
            Destination destination,
            Cooldown cooldown,
            Sound sound
    ) implements CommandSetting {}

    @With
    @Builder(toBuilder = true)
    @Jacksonized
    public record Tell(
            Boolean enable,
            Boolean suggestOfflinePlayers,
            Range range,
            List<String> aliases,
            Destination destination,
            Cooldown cooldown,
            Sound sound
    ) implements CommandSetting {}

    @With
    @Builder(toBuilder = true)
    @Jacksonized
    public record Tictactoe(
            Boolean enable,
            List<String> aliases,
            Cooldown cooldown,
            Sound sound
    ) implements CommandSetting {}

    @With
    @Builder(toBuilder = true)
    @Jacksonized
    public record Toponline(
            Boolean enable,
            Integer perPage,
            List<String> aliases,
            Cooldown cooldown,
            Sound sound
    ) implements CommandSetting {}

    @With
    @Builder(toBuilder = true)
    @Jacksonized
    public record Translateto(
            Boolean enable,
            Range range,
            Service service,
            List<String> aliases,
            List<String> languages,
            Destination destination,
            Cooldown cooldown,
            Sound sound
    ) implements CommandSetting {

        public enum Service {
            DEEPL,
            GOOGLE,
            YANDEX
        }
    }

    @With
    @Builder(toBuilder = true)
    @Jacksonized
    public record CommandTry(
            Boolean enable,
            Range range,
            Integer min,
            Integer max,
            Integer good,
            List<String> aliases,
            Destination destination,
            Cooldown cooldown,
            Sound sound
    ) implements CommandSetting {}

    @With
    @Builder(toBuilder = true)
    @Jacksonized
    public record Unban(
            Boolean enable,
            Boolean checkGroupWeight,
            Range range,
            List<String> aliases,
            Destination destination,
            Cooldown cooldown,
            Sound sound
    ) implements CommandSetting {}

    @With
    @Builder(toBuilder = true)
    @Jacksonized
    public record Unmute(
            Boolean enable,
            Boolean checkGroupWeight,
            Range range,
            List<String> aliases,
            Destination destination,
            Cooldown cooldown,
            Sound sound
    ) implements CommandSetting {}

    @With
    @Builder(toBuilder = true)
    @Jacksonized
    public record Unwarn(
            Boolean enable,
            Boolean checkGroupWeight,
            Range range,
            List<String> aliases,
            Destination destination,
            Cooldown cooldown,
            Sound sound
    ) implements CommandSetting {}

    @With
    @Builder(toBuilder = true)
    @Jacksonized
    public record Warn(
            Boolean enable,
            Boolean suggestOfflinePlayers,
            Boolean checkGroupWeight,
            Range range,
            Map<Integer, Long> timeLimits,
            List<String> aliases,
            Map<Integer, String> actions,
            Destination destination,
            Cooldown cooldown,
            Sound sound
    ) implements CommandSetting {}

    @With
    @Builder(toBuilder = true)
    @Jacksonized
    public record Warnlist(
            Boolean enable,
            Integer perPage,
            List<String> aliases,
            Cooldown cooldown,
            Sound sound
    ) implements CommandSetting {}
}