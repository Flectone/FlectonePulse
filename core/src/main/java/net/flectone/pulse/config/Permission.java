package net.flectone.pulse.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.Builder;
import lombok.With;
import lombok.extern.jackson.Jacksonized;
import net.flectone.pulse.config.setting.CooldownPermissionSetting;
import net.flectone.pulse.config.setting.PermissionSetting;
import net.flectone.pulse.config.setting.SoundPermissionSetting;
import net.flectone.pulse.util.constant.AdventureTag;

import java.util.Map;

/**
 * Configuration for permissions in FlectonePulse.
 * Contains all permissions and their types.
 *
 * @author TheFaser
 * @since 1.0
 */
@With
@Builder(toBuilder = true)
@Jacksonized
public record Permission(

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/config/module")
        PermissionEntry module,

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/command")
        Command command,

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/integration")
        Integration integration,

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/message")
        Message message

) {

    @With
    @Builder(toBuilder = true)
    @Jacksonized
    public record Command(
            String name,
            Permission.Type type,
            PermissionEntry seeInvisiblePlayersInSuggest,

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

            @JsonPropertyDescription(" https://flectone.net/pulse/docs/command/mute")
            Mute mute,

            @JsonPropertyDescription(" https://flectone.net/pulse/docs/command/mutelist")
            Mutelist mutelist,

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
            Warnlist warnlist
    ) implements PermissionSetting {

        @With
        @Builder(toBuilder = true)
        @Jacksonized
        public record Afk(
                String name,
                Permission.Type type,
                PermissionEntry cooldownBypass,
                PermissionEntry sound
        ) implements CooldownPermissionSetting, SoundPermissionSetting, PermissionSetting {}

        @With
        @Builder(toBuilder = true)
        @Jacksonized
        public record Anon(
                String name,
                Permission.Type type,
                PermissionEntry cooldownBypass,
                PermissionEntry sound
        ) implements CooldownPermissionSetting, SoundPermissionSetting, PermissionSetting {}

        @With
        @Builder(toBuilder = true)
        @Jacksonized
        public record Ball(
                String name,
                Permission.Type type,
                PermissionEntry cooldownBypass,
                PermissionEntry sound
        ) implements CooldownPermissionSetting, SoundPermissionSetting, PermissionSetting {}

        @With
        @Builder(toBuilder = true)
        @Jacksonized
        public record Ban(
                String name,
                Permission.Type type,
                PermissionEntry cooldownBypass,
                PermissionEntry sound
        ) implements CooldownPermissionSetting, SoundPermissionSetting, PermissionSetting {}

        @With
        @Builder(toBuilder = true)
        @Jacksonized
        public record Banlist(
                String name,
                Permission.Type type,
                PermissionEntry cooldownBypass,
                PermissionEntry sound
        ) implements CooldownPermissionSetting, SoundPermissionSetting, PermissionSetting {}

        @With
        @Builder(toBuilder = true)
        @Jacksonized
        public record Broadcast(
                String name,
                Permission.Type type,
                PermissionEntry cooldownBypass,
                PermissionEntry sound
        ) implements CooldownPermissionSetting, SoundPermissionSetting, PermissionSetting {}

        @With
        @Builder(toBuilder = true)
        @Jacksonized
        public record Chatcolor(
                String name,
                Permission.Type type,
                PermissionEntry other,
                PermissionEntry cooldownBypass,
                PermissionEntry sound,
                Map<String, PermissionEntry> colors
        ) implements CooldownPermissionSetting, SoundPermissionSetting, PermissionSetting {}

        @With
        @Builder(toBuilder = true)
        @Jacksonized
        public record Chatsetting(
                String name,
                Permission.Type type,
                Map<String, SettingItem> settings,
                PermissionEntry other,
                PermissionEntry cooldownBypass,
                PermissionEntry sound
        ) implements CooldownPermissionSetting, SoundPermissionSetting, PermissionSetting {

            @With
            @Builder(toBuilder = true)
            @Jacksonized
            public record SettingItem(
                    String name,
                    Permission.Type type
            ) implements PermissionSetting {}
        }

        @With
        @Builder(toBuilder = true)
        @Jacksonized
        public record Clearchat(
                String name,
                Permission.Type type,
                PermissionEntry other,
                PermissionEntry cooldownBypass,
                PermissionEntry sound
        ) implements CooldownPermissionSetting, SoundPermissionSetting, PermissionSetting {}

        @With
        @Builder(toBuilder = true)
        @Jacksonized
        public record Clearmail(
                String name,
                Permission.Type type,
                PermissionEntry cooldownBypass,
                PermissionEntry sound
        ) implements CooldownPermissionSetting, SoundPermissionSetting, PermissionSetting {}

        @With
        @Builder(toBuilder = true)
        @Jacksonized
        public record Coin(
                String name,
                Permission.Type type,
                PermissionEntry cooldownBypass,
                PermissionEntry sound
        ) implements CooldownPermissionSetting, SoundPermissionSetting, PermissionSetting {}

        @With
        @Builder(toBuilder = true)
        @Jacksonized
        public record Deletemessage(
                String name,
                Permission.Type type,
                PermissionEntry cooldownBypass,
                PermissionEntry sound
        ) implements CooldownPermissionSetting, SoundPermissionSetting, PermissionSetting {}

        @With
        @Builder(toBuilder = true)
        @Jacksonized
        public record Dice(
                String name,
                Permission.Type type,
                PermissionEntry cooldownBypass,
                PermissionEntry sound
        ) implements CooldownPermissionSetting, SoundPermissionSetting, PermissionSetting {}

        @With
        @Builder(toBuilder = true)
        @Jacksonized
        public record CommandDo(
                String name,
                Permission.Type type,
                PermissionEntry cooldownBypass,
                PermissionEntry sound
        ) implements CooldownPermissionSetting, SoundPermissionSetting, PermissionSetting {}

        @With
        @Builder(toBuilder = true)
        @Jacksonized
        public record Emit(
                String name,
                Permission.Type type,
                PermissionEntry cooldownBypass,
                PermissionEntry sound
        ) implements CooldownPermissionSetting, SoundPermissionSetting, PermissionSetting {}

        @With
        @Builder(toBuilder = true)
        @Jacksonized
        public record Flectonepulse(
                String name,
                Permission.Type type,
                PermissionEntry cooldownBypass,
                PermissionEntry sound
        ) implements CooldownPermissionSetting, SoundPermissionSetting, PermissionSetting {}

        @With
        @Builder(toBuilder = true)
        @Jacksonized
        public record Geolocate(
                String name,
                Permission.Type type,
                PermissionEntry cooldownBypass,
                PermissionEntry sound
        ) implements CooldownPermissionSetting, SoundPermissionSetting, PermissionSetting {}

        @With
        @Builder(toBuilder = true)
        @Jacksonized
        public record Helper(
                String name,
                Permission.Type type,
                PermissionEntry see,
                PermissionEntry cooldownBypass,
                PermissionEntry sound
        ) implements CooldownPermissionSetting, SoundPermissionSetting, PermissionSetting {}

        @With
        @Builder(toBuilder = true)
        @Jacksonized
        public record Ignore(
                String name,
                Permission.Type type,
                PermissionEntry cooldownBypass,
                PermissionEntry sound
        ) implements CooldownPermissionSetting, SoundPermissionSetting, PermissionSetting {}

        @With
        @Builder(toBuilder = true)
        @Jacksonized
        public record Ignorelist(
                String name,
                Permission.Type type,
                PermissionEntry cooldownBypass,
                PermissionEntry sound
        ) implements CooldownPermissionSetting, SoundPermissionSetting, PermissionSetting {}

        @With
        @Builder(toBuilder = true)
        @Jacksonized
        public record Kick(
                String name,
                Permission.Type type,
                PermissionEntry cooldownBypass,
                PermissionEntry sound
        ) implements CooldownPermissionSetting, SoundPermissionSetting, PermissionSetting {}

        @With
        @Builder(toBuilder = true)
        @Jacksonized
        public record Mail(
                String name,
                Permission.Type type,
                PermissionEntry cooldownBypass,
                PermissionEntry sound
        ) implements CooldownPermissionSetting, SoundPermissionSetting, PermissionSetting {}

        @With
        @Builder(toBuilder = true)
        @Jacksonized
        public record Maintenance(
                String name,
                Permission.Type type,
                PermissionEntry join,
                PermissionEntry cooldownBypass,
                PermissionEntry sound
        ) implements CooldownPermissionSetting, SoundPermissionSetting, PermissionSetting {}

        @With
        @Builder(toBuilder = true)
        @Jacksonized
        public record Me(
                String name,
                Permission.Type type,
                PermissionEntry cooldownBypass,
                PermissionEntry sound
        )implements CooldownPermissionSetting, SoundPermissionSetting, PermissionSetting {}

        @With
        @Builder(toBuilder = true)
        @Jacksonized
        public record Mute(
                String name,
                Permission.Type type,
                PermissionEntry cooldownBypass,
                PermissionEntry sound
        ) implements CooldownPermissionSetting, SoundPermissionSetting, PermissionSetting {}

        @With
        @Builder(toBuilder = true)
        @Jacksonized
        public record Mutelist(
                String name,
                Permission.Type type,
                PermissionEntry cooldownBypass,
                PermissionEntry sound
        ) implements CooldownPermissionSetting, SoundPermissionSetting, PermissionSetting {}

        @With
        @Builder(toBuilder = true)
        @Jacksonized
        public record Online(
                String name,
                Permission.Type type,
                PermissionEntry cooldownBypass,
                PermissionEntry sound
        ) implements CooldownPermissionSetting, SoundPermissionSetting, PermissionSetting {}

        @With
        @Builder(toBuilder = true)
        @Jacksonized
        public record Ping(
                String name,
                Permission.Type type,
                PermissionEntry cooldownBypass,
                PermissionEntry sound
        ) implements CooldownPermissionSetting, SoundPermissionSetting, PermissionSetting {}

        @With
        @Builder(toBuilder = true)
        @Jacksonized
        public record Poll(
                String name,
                Permission.Type type,
                PermissionEntry create,
                PermissionEntry cooldownBypass,
                PermissionEntry sound
        ) implements CooldownPermissionSetting, SoundPermissionSetting, PermissionSetting {}

        @With
        @Builder(toBuilder = true)
        @Jacksonized
        public record Reply(
                String name,
                Permission.Type type,
                PermissionEntry cooldownBypass,
                PermissionEntry sound
        ) implements CooldownPermissionSetting, SoundPermissionSetting, PermissionSetting {}

        @With
        @Builder(toBuilder = true)
        @Jacksonized
        public record Rockpaperscissors(
                String name,
                Permission.Type type,
                PermissionEntry cooldownBypass,
                PermissionEntry sound
        ) implements CooldownPermissionSetting, SoundPermissionSetting, PermissionSetting {}

        @With
        @Builder(toBuilder = true)
        @Jacksonized
        public record Sprite(
                String name,
                Permission.Type type,
                PermissionEntry cooldownBypass,
                PermissionEntry sound
        ) implements CooldownPermissionSetting, SoundPermissionSetting, PermissionSetting {}

        @With
        @Builder(toBuilder = true)
        @Jacksonized
        public record Spy(
                String name,
                Permission.Type type,
                PermissionEntry cooldownBypass,
                PermissionEntry sound
        ) implements CooldownPermissionSetting, SoundPermissionSetting, PermissionSetting {}

        @With
        @Builder(toBuilder = true)
        @Jacksonized
        public record Stream(
                String name,
                Permission.Type type,
                PermissionEntry cooldownBypass,
                PermissionEntry sound
        ) implements CooldownPermissionSetting, SoundPermissionSetting, PermissionSetting {}

        @With
        @Builder(toBuilder = true)
        @Jacksonized
        public record Symbol(
                String name,
                Permission.Type type,
                PermissionEntry cooldownBypass,
                PermissionEntry sound
        ) implements CooldownPermissionSetting, SoundPermissionSetting, PermissionSetting {}

        @With
        @Builder(toBuilder = true)
        @Jacksonized
        public record Tell(
                String name,
                Permission.Type type,
                PermissionEntry cooldownBypass,
                PermissionEntry sound
        ) implements CooldownPermissionSetting, SoundPermissionSetting, PermissionSetting {}

        @With
        @Builder(toBuilder = true)
        @Jacksonized
        public record Tictactoe(
                String name,
                Permission.Type type,
                PermissionEntry cooldownBypass,
                PermissionEntry sound
        ) implements CooldownPermissionSetting, SoundPermissionSetting, PermissionSetting {}

        @With
        @Builder(toBuilder = true)
        @Jacksonized
        public record Toponline(
                String name,
                Permission.Type type,
                PermissionEntry cooldownBypass,
                PermissionEntry sound
        ) implements CooldownPermissionSetting, SoundPermissionSetting, PermissionSetting {}

        @With
        @Builder(toBuilder = true)
        @Jacksonized
        public record Translateto(
                String name,
                Permission.Type type,
                PermissionEntry cooldownBypass,
                PermissionEntry sound
        ) implements CooldownPermissionSetting, SoundPermissionSetting, PermissionSetting {}

        @With
        @Builder(toBuilder = true)
        @Jacksonized
        public record CommandTry(
                String name,
                Permission.Type type,
                PermissionEntry cooldownBypass,
                PermissionEntry sound
        ) implements CooldownPermissionSetting, SoundPermissionSetting, PermissionSetting {}

        @With
        @Builder(toBuilder = true)
        @Jacksonized
        public record Unban(
                String name,
                Permission.Type type,
                PermissionEntry cooldownBypass,
                PermissionEntry sound
        ) implements CooldownPermissionSetting, SoundPermissionSetting, PermissionSetting {}

        @With
        @Builder(toBuilder = true)
        @Jacksonized
        public record Unmute(
                String name,
                Permission.Type type,
                PermissionEntry cooldownBypass,
                PermissionEntry sound
        ) implements CooldownPermissionSetting, SoundPermissionSetting, PermissionSetting {}

        @With
        @Builder(toBuilder = true)
        @Jacksonized
        public record Unwarn(
                String name,
                Permission.Type type,
                PermissionEntry cooldownBypass,
                PermissionEntry sound
        ) implements CooldownPermissionSetting, SoundPermissionSetting, PermissionSetting {}

        @With
        @Builder(toBuilder = true)
        @Jacksonized
        public record Warn(
                String name,
                Permission.Type type,
                PermissionEntry cooldownBypass,
                PermissionEntry sound
        ) implements CooldownPermissionSetting, SoundPermissionSetting, PermissionSetting {}

        @With
        @Builder(toBuilder = true)
        @Jacksonized
        public record Warnlist(
                String name,
                Permission.Type type,
                PermissionEntry cooldownBypass,
                PermissionEntry sound
        ) implements CooldownPermissionSetting, SoundPermissionSetting, PermissionSetting {}
    }

    @With
    @Builder(toBuilder = true)
    @Jacksonized
    public record Integration(
            String name,
            Permission.Type type,

            @JsonPropertyDescription(" https://flectone.net/pulse/docs/integration/advancedban")
            Advancedban advancedban,

            @JsonPropertyDescription(" https://flectone.net/pulse/docs/integration/cmi")
            CMI cmi,

            @JsonPropertyDescription(" https://flectone.net/pulse/docs/integration/deepl")
            Deepl deepl,

            @JsonPropertyDescription(" https://flectone.net/pulse/docs/integration/discord")
            Discord discord,

            @JsonPropertyDescription(" https://flectone.net/pulse/docs/integration/floodgate")
            Floodgate floodgate,

            @JsonPropertyDescription(" https://flectone.net/pulse/docs/integration/geyser")
            Geyser geyser,

            @JsonPropertyDescription(" https://flectone.net/pulse/docs/integration/interactivechat")
            Interactivechat interactivechat,

            @JsonPropertyDescription(" https://flectone.net/pulse/docs/integration/itemsadder")
            Itemsadder itemsadder,

            @JsonPropertyDescription(" https://flectone.net/pulse/docs/integration/libertybans")
            Libertybans libertybans,

            @JsonPropertyDescription(" https://flectone.net/pulse/docs/integration/litebans")
            Litebans litebans,

            @JsonPropertyDescription(" https://flectone.net/pulse/docs/integration/luckperms")
            Luckperms luckperms,

            @JsonPropertyDescription(" https://flectone.net/pulse/docs/integration/maintenance")
            Maintenance maintenance,

            @JsonPropertyDescription(" https://flectone.net/pulse/docs/integration/minimotd")
            MiniMOTD minimotd,

            @JsonPropertyDescription(" https://flectone.net/pulse/docs/integration/miniplaceholders")
            MiniPlaceholders miniplaceholders,

            @JsonPropertyDescription(" https://flectone.net/pulse/docs/integration/motd")
            MOTD motd,

            @JsonPropertyDescription(" https://flectone.net/pulse/docs/integration/placeholderapi")
            Placeholderapi placeholderapi,

            @JsonPropertyDescription(" https://flectone.net/pulse/docs/integration/plasmovoice")
            Plasmovoice plasmovoice,

            @JsonPropertyDescription(" https://flectone.net/pulse/docs/integration/simplevoice")
            Simplevoice simplevoice,

            @JsonPropertyDescription(" https://flectone.net/pulse/docs/integration/skinsrestorer")
            Skinsrestorer skinsrestorer,

            @JsonPropertyDescription(" https://flectone.net/pulse/docs/integration/supervanish")
            Supervanish supervanish,

            @JsonPropertyDescription(" https://flectone.net/pulse/docs/integration/tab")
            Tab tab,

            @JsonPropertyDescription(" https://flectone.net/pulse/docs/integration/telegram")
            Telegram telegram,

            @JsonPropertyDescription(" https://flectone.net/pulse/docs/integration/triton")
            Triton triton,

            @JsonPropertyDescription(" https://flectone.net/pulse/docs/integration/twitch")
            Twitch twitch,

            @JsonPropertyDescription(" https://flectone.net/pulse/docs/integration/vault")
            Vault vault,

            @JsonPropertyDescription(" https://flectone.net/pulse/docs/integration/yandex")
            Yandex yandex
    ) implements PermissionSetting {

        @With
        @Builder(toBuilder = true)
        @Jacksonized
        public record Advancedban(
                String name,
                Permission.Type type
        ) implements PermissionSetting {}

        @With
        @Builder(toBuilder = true)
        @Jacksonized
        public record CMI(
                String name,
                Permission.Type type
        ) implements PermissionSetting {}

        @With
        @Builder(toBuilder = true)
        @Jacksonized
        public record Libertybans(
                String name,
                Permission.Type type
        ) implements PermissionSetting {}

        @With
        @Builder(toBuilder = true)
        @Jacksonized
        public record Deepl(
                String name,
                Permission.Type type
        ) implements PermissionSetting {}

        @With
        @Builder(toBuilder = true)
        @Jacksonized
        public record Discord(
                String name,
                Permission.Type type,
                PermissionEntry sound
        ) implements SoundPermissionSetting, PermissionSetting {}

        @With
        @Builder(toBuilder = true)
        @Jacksonized
        public record Floodgate(
                String name,
                Permission.Type type
        ) implements PermissionSetting {}

        @With
        @Builder(toBuilder = true)
        @Jacksonized
        public record Geyser(
                String name,
                Permission.Type type
        ) implements PermissionSetting {}

        @With
        @Builder(toBuilder = true)
        @Jacksonized
        public record Interactivechat(
                String name,
                Permission.Type type
        ) implements PermissionSetting {}

        @With
        @Builder(toBuilder = true)
        @Jacksonized
        public record Itemsadder(
                String name,
                Permission.Type type
        ) implements PermissionSetting {}

        @With
        @Builder(toBuilder = true)
        @Jacksonized
        public record Litebans(
                String name,
                Permission.Type type
        ) implements PermissionSetting {}

        @With
        @Builder(toBuilder = true)
        @Jacksonized
        public record Luckperms(
                String name,
                Permission.Type type
        ) implements PermissionSetting {}

        @With
        @Builder(toBuilder = true)
        @Jacksonized
        public record Maintenance(
                String name,
                Permission.Type type
        ) implements PermissionSetting {}

        @With
        @Builder(toBuilder = true)
        @Jacksonized
        public record MiniMOTD(
                String name,
                Permission.Type type
        ) implements PermissionSetting {}

        @With
        @Builder(toBuilder = true)
        @Jacksonized
        public record MiniPlaceholders(
                String name,
                Permission.Type type,
                PermissionEntry use
        ) implements PermissionSetting {}

        @With
        @Builder(toBuilder = true)
        @Jacksonized
        public record MOTD(
                String name,
                Permission.Type type
        ) implements PermissionSetting {}

        @With
        @Builder(toBuilder = true)
        @Jacksonized
        public record Placeholderapi(
                String name,
                Permission.Type type,
                PermissionEntry use
        ) implements PermissionSetting {}

        @With
        @Builder(toBuilder = true)
        @Jacksonized
        public record Plasmovoice(
                String name,
                Permission.Type type
        ) implements PermissionSetting {}

        @With
        @Builder(toBuilder = true)
        @Jacksonized
        public record Simplevoice(
                String name,
                Permission.Type type
        ) implements PermissionSetting {}

        @With
        @Builder(toBuilder = true)
        @Jacksonized
        public record Skinsrestorer(
                String name,
                Permission.Type type
        ) implements PermissionSetting {}

        @With
        @Builder(toBuilder = true)
        @Jacksonized
        public record Supervanish(
                String name,
                Permission.Type type
        ) implements PermissionSetting {}

        @With
        @Builder(toBuilder = true)
        @Jacksonized
        public record Tab(
                String name,
                Permission.Type type
        ) implements PermissionSetting {}

        @With
        @Builder(toBuilder = true)
        @Jacksonized
        public record Telegram(
                String name,
                Permission.Type type,
                PermissionEntry sound
        ) implements SoundPermissionSetting, PermissionSetting {}

        @With
        @Builder(toBuilder = true)
        @Jacksonized
        public record Triton(
                String name,
                Permission.Type type
        ) implements PermissionSetting {}

        @With
        @Builder(toBuilder = true)
        @Jacksonized
        public record Twitch(
                String name,
                Permission.Type type,
                PermissionEntry sound
        ) implements SoundPermissionSetting, PermissionSetting {}

        @With
        @Builder(toBuilder = true)
        @Jacksonized
        public record Vault(
                String name,
                Permission.Type type
        ) implements PermissionSetting {}

        @With
        @Builder(toBuilder = true)
        @Jacksonized
        public record Yandex(
                String name,
                Permission.Type type
        ) implements PermissionSetting {}
    }

    @With
    @Builder(toBuilder = true)
    @Jacksonized
    public record Message(
            String name,
            Permission.Type type,

            @JsonPropertyDescription(" https://flectone.net/pulse/docs/message/afk")
            Afk afk,

            @JsonPropertyDescription(" https://flectone.net/pulse/docs/message/anvil")
            Anvil anvil,

            @JsonPropertyDescription(" https://flectone.net/pulse/docs/message/auto")
            Auto auto,

            @JsonPropertyDescription(" https://flectone.net/pulse/docs/message/book")
            Book book,

            @JsonPropertyDescription(" https://flectone.net/pulse/docs/message/bossbar")
            Bossbar bossbar,

            @JsonPropertyDescription(" https://flectone.net/pulse/docs/message/brand")
            Brand brand,

            @JsonPropertyDescription(" https://flectone.net/pulse/docs/message/bubble")
            Bubble bubble,

            @JsonPropertyDescription(" https://flectone.net/pulse/docs/message/chat")
            Chat chat,

            @JsonPropertyDescription(" https://flectone.net/pulse/docs/message/format")
            Format format,

            @JsonPropertyDescription(" https://flectone.net/pulse/docs/message/greeting")
            Greeting greeting,

            @JsonPropertyDescription(" https://flectone.net/pulse/docs/message/join")
            Join join,

            @JsonPropertyDescription(" https://flectone.net/pulse/docs/message/objective")
            Objective objective,

            @JsonPropertyDescription(" https://flectone.net/pulse/docs/message/quit")
            Quit quit,

            @JsonPropertyDescription(" https://flectone.net/pulse/docs/message/rightclick")
            Rightclick rightclick,

            @JsonPropertyDescription(" https://flectone.net/pulse/docs/message/sidebar")
            Sidebar sidebar,

            @JsonPropertyDescription(" https://flectone.net/pulse/docs/message/sign")
            Sign sign,

            @JsonPropertyDescription(" https://flectone.net/pulse/docs/message/status")
            Status status,

            @JsonPropertyDescription(" https://flectone.net/pulse/docs/message/tab")
            Tab tab,

            @JsonPropertyDescription(" https://flectone.net/pulse/docs/message/update")
            Update update,

            @JsonPropertyDescription(" https://flectone.net/pulse/docs/message/vanilla")
            Vanilla vanilla
    ) implements PermissionSetting {

        @With
        @Builder(toBuilder = true)
        @Jacksonized
        public record Afk(
                String name,
                Permission.Type type,
                PermissionEntry sound
        ) implements SoundPermissionSetting, PermissionSetting {}

        @With
        @Builder(toBuilder = true)
        @Jacksonized
        public record Anvil(
                String name,
                Permission.Type type
        ) implements PermissionSetting {}

        @With
        @Builder(toBuilder = true)
        @Jacksonized
        public record Auto(
                String name,
                Permission.Type type,
                Map<String, PermissionEntry> types
        ) implements PermissionSetting {}

        @With
        @Builder(toBuilder = true)
        @Jacksonized
        public record Book(
                String name,
                Permission.Type type
        ) implements PermissionSetting {}

        @With
        @Builder(toBuilder = true)
        @Jacksonized
        public record Bossbar(
                String name,
                Permission.Type type,
                Map<String, PermissionEntry> types
        ) implements PermissionSetting {}

        @With
        @Builder(toBuilder = true)
        @Jacksonized
        public record Brand(
                String name,
                Permission.Type type
        ) implements PermissionSetting {}

        @With
        @Builder(toBuilder = true)
        @Jacksonized
        public record Bubble(
                String name,
                Permission.Type type
        ) implements PermissionSetting {}

        @With
        @Builder(toBuilder = true)
        @Jacksonized
        public record Chat(
                String name,
                Permission.Type type,
                Map<String, Type> types
        ) implements PermissionSetting {

            @With
            @Builder(toBuilder = true)
            @Jacksonized
            public record Type(
                    String name,
                    Permission.Type type,
                    PermissionEntry cooldownBypass,
                    PermissionEntry sound
            ) implements CooldownPermissionSetting, SoundPermissionSetting, PermissionSetting {}
        }

        @With
        @Builder(toBuilder = true)
        @Jacksonized
        public record Format(
                String name,
                Permission.Type type,
                PermissionEntry legacyColors,
                Map<AdventureTag, PermissionEntry> adventureTags,

                @JsonPropertyDescription(" https://flectone.net/pulse/docs/message/format/fcolor")
                FColor fcolor,

                @JsonPropertyDescription(" https://flectone.net/pulse/docs/message/format/fixation")
                Fixation fixation,

                @JsonPropertyDescription(" https://flectone.net/pulse/docs/message/format/image")
                Image image,

                @JsonPropertyDescription(" https://flectone.net/pulse/docs/message/format/mention")
                Mention mention,

                @JsonPropertyDescription(" https://flectone.net/pulse/docs/message/format/moderation")
                Moderation moderation,

                @JsonPropertyDescription(" https://flectone.net/pulse/docs/message/format/names")
                Names names,

                @JsonPropertyDescription(" https://flectone.net/pulse/docs/message/format/object")
                Object object,

                @JsonPropertyDescription(" https://flectone.net/pulse/docs/message/format/questionanswer")
                @JsonProperty("question_answer")
                QuestionAnswer questionAnswer,

                @JsonPropertyDescription(" https://flectone.net/pulse/docs/message/format/replacement")
                Replacement replacement,

                @JsonPropertyDescription(" https://flectone.net/pulse/docs/message/format/scoreboard")
                Scoreboard scoreboard,

                @JsonPropertyDescription(" https://flectone.net/pulse/docs/message/format/spoiler")
                Spoiler spoiler,

                @JsonPropertyDescription(" https://flectone.net/pulse/docs/message/format/translate")
                Translate translate,

                @JsonPropertyDescription(" https://flectone.net/pulse/docs/message/format/world")
                World world
        ) implements PermissionSetting {

            @With
            @Builder(toBuilder = true)
            @Jacksonized
            public record FColor(
                    String name,
                    Permission.Type type,
                    Map<net.flectone.pulse.model.FColor.Type, PermissionEntry> colors
            ) implements PermissionSetting {}

            @With
            @Builder(toBuilder = true)
            @Jacksonized
            public record Fixation(
                    String name,
                    Permission.Type type
            ) implements PermissionSetting {}

            @With
            @Builder(toBuilder = true)
            @Jacksonized
            public record Image(
                    String name,
                    Permission.Type type
            ) implements PermissionSetting {}

            @With
            @Builder(toBuilder = true)
            @Jacksonized
            public record Mention(
                    String name,
                    Permission.Type type,
                    PermissionEntry group,
                    PermissionEntry bypass,
                    PermissionEntry sound
            ) implements SoundPermissionSetting, PermissionSetting {}

            @With
            @Builder(toBuilder = true)
            @Jacksonized
            public record Moderation(
                    String name,
                    Permission.Type type,

                    @JsonPropertyDescription(" https://flectone.net/pulse/docs/message/format/moderation/caps")
                    Caps caps,

                    @JsonPropertyDescription(" https://flectone.net/pulse/docs/message/format/moderation/delete")
                    Delete delete,

                    @JsonPropertyDescription(" https://flectone.net/pulse/docs/message/format/moderation/newbie")
                    Newbie newbie,

                    @JsonPropertyDescription(" https://flectone.net/pulse/docs/message/format/moderation/flood")
                    Flood flood,

                    @JsonPropertyDescription(" https://flectone.net/pulse/docs/message/format/moderation/swear")
                    Swear swear
            ) implements PermissionSetting {

                @With
                @Builder(toBuilder = true)
                @Jacksonized
                public record Caps(
                        String name,
                        Permission.Type type,
                        PermissionEntry bypass
                ) implements PermissionSetting {}

                @With
                @Builder(toBuilder = true)
                @Jacksonized
                public record Delete(
                        String name,
                        Permission.Type type
                ) implements PermissionSetting {}

                @With
                @Builder(toBuilder = true)
                @Jacksonized
                public record Newbie(
                        String name,
                        Permission.Type type,
                        PermissionEntry bypass
                ) implements PermissionSetting {}

                @With
                @Builder(toBuilder = true)
                @Jacksonized
                public record Flood(
                        String name,
                        Permission.Type type,
                        PermissionEntry bypass
                ) implements PermissionSetting {}

                @With
                @Builder(toBuilder = true)
                @Jacksonized
                public record Swear(
                        String name,
                        Permission.Type type,
                        PermissionEntry bypass,
                        PermissionEntry see
                ) implements PermissionSetting {}
            }

            @With
            @Builder(toBuilder = true)
            @Jacksonized
            public record Names(
                    String name,
                    Permission.Type type
            ) implements PermissionSetting {}

            @With
            @Builder(toBuilder = true)
            @Jacksonized
            public record Object(
                    String name,
                    Permission.Type type,
                    PermissionEntry playerHead,
                    PermissionEntry sprite
            ) implements PermissionSetting {}

            @With
            @Builder(toBuilder = true)
            @Jacksonized
            public record Scoreboard(
                    String name,
                    Permission.Type type
            ) implements PermissionSetting {}

            @With
            @Builder(toBuilder = true)
            @Jacksonized
            public record QuestionAnswer(
                    String name,
                    Permission.Type type,
                    Map<String, Question> questions
            ) implements PermissionSetting {

                @With
                @Builder(toBuilder = true)
                @Jacksonized
                public record Question(
                        String name,
                        Permission.Type type,
                        PermissionEntry sound,
                        PermissionEntry cooldownBypass
                ) implements CooldownPermissionSetting, SoundPermissionSetting, PermissionSetting {}
            }

            @With
            @Builder(toBuilder = true)
            @Jacksonized
            public record Replacement(
                    String name,
                    Permission.Type type,
                    Map<String, PermissionEntry> values
            ) implements PermissionSetting {}

            @With
            @Builder(toBuilder = true)
            @Jacksonized
            public record Spoiler(
                    String name,
                    Permission.Type type
            ) implements PermissionSetting {}

            @With
            @Builder(toBuilder = true)
            @Jacksonized
            public record Translate(
                    String name,
                    Permission.Type type
            ) implements PermissionSetting {}

            @With
            @Builder(toBuilder = true)
            @Jacksonized
            public record World(
                    String name,
                    Permission.Type type
            ) implements PermissionSetting {}
        }

        @With
        @Builder(toBuilder = true)
        @Jacksonized
        public record Greeting(
                String name,
                Permission.Type type,
                PermissionEntry sound
        ) implements SoundPermissionSetting, PermissionSetting {}

        @With
        @Builder(toBuilder = true)
        @Jacksonized
        public record Join(
                String name,
                Permission.Type type,
                PermissionEntry sound
        ) implements SoundPermissionSetting, PermissionSetting {}

        @With
        @Builder(toBuilder = true)
        @Jacksonized
        public record Objective(
                String name,
                Permission.Type type,

                @JsonPropertyDescription(" https://flectone.net/pulse/docs/message/objective/belowname")
                Belowname belowname,

                @JsonPropertyDescription(" https://flectone.net/pulse/docs/message/objective/tabname")
                Tabname tabname
        ) implements PermissionSetting {

            @With
            @Builder(toBuilder = true)
            @Jacksonized
            public record Belowname(
                    String name,
                    Permission.Type type
            ) implements PermissionSetting {}

            @With
            @Builder(toBuilder = true)
            @Jacksonized
            public record Tabname(
                    String name,
                    Permission.Type type
            ) implements PermissionSetting {}
        }

        @With
        @Builder(toBuilder = true)
        @Jacksonized
        public record Quit(
                String name,
                Permission.Type type,
                PermissionEntry sound
        ) implements SoundPermissionSetting, PermissionSetting {}

        @With
        @Builder(toBuilder = true)
        @Jacksonized
        public record Rightclick(
                String name,
                Permission.Type type,
                PermissionEntry cooldownBypass,
                PermissionEntry sound
        ) implements CooldownPermissionSetting, SoundPermissionSetting, PermissionSetting {}

        @With
        @Builder(toBuilder = true)
        @Jacksonized
        public record Sidebar(
                String name,
                Permission.Type type
        ) implements PermissionSetting {}

        @With
        @Builder(toBuilder = true)
        @Jacksonized
        public record Sign(
                String name,
                Permission.Type type
        ) implements PermissionSetting {}

        @With
        @Builder(toBuilder = true)
        @Jacksonized
        public record Status(
                String name,
                Permission.Type type,

                @JsonPropertyDescription(" https://flectone.net/pulse/docs/message/status/icon")
                Icon icon,

                @JsonPropertyDescription(" https://flectone.net/pulse/docs/message/status/motd")
                MOTD motd,

                @JsonPropertyDescription(" https://flectone.net/pulse/docs/message/status/players")
                Players players,

                @JsonPropertyDescription(" https://flectone.net/pulse/docs/message/status/version")
                Version version
        ) implements PermissionSetting {

            @With
            @Builder(toBuilder = true)
            @Jacksonized
            public record MOTD(
                    String name,
                    Permission.Type type
            ) implements PermissionSetting {}

            @With
            @Builder(toBuilder = true)
            @Jacksonized
            public record Icon(
                    String name,
                    Permission.Type type
            ) implements PermissionSetting {}

            @With
            @Builder(toBuilder = true)
            @Jacksonized
            public record Players(
                    String name,
                    Permission.Type type,
                    PermissionEntry bypass
            ) implements PermissionSetting {}

            @With
            @Builder(toBuilder = true)
            @Jacksonized
            public record Version(
                    String name,
                    Permission.Type type
            ) implements PermissionSetting {}
        }

        @With
        @Builder(toBuilder = true)
        @Jacksonized
        public record Tab(
                String name,
                Permission.Type type,

                @JsonPropertyDescription(" https://flectone.net/pulse/docs/message/tab/footer")
                Footer footer,

                @JsonPropertyDescription(" https://flectone.net/pulse/docs/message/tab/header")
                Header header,

                @JsonPropertyDescription(" https://flectone.net/pulse/docs/message/tab/playerlistname")
                Playerlistname playerlistname
        ) implements PermissionSetting {

            @With
            @Builder(toBuilder = true)
            @Jacksonized
            public record Footer(
                    String name,
                    Permission.Type type
            ) implements PermissionSetting {}

            @With
            @Builder(toBuilder = true)
            @Jacksonized
            public record Header(
                    String name,
                    Permission.Type type
            ) implements PermissionSetting {}

            @With
            @Builder(toBuilder = true)
            @Jacksonized
            public record Playerlistname(
                    String name,
                    Permission.Type type
            ) implements PermissionSetting {}
        }

        @With
        @Builder(toBuilder = true)
        @Jacksonized
        public record Update(
                String name,
                Permission.Type type,
                PermissionEntry sound
        ) implements SoundPermissionSetting, PermissionSetting {}

        @With
        @Builder(toBuilder = true)
        @Jacksonized
        public record Vanilla(
                String name,
                Permission.Type type
        ) implements PermissionSetting {}
    }

    @With
    @Builder(toBuilder = true)
    @Jacksonized
    public record PermissionEntry(
            String name,
            Permission.Type type
    ) implements PermissionSetting {
    }

    public enum Type {
        TRUE,
        FALSE,
        OP,
        NOT_OP
    }
}