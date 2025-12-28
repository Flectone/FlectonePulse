package net.flectone.pulse.config;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.Builder;
import lombok.With;
import lombok.extern.jackson.Jacksonized;
import net.flectone.pulse.config.setting.EnableSetting;
import net.flectone.pulse.util.constant.CacheName;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Configuration for the FlectonePulse.
 * Contains all top-level configuration sections and settings.
 *
 * @author TheFaser
 * @since 1.7.1
 */
@With
@Builder(toBuilder = true)
@Jacksonized
public record Config(

        @JsonPropertyDescription(" Don't change it if you don't know what it is")
        String version,

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/config/language/")
        Language language,

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/config/database/")
        Database database,

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/config/proxy/")
        Proxy proxy,

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/config/command/")
        Command command,

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/config/module/")
        Module module,

        @JsonPropertyDescription("https://flectone.net/pulse/docs/config/editor/")
        Editor editor,

        @JsonPropertyDescription("https://flectone.net/pulse/docs/config/logger/")
        Logger logger,

        @JsonPropertyDescription("https://flectone.net/pulse/docs/config/cache/")
        Cache cache,

        @JsonPropertyDescription("Help us improve FlectonePulse! This collects basic, anonymous data like server version and module usage. \nNo personal data, No IPs, No player names. \nThis helps us understand what features matter most and focus development where it's needed. \nYou can see the public stats here: https://flectone.net/pulse/metrics/ \nThanks for supporting the project! ❤️")
        Metrics metrics

) {

    @With
    @Builder(toBuilder = true)
    @Jacksonized
    public record Language(String type,
                           Boolean byPlayer) {
    }

    @With
    @Builder(toBuilder = true)
    @Jacksonized
    public record Database(Boolean ignoreExistingDriver,
                           net.flectone.pulse.data.database.Database.Type type,
                           String name,
                           String host,
                           String port,
                           String user,
                           String password,
                           String parameters,
                           String prefix) {
    }

    @With
    @Builder(toBuilder = true)
    @Jacksonized
    public record Proxy(Set<String> clusters,
                        Boolean bungeecord,
                        Boolean velocity,
                        @JsonPropertyDescription(" https://flectone.net/pulse/docs/config/proxy#redis")
                        Redis redis) {

        @With
        @Builder(toBuilder = true)
        @Jacksonized
        public record Redis(Boolean enable,
                            String host,
                            Integer port,
                            Boolean ssl,
                            String user,
                            String password) implements EnableSetting {
        }
    }

    @With
    @Builder(toBuilder = true)
    @Jacksonized
    public record Command(Boolean unregisterOnReload,
                          Set<String> disabledFabric) {
    }

    @With
    @Builder(toBuilder = true)
    @Jacksonized
    public record Module(Boolean enable,
                         Boolean usePaperMessageSender) implements EnableSetting {
    }

    @With
    @Builder(toBuilder = true)
    @Jacksonized
    public record Editor(String host,
                         Boolean https,
                         Integer port) {
    }

    @With
    @Builder(toBuilder = true)
    @Jacksonized
    public record Logger(String console,
                         String prefix,
                         List<String> description,
                         String warn,
                         String info,
                         List<String> filter) {
    }

    @With
    @Builder(toBuilder = true)
    @Jacksonized
    public record Cache(Map<CacheName, CacheSetting> types) {

        @With
        @Builder(toBuilder = true)
        @Jacksonized
        public record CacheSetting(long duration, TimeUnit timeUnit, long size) {
        }
    }

    @With
    @Builder(toBuilder = true)
    @Jacksonized
    public record Metrics(Boolean enable) {
    }
}