package net.flectone.pulse.config;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.Builder;
import lombok.With;
import lombok.extern.jackson.Jacksonized;
import net.flectone.pulse.config.setting.EnableSetting;
import net.flectone.pulse.config.setting.SoundConfigSetting;
import net.flectone.pulse.model.util.Destination;
import net.flectone.pulse.model.util.Sound;
import net.flectone.pulse.model.util.Ticker;

import java.util.List;
import java.util.Map;

/**
 * Configuration for third-party integrations in FlectonePulse.
 * Contains settings for various external services and plugins.
 *
 * @author TheFaser
 * @since 1.7.1
 */
@With
@Builder(toBuilder = true)
@Jacksonized
public record Integration(

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/integration/")
        Boolean enable,

        String avatarApiUrl,
        String bodyApiUrl,

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/integration/advancedban/")
        Advancedban advancedban,

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/integration/deepl/")
        Deepl deepl,

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/integration/discord/")
        Discord discord,

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/integration/floodgate/")
        Floodgate floodgate,

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/integration/geyser/")
        Geyser geyser,

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/integration/interactivechat/")
        Interactivechat interactivechat,

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/integration/itemsadder/")
        Itemsadder itemsadder,

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/integration/libertybans/")
        Libertybans libertybans,

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/integration/litebans/")
        Litebans litebans,

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/integration/luckperms/")
        Luckperms luckperms,

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/integration/maintenance/")
        Maintenance maintenance,

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/integration/minimotd/")
        MiniMOTD minimotd,

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/integration/miniplaceholders/")
        MiniPlaceholders miniplaceholders,

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/integration/motd/")
        MOTD motd,

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/integration/placeholderapi/")
        Placeholderapi placeholderapi,

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/integration/plasmovoice/")
        Plasmovoice plasmovoice,

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/integration/simplevoice/")
        Simplevoice simplevoice,

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/integration/skinsrestorer/")
        Skinsrestorer skinsrestorer,

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/integration/supervanish/")
        Supervanish supervanish,

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/integration/tab/")
        Tab tab,

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/integration/telegram/")
        Telegram telegram,

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/integration/triton/")
        Triton triton,

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/integration/twitch/")
        Twitch twitch,

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/integration/vault/")
        Vault vault,

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/integration/yandex/")
        Yandex yandex

) implements EnableSetting {

    @With
    @Builder(toBuilder = true)
    @Jacksonized
    public record Advancedban(
            Boolean enable,
            Boolean disableFlectonepulseBan,
            Boolean disableFlectonepulseMute,
            Boolean disableFlectonepulseWarn,
            Boolean disableFlectonepulseKick
    ) implements EnableSetting {
    }

    @With
    @Builder(toBuilder = true)
    @Jacksonized
    public record Libertybans(
            Boolean enable,
            Boolean disableFlectonepulseBan,
            Boolean disableFlectonepulseMute,
            Boolean disableFlectonepulseWarn,
            Boolean disableFlectonepulseKick
    ) implements EnableSetting {
    }

    @With
    @Builder(toBuilder = true)
    @Jacksonized
    public record Deepl(
            Boolean enable,
            String authKey
    ) implements EnableSetting {
    }

    @With
    @Builder(toBuilder = true)
    @Jacksonized
    public record Discord(
            Boolean enable,
            String token,
            Map<String, Command> customCommand,
            Presence presence,
            ChannelInfo channelInfo,
            Map<String, List<String>> messageChannel,
            Destination destination,
            Sound sound
    ) implements SoundConfigSetting, EnableSetting {

        @With
        @Builder(toBuilder = true)
        @Jacksonized
        public record Presence(
                Boolean enable,
                String status,
                Activity activity
        ) {
            @With
            @Builder(toBuilder = true)
            @Jacksonized
            public record Activity(
                    Boolean enable,
                    String type,
                    String name,
                    String url
            ) {
            }
        }
    }

    @With
    @Builder(toBuilder = true)
    @Jacksonized
    public record ChannelInfo(Boolean enable, Ticker ticker) {
    }

    @With
    @Builder(toBuilder = true)
    @Jacksonized
    public record Floodgate(Boolean enable) implements EnableSetting {
    }

    @With
    @Builder(toBuilder = true)
    @Jacksonized
    public record Geyser(Boolean enable) implements EnableSetting {
    }

    @With
    @Builder(toBuilder = true)
    @Jacksonized
    public record Interactivechat(Boolean enable) implements EnableSetting {
    }

    @With
    @Builder(toBuilder = true)
    @Jacksonized
    public record Itemsadder(Boolean enable) implements EnableSetting {
    }

    @With
    @Builder(toBuilder = true)
    @Jacksonized
    public record Litebans(
            Boolean enable,
            Boolean disableFlectonepulseBan,
            Boolean disableFlectonepulseMute,
            Boolean disableFlectonepulseWarn,
            Boolean disableFlectonepulseKick
    ) implements EnableSetting {
    }

    @With
    @Builder(toBuilder = true)
    @Jacksonized
    public record Luckperms(Boolean enable, Boolean tabSort) implements EnableSetting {
    }

    @With
    @Builder(toBuilder = true)
    @Jacksonized
    public record Maintenance(Boolean enable, Boolean disableFlectonepulseMaintenance) implements EnableSetting {
    }

    @With
    @Builder(toBuilder = true)
    @Jacksonized
    public record MiniMOTD(
            Boolean enable,
            Boolean disableFlectonepulseStatus
    ) implements EnableSetting {
    }

    @With
    @Builder(toBuilder = true)
    @Jacksonized
    public record MiniPlaceholders(Boolean enable) implements EnableSetting {
    }

    @With
    @Builder(toBuilder = true)
    @Jacksonized
    public record MOTD(Boolean enable, Boolean disableFlectonepulseStatus) implements EnableSetting {
    }

    @With
    @Builder(toBuilder = true)
    @Jacksonized
    public record Placeholderapi(Boolean enable) implements EnableSetting {
    }

    @With
    @Builder(toBuilder = true)
    @Jacksonized
    public record Plasmovoice(Boolean enable) implements EnableSetting {
    }

    @With
    @Builder(toBuilder = true)
    @Jacksonized
    public record Simplevoice(Boolean enable) implements EnableSetting {
    }

    @With
    @Builder(toBuilder = true)
    @Jacksonized
    public record Skinsrestorer(Boolean enable) implements EnableSetting {
    }

    @With
    @Builder(toBuilder = true)
    @Jacksonized
    public record Supervanish(Boolean enable) implements EnableSetting {
    }

    @With
    @Builder(toBuilder = true)
    @Jacksonized
    public record Tab(
            Boolean enable,
            Boolean disableFlectonepulseScoreboard,
            Boolean disableFlectonepulseHeader,
            Boolean disableFlectonepulseFooter,
            Boolean disableFlectonepulsePlayerlistname
    ) implements EnableSetting {
    }

    @With
    @Builder(toBuilder = true)
    @Jacksonized
    public record Telegram(
            Boolean enable,
            Mode parseMode,
            String token,
            Map<String, Command> customCommand,
            ChannelInfo channelInfo,
            Map<String, List<String>> messageChannel,
            Destination destination,
            Sound sound
    ) implements SoundConfigSetting, EnableSetting {
        public enum Mode {
            MARKDOWN,
            MARKDOWN_V2,
            HTML,
            NONE
        }
    }

    @With
    @Builder(toBuilder = true)
    @Jacksonized
    public record Triton(Boolean enable) implements EnableSetting {
    }

    @With
    @Builder(toBuilder = true)
    @Jacksonized
    public record Twitch(
            Boolean enable,
            String clientID,
            String token,
            Map<String, Command> customCommand,
            Map<String, List<String>> messageChannel,
            Map<String, List<String>> followChannel,
            Destination destination,
            Sound sound
    ) implements SoundConfigSetting, EnableSetting {
    }

    @With
    @Builder(toBuilder = true)
    @Jacksonized
    public record Vault(Boolean enable) implements EnableSetting {
    }

    @With
    @Builder(toBuilder = true)
    @Jacksonized
    public record Yandex(Boolean enable, String token, String folderId) implements EnableSetting {
    }

    @With
    @Builder(toBuilder = true)
    @Jacksonized
    public record Command(Boolean needPlayer, List<String> aliases) {
    }
}