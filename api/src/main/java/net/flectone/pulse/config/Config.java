package net.flectone.pulse.config;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.Builder;
import lombok.With;
import net.flectone.pulse.config.setting.EnableSetting;
import net.flectone.pulse.constant.CacheName;
import net.flectone.pulse.constant.DatabaseType;

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

        @JsonPropertyDescription("https://flectone.net/pulse/docs/config/logger")
        Logger logger,

        @JsonPropertyDescription("https://flectone.net/pulse/docs/config/cache")
        Cache cache,

        @JsonPropertyDescription("Help us improve FlectonePulse! This collects basic, anonymous data like server version and module usage. \nNo personal data, No IPs, No player names. \nThis helps us understand what features matter most and focus development where it's needed. \nYou can see the public stats here: https://flectone.net/pulse/docs/metrics/ \nThanks for supporting the project! ❤️")
        Metrics metrics

) {

    @With
    @Builder(toBuilder = true)
    public record Language(String type,
                           Boolean byPlayer) {
    }

    @With
    @Builder(toBuilder = true)
    public record Database(
            Boolean usePlaytimeTracking,
            DatabaseType type,
            String name,
            String host,
            String port,
            String user,
            String password,
            String parameters,
            String prefix,
            PoolSettings poolSettings
    ) {

        @With
        @Builder(toBuilder = true)
        public record PoolSettings(
                Integer maxSize,
                Integer minIdle,
                Long maxLifetime,
                Long keepaliveTime,
                Long connectionTimeout
        ) {
        }

    }

    @With
    @Builder(toBuilder = true)
    public record Executor(
            Integer minPoolSize,
            Integer maxPoolSize,
            Boolean allowCoreThreadTimeout,
            WorkQueue workQueue,
            DurationUnit keepAlive,
            DurationUnit shutdownTimeout
    ) {

        @With
        @Builder(toBuilder = true)
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
    public record Internal(
            Boolean enable,
            Boolean alwaysSendSilentPacket,
            Boolean useVanillaMessageSender,

            @Deprecated
            Boolean usePaperMessageSender,

            Boolean usePacketLoginListener,
            Boolean unregisterCommandOnReload,
            Set<String> vanillaCommandsToRemove
    ) implements EnableSetting {
    }

    @With
    @Builder(toBuilder = true)
    public record Proxy(
            Set<String> clusters,
            Boolean bungeecord,
            Boolean velocity,
            Redis redis
    ) {

        @With
        @Builder(toBuilder = true)
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

    @With
    @Builder(toBuilder = true)
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
    public record Cache(Map<CacheName, CacheSetting> types) {

        @With
        @Builder(toBuilder = true)
        public record CacheSetting(
                boolean invalidateOnReload,
                boolean expireAfterWrite,
                long duration,
                TimeUnit timeUnit,
                long size
        ) {
        }
    }

    @With
    @Builder(toBuilder = true)
    public record Metrics(Boolean enable) {
    }
}