package net.flectone.pulse.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.Builder;
import lombok.With;
import net.flectone.pulse.config.setting.CommandSetting;
import net.flectone.pulse.config.setting.CooldownConfigSetting;
import net.flectone.pulse.config.setting.EnableSetting;
import net.flectone.pulse.config.setting.SoundConfigSetting;
import net.flectone.pulse.model.value.Cooldown;
import net.flectone.pulse.model.value.Destination;
import net.flectone.pulse.model.value.Range;
import net.flectone.pulse.model.value.Sound;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Configuration for all commands in FlectonePulse.
 * Contains nested record classes for individual command configurations.
 *
 * @author TheFaser
 * @since 1.7.1
 */
@With
@Builder(toBuilder = true)
public record Command(

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/command")
        Boolean enable,

        Boolean suggestInvisiblePlayers,

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/command/afk")
        Afk afk,

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/command/anon")
        Anon anon,

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/command/ball")
        Ball ball,

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/command/ban")
        Ban ban,

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/command/banlist")
        Banlist banlist,

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/command/broadcast")
        Broadcast broadcast,

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/command/chatcolor")
        Chatcolor chatcolor,

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/command/chatsetting")
        Chatsetting chatsetting,

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/command/clearchat")
        Clearchat clearchat,

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/command/clearmail")
        Clearmail clearmail,

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/command/coin")
        Coin coin,

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/command/deletemessage")
        Deletemessage deletemessage,

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/command/dice")
        Dice dice,

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/command/do")
        @JsonProperty("do")
        CommandDo commandDo,

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/command/emit")
        Emit emit,

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/command/flectonepulse")
        Flectonepulse flectonepulse,

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/command/geolocate")
        Geolocate geolocate,

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/command/helper")
        Helper helper,

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/command/ignore")
        Ignore ignore,

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/command/ignorelist")
        Ignorelist ignorelist,

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/command/kick")
        Kick kick,

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/command/mail")
        Mail mail,

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/command/maintenance")
        Maintenance maintenance,

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/command/me")
        Me me,

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/command/minesweeper")
        Minesweeper minesweeper,

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/command/mute")
        Mute mute,

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/command/mutelist")
        Mutelist mutelist,

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/command/nickname")
        Nickname nickname,

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/command/online")
        Online online,

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/command/ping")
        Ping ping,

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/command/poll")
        Poll poll,

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/command/reply")
        Reply reply,

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/command/rockpaperscissors")
        Rockpaperscissors rockpaperscissors,

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/command/sprite")
        Sprite sprite,

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/command/spy")
        Spy spy,

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/command/stream")
        Stream stream,

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/command/symbol")
        Symbol symbol,

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/command/tell")
        Tell tell,

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/command/tictactoe")
        Tictactoe tictactoe,

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/command/toponline")
        Toponline toponline,

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/command/translateto")
        Translateto translateto,

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/command/try")
        @JsonProperty("try")
        CommandTry commandTry,

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/command/unban")
        Unban unban,

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/command/unmute")
        Unmute unmute,

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/command/unwarn")
        Unwarn unwarn,

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/command/warn")
        Warn warn,

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/command/warnlist")
        Warnlist warnlist,

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/command/whitelist")
        Whitelist whitelist,

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/command/whois")
        Whois whois

) implements EnableSetting {

    @With
    @Builder(toBuilder = true)
    public record Anon(
            Boolean enable,
            Range range,
            List<String> aliases,
            Destination destination,
            Cooldown cooldown,
            Sound sound
    ) implements CommandSetting, CooldownConfigSetting, SoundConfigSetting {
    }

    @With
    @Builder(toBuilder = true)
    public record Afk(
            Boolean enable,
            List<String> aliases,
            Cooldown cooldown,
            Sound sound
    ) implements CommandSetting, CooldownConfigSetting, SoundConfigSetting {
    }

    @With
    @Builder(toBuilder = true)
    public record Ball(
            Boolean enable,
            Range range,
            List<String> aliases,
            Destination destination,
            Cooldown cooldown,
            Sound sound
    ) implements CommandSetting, CooldownConfigSetting, SoundConfigSetting {
    }

    @With
    @Builder(toBuilder = true)
    public record Ban(
            Boolean enable,
            Boolean filterByServer,
            Boolean suggestOfflinePlayers,
            Boolean showConnectionAttempts,
            Boolean checkGroupWeight,
            Boolean checkDuplicate,
            Range range,
            Map<Integer, Long> timeLimits,
            ReasonTimeMap reasonTimes,
            List<String> aliases,
            Destination destination,
            Cooldown cooldown,
            Sound sound
    ) implements CommandSetting, CooldownConfigSetting, SoundConfigSetting {
    }

    @With
    @Builder(toBuilder = true)
    public record Banlist(
            Boolean enable,
            Integer perPage,
            List<String> aliases,
            Cooldown cooldown,
            Sound sound
    ) implements CommandSetting, CooldownConfigSetting, SoundConfigSetting {
    }

    @With
    @Builder(toBuilder = true)
    public record Broadcast(
            Boolean enable,
            Range range,
            List<String> aliases,
            Destination destination,
            Cooldown cooldown,
            Sound sound
    ) implements CommandSetting, CooldownConfigSetting, SoundConfigSetting {
    }

    @With
    @Builder(toBuilder = true)
    public record Chatcolor(
            Boolean enable,
            List<String> aliases,
            Destination destination,
            Cooldown cooldown,
            Sound sound
    ) implements CommandSetting, CooldownConfigSetting, SoundConfigSetting {
    }

    @With
    @Builder(toBuilder = true)
    public record Deletemessage(
            Boolean enable,
            List<String> aliases,
            Destination destination,
            Cooldown cooldown,
            Sound sound
    ) implements CommandSetting, CooldownConfigSetting, SoundConfigSetting {
    }

    @With
    @Builder(toBuilder = true)
    public record Chatsetting(
            Boolean enable,
            List<String> aliases,
            Modern modern,
            Checkbox checkbox,
            Menu menu,
            Cooldown cooldown,
            Sound sound
    ) implements CommandSetting, CooldownConfigSetting, SoundConfigSetting {

        @With
        @Builder(toBuilder = true)
        public record Modern(
                Boolean enable,
                Integer panelWidth,
                Integer panelHeight,
                Integer buttonWidth,
                Integer buttonHeight,
                Integer buttonPadding,
                Integer columns
        ) {
        }

        @With
        @Builder(toBuilder = true)
        public record Checkbox(
                String disabledMaterial,
                String enabledMaterial,
                Map<String, Integer> types
        ) {
        }

        @With
        @Builder(toBuilder = true)
        public record Menu(
                String material,
                Chat chat,
                Color see,
                Color out
        ) {

            @With
            @Builder(toBuilder = true)
            public record Chat(
                    Integer slot,
                    List<Type> types
            ) {

                @With
                @Builder(toBuilder = true)
                public record Type(
                        String name,
                        String material
                ) {
                }
            }

            @With
            @Builder(toBuilder = true)
            public record Color(
                    Integer slot,
                    List<Type> types
            ) {

                @With
                @Builder(toBuilder = true)
                public record Type(
                        String name,
                        String material,
                        LinkedHashMap<Integer, String> colors
                ) {
                }
            }
        }
    }

    @With
    @Builder(toBuilder = true)
    public record Clearchat(
            Boolean enable,
            Integer length,
            List<String> aliases,
            Destination destination,
            Cooldown cooldown,
            Sound sound
    ) implements CommandSetting, CooldownConfigSetting, SoundConfigSetting {
    }

    @With
    @Builder(toBuilder = true)
    public record Clearmail(
            Boolean enable,
            List<String> aliases,
            Destination destination,
            Cooldown cooldown,
            Sound sound
    ) implements CommandSetting, CooldownConfigSetting, SoundConfigSetting {
    }

    @With
    @Builder(toBuilder = true)
    public record Coin(
            Boolean enable,
            Boolean draw,
            Range range,
            List<String> aliases,
            Destination destination,
            Cooldown cooldown,
            Sound sound
    ) implements CommandSetting, CooldownConfigSetting, SoundConfigSetting {
    }

    @With
    @Builder(toBuilder = true)
    public record Dice(
            Boolean enable,
            Range range,
            Integer min,
            Integer max,
            List<String> aliases,
            Destination destination,
            Cooldown cooldown,
            Sound sound
    ) implements CommandSetting, CooldownConfigSetting, SoundConfigSetting {
    }

    @With
    @Builder(toBuilder = true)
    public record CommandDo(
            Boolean enable,
            Range range,
            List<String> aliases,
            Destination destination,
            Cooldown cooldown,
            Sound sound
    ) implements CommandSetting, CooldownConfigSetting, SoundConfigSetting {
    }

    @With
    @Builder(toBuilder = true)
    public record Emit(
            Boolean enable,
            List<String> aliases,
            Cooldown cooldown,
            Sound sound
    ) implements CommandSetting, CooldownConfigSetting, SoundConfigSetting {
    }

    @With
    @Builder(toBuilder = true)
    public record Flectonepulse(
            Boolean enable,
            Boolean executeInMainThread,
            List<String> aliases,
            Editor editor,
            Destination destination,
            Cooldown cooldown,
            Sound sound
    ) implements CommandSetting, CooldownConfigSetting, SoundConfigSetting {

        @With
        @Builder(toBuilder = true)
        public record Editor(
                String host,
                Boolean https,
                Integer port
        ) {
        }

    }

    @With
    @Builder(toBuilder = true)
    public record Geolocate(
            Boolean enable,
            Boolean suggestOfflinePlayers,
            List<String> aliases,
            Destination destination,
            Cooldown cooldown,
            Sound sound
    ) implements CommandSetting, CooldownConfigSetting, SoundConfigSetting {
    }

    @With
    @Builder(toBuilder = true)
    public record Helper(
            Boolean enable,
            Boolean nullHelper,
            Range range,
            List<String> aliases,
            Destination destination,
            Cooldown cooldown,
            Sound sound
    ) implements CommandSetting, CooldownConfigSetting, SoundConfigSetting {
    }

    @With
    @Builder(toBuilder = true)
    public record Ignore(
            Boolean enable,
            Boolean suggestOfflinePlayers,
            List<String> aliases,
            Destination destination,
            Cooldown cooldown,
            Sound sound
    ) implements CommandSetting, CooldownConfigSetting, SoundConfigSetting {
    }

    @With
    @Builder(toBuilder = true)
    public record Ignorelist(
            Boolean enable,
            Integer perPage,
            List<String> aliases,
            Cooldown cooldown,
            Sound sound
    ) implements CommandSetting, CooldownConfigSetting, SoundConfigSetting {
    }

    @With
    @Builder(toBuilder = true)
    public record Kick(
            Boolean enable,
            Boolean filterByServer,
            Boolean checkGroupWeight,
            Range range,
            List<String> aliases,
            Destination destination,
            Cooldown cooldown,
            Sound sound
    ) implements CommandSetting, CooldownConfigSetting, SoundConfigSetting {
    }

    @With
    @Builder(toBuilder = true)
    public record Mail(
            Boolean enable,
            List<String> aliases,
            Destination destination,
            Cooldown cooldown,
            Sound sound
    ) implements CommandSetting, CooldownConfigSetting, SoundConfigSetting {
    }

    @With
    @Builder(toBuilder = true)
    public record Maintenance(
            Boolean enable,
            Boolean filterByServer,
            Range range,
            ReasonTimeMap reasonTimes,
            List<String> aliases,
            Destination destination,
            Cooldown cooldown,
            Sound sound
    ) implements CommandSetting, CooldownConfigSetting, SoundConfigSetting {
    }

    @With
    @Builder(toBuilder = true)
    public record Me(
            Boolean enable,
            Range range,
            List<String> aliases,
            Destination destination,
            Cooldown cooldown,
            Sound sound
    ) implements CommandSetting, CooldownConfigSetting, SoundConfigSetting {
    }

    @With
    @Builder(toBuilder = true)
    public record Minesweeper(
            Boolean enable,
            Boolean checkDuplicate,
            Integer maxRow,
            Integer maxColumn,
            Integer defaultMine,
            List<String> aliases,
            Cooldown cooldown,
            Sound sound
    ) implements CommandSetting, CooldownConfigSetting, SoundConfigSetting {
    }

    @With
    @Builder(toBuilder = true)
    public record Mute(
            Boolean enable,
            Boolean filterByServer,
            Boolean suggestOfflinePlayers,
            Boolean checkGroupWeight,
            Boolean checkDuplicate,
            Range range,
            Map<Integer, Long> timeLimits,
            ReasonTimeMap reasonTimes,
            List<String> aliases,
            Destination destination,
            Cooldown cooldown,
            Sound sound
    ) implements CommandSetting, CooldownConfigSetting, SoundConfigSetting {
    }

    @With
    @Builder(toBuilder = true)
    public record Mutelist(
            Boolean enable,
            Integer perPage,
            List<String> aliases,
            Cooldown cooldown,
            Sound sound
    ) implements CommandSetting, CooldownConfigSetting, SoundConfigSetting {
    }

    @With
    @Builder(toBuilder = true)
    public record Nickname(
            Boolean enable,
            String allowedInput,
            String subCommandOther,
            List<String> aliases,
            Destination destination,
            Cooldown cooldown,
            Sound sound
    ) implements CommandSetting, CooldownConfigSetting, SoundConfigSetting {
    }

    @With
    @Builder(toBuilder = true)
    public record Online(
            Boolean enable,
            Boolean suggestOfflinePlayers,
            Range range,
            List<String> aliases,
            Destination destination,
            Cooldown cooldown,
            Sound sound
    ) implements CommandSetting, CooldownConfigSetting, SoundConfigSetting {
    }

    @With
    @Builder(toBuilder = true)
    public record Ping(
            Boolean enable,
            List<String> aliases,
            Destination destination,
            Cooldown cooldown,
            Sound sound
    ) implements CommandSetting, CooldownConfigSetting, SoundConfigSetting {
    }

    @With
    @Builder(toBuilder = true)
    public record Poll(
            Boolean enable,
            Boolean enableGui,
            Integer lastId,
            Range range,
            String subCommandVote,
            String subCommandGui,
            List<String> aliases,
            Cooldown cooldown,
            Sound sound
    ) implements CommandSetting, CooldownConfigSetting, SoundConfigSetting {
    }

    @With
    @Builder(toBuilder = true)
    public record Reply(
            Boolean enable,
            List<String> aliases,
            Cooldown cooldown,
            Sound sound
    ) implements CommandSetting, CooldownConfigSetting, SoundConfigSetting {
    }

    @With
    @Builder(toBuilder = true)
    public record Rockpaperscissors(
            Boolean enable,
            List<String> aliases,
            Map<String, List<String>> strategies,
            Cooldown cooldown,
            Sound sound
    ) implements CommandSetting, CooldownConfigSetting, SoundConfigSetting {
    }

    @With
    @Builder(toBuilder = true)
    public record Sprite(
            Boolean enable,
            Integer perPage,
            List<String> aliases,
            List<String> categories,
            Cooldown cooldown,
            Sound sound
    ) implements CommandSetting, CooldownConfigSetting, SoundConfigSetting {
    }

    @With
    @Builder(toBuilder = true)
    public record Spy(
            Boolean enable,
            Range range,
            List<String> aliases,
            Map<String, List<String>> categories,
            Destination destination,
            Cooldown cooldown,
            Sound sound
    ) implements CommandSetting, CooldownConfigSetting, SoundConfigSetting {
    }

    @With
    @Builder(toBuilder = true)
    public record Stream(
            Boolean enable,
            Range range,
            List<String> aliases,
            Destination destination,
            Cooldown cooldown,
            Sound sound
    ) implements CommandSetting, CooldownConfigSetting, SoundConfigSetting {
    }

    @With
    @Builder(toBuilder = true)
    public record Symbol(
            Boolean enable,
            Integer perPage,
            List<String> aliases,
            Map<String, String> categories,
            Destination destination,
            Cooldown cooldown,
            Sound sound
    ) implements CommandSetting, CooldownConfigSetting, SoundConfigSetting {
    }

    @With
    @Builder(toBuilder = true)
    public record Tell(
            Boolean enable,
            Boolean suggestOfflinePlayers,
            Range range,
            List<String> aliases,
            Destination destination,
            Cooldown cooldown,
            Sound sound
    ) implements CommandSetting, CooldownConfigSetting, SoundConfigSetting {
    }

    @With
    @Builder(toBuilder = true)
    public record Tictactoe(
            Boolean enable,
            String subCommandMove,
            List<String> aliases,
            Cooldown cooldown,
            Sound sound
    ) implements CommandSetting, CooldownConfigSetting, SoundConfigSetting {
    }

    @With
    @Builder(toBuilder = true)
    public record Toponline(
            Boolean enable,
            Integer perPage,
            List<String> aliases,
            Cooldown cooldown,
            Sound sound
    ) implements CommandSetting, CooldownConfigSetting, SoundConfigSetting {
    }

    @With
    @Builder(toBuilder = true)
    public record Translateto(
            Boolean enable,
            Range range,
            Service service,
            List<String> aliases,
            List<String> languages,
            Destination destination,
            Cooldown cooldown,
            Sound sound
    ) implements CommandSetting, CooldownConfigSetting, SoundConfigSetting {

        public enum Service {
            DEEPL,
            GOOGLE,
            YANDEX
        }
    }

    @With
    @Builder(toBuilder = true)
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
    ) implements CommandSetting, CooldownConfigSetting, SoundConfigSetting {
    }

    @With
    @Builder(toBuilder = true)
    public record Unban(
            Boolean enable,
            Boolean checkGroupWeight,
            Range range,
            List<String> aliases,
            Destination destination,
            Cooldown cooldown,
            Sound sound
    ) implements CommandSetting, CooldownConfigSetting, SoundConfigSetting {
    }

    @With
    @Builder(toBuilder = true)
    public record Unmute(
            Boolean enable,
            Boolean checkGroupWeight,
            Range range,
            List<String> aliases,
            Destination destination,
            Cooldown cooldown,
            Sound sound
    ) implements CommandSetting, CooldownConfigSetting, SoundConfigSetting {
    }

    @With
    @Builder(toBuilder = true)
    public record Unwarn(
            Boolean enable,
            Boolean checkGroupWeight,
            Range range,
            List<String> aliases,
            Destination destination,
            Cooldown cooldown,
            Sound sound
    ) implements CommandSetting, CooldownConfigSetting, SoundConfigSetting {
    }

    @With
    @Builder(toBuilder = true)
    public record Warn(
            Boolean enable,
            Boolean filterByServer,
            Boolean suggestOfflinePlayers,
            Boolean checkGroupWeight,
            Range range,
            Map<Integer, Long> timeLimits,
            ReasonTimeMap reasonTimes,
            List<String> aliases,
            Map<Integer, String> actions,
            Destination destination,
            Cooldown cooldown,
            Sound sound
    ) implements CommandSetting, CooldownConfigSetting, SoundConfigSetting {
    }

    @With
    @Builder(toBuilder = true)
    public record Warnlist(
            Boolean enable,
            Integer perPage,
            List<String> aliases,
            Cooldown cooldown,
            Sound sound
    ) implements CommandSetting, CooldownConfigSetting, SoundConfigSetting {
    }

    @With
    @Builder(toBuilder = true)
    public record Whitelist(
            Boolean enable,
            Boolean autoAdd,
            Long autoAddDuration,
            Boolean checkDuplicate,
            Boolean filterByServer,
            Boolean showConnectionAttempts,
            Integer perPage,
            Range range,
            String subCommandPlayer,
            ReasonTimeMap reasonTimes,
            List<String> aliases,
            Destination destination,
            Cooldown cooldown,
            Sound sound
    ) implements CommandSetting, CooldownConfigSetting, SoundConfigSetting {
    }

    @With
    @Builder(toBuilder = true)
    public record Whois(
            Boolean enable,
            Integer perPage,
            List<String> aliases,
            Cooldown cooldown,
            Sound sound
    ) implements CommandSetting, CooldownConfigSetting, SoundConfigSetting {
    }

    public static class ReasonTimeMap extends LinkedHashMap<String, String> {

        public ReasonTimeMap() {
            super(new LinkedHashMap<>());
        }

        public ReasonTimeMap(Map<String, String> map) {
            super(map);
        }

        public String getTime(String reason) {
            if (reason == null || reason.isEmpty()) {
                return super.get("default");
            }

            return super.getOrDefault(reason, super.get("default"));
        }

    }

}