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
        String server,
        String version,

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/config/language")
        Language language,

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/config/database")
        Database database,

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/config/executor")
        Executor executor,

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/config/proxy")
        Proxy proxy,

        @JsonPropertyDescription(" https://flectone.net/pulse/docs/config/internal")
        Internal internal,

        @Deprecated(forRemoval = true)
        DeprecatedCommand command,

        @Deprecated(forRemoval = true)
        DeprecatedModule module,

        @JsonPropertyDescription("https://flectone.net/pulse/docs/config/logger")
        Logger logger,

        @JsonPropertyDescription("https://flectone.net/pulse/docs/config/cache")
        Cache cache,

        @JsonPropertyDescription("Help us improve FlectonePulse! This collects basic, anonymous data like server version and module usage. \nNo personal data, No IPs, No player names. \nThis helps us understand what features matter most and focus development where it's needed. \nYou can see the public stats here: https://flectone.net/pulse/docs/metrics/ \nThanks for supporting the project! ❤️")
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
    public record Database(
            Boolean ignoreExistingDriver,
            Boolean usePlaytimeTracking,
            net.flectone.pulse.data.database.Database.Type type,
            String name,
            String host,
            String port,
            String user,
            String password,
            String parameters,
            String prefix
    ) {
    }

    @With
    @Builder(toBuilder = true)
    @Jacksonized
    public record Executor(
            Integer minPoolSize,
            Integer maxPoolSize,
            WorkQueue workQueue,
            DurationUnit keepAlive,
            DurationUnit shutdownTimeout
    ) {

        @With
        @Builder(toBuilder = true)
        @Jacksonized
        public record DurationUnit(
                Long duration,
                TimeUnit timeUnit
        ) {
        }

        public enum WorkQueue {
            SYNCHRONOUS,
            LINKED_BLOCKING
        }

    }

    @With
    @Builder(toBuilder = true)
    @Jacksonized
    public record Internal(
            Boolean enable,
            Boolean alwaysSendSilentPacket,
            Boolean usePaperMessageSender,
            Boolean usePacketLoginListener,
            Boolean unregisterCommandOnReload,
            Set<String> vanillaCommandsToRemove
    ) implements EnableSetting {
    }

    @With
    @Builder(toBuilder = true)
    @Jacksonized
    public record Proxy(
            Set<String> clusters,
            Boolean bungeecord,
            Boolean velocity,
            Redis redis
    ) {

        @With
        @Builder(toBuilder = true)
        @Jacksonized
        public record Redis(
                Boolean enable,
                String host,
                Integer port,
                Boolean ssl,
                String user,
                String password
        ) implements EnableSetting {
        }

    }

    @Deprecated(forRemoval = true)
    @With
    @Builder(toBuilder = true)
    @Jacksonized
    public record DeprecatedCommand(
            Boolean unregisterOnReload,
            Set<String> disabledFabric
    ) {
    }

    @Deprecated(forRemoval = true)
    @With
    @Builder(toBuilder = true)
    @Jacksonized
    public record DeprecatedModule(
            Boolean enable,
            Boolean alwaysSendSilentPacket,
            Boolean usePaperMessageSender,
            Boolean useBukkitPreLoginListener
    ) implements EnableSetting {
    }

    @With
    @Builder(toBuilder = true)
    @Jacksonized
    public record Logger(
            String console,
            String prefix,
            List<String> description,
            String primary,
            String warn,
            String info,
            List<String> filter
    ) {
    }

    @With
    @Builder(toBuilder = true)
    @Jacksonized
    public record Cache(Map<CacheName, CacheSetting> types) {

        @With
        @Builder(toBuilder = true)
        @Jacksonized
        public record CacheSetting(boolean invalidateOnReload, long duration, TimeUnit timeUnit, long size) {
        }
    }

    @With
    @Builder(toBuilder = true)
    @Jacksonized
    public record Metrics(Boolean enable) {
    }
}