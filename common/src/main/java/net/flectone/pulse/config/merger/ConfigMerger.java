package net.flectone.pulse.config.merger;

import net.flectone.pulse.config.Config;
import net.flectone.pulse.constant.CacheName;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.EnumMap;
import java.util.Map;

/**
 * MapStruct mapper for merging {@link Config} configuration objects.
 * <p>
 * This interface defines mapping methods for deep merging plugin configurations,
 * handling nested structures through builder patterns.
 * </p>
 *
 * @author TheFaser
 * @since 1.7.1
 */
@Mapper(config = MapstructMergerConfig.class)
public interface ConfigMerger {

    @Mapping(target = "language", expression = "java(mergeLanguage(target.build().language().toBuilder(), source.language()))")
    @Mapping(target = "database", expression = "java(mergeDatabase(target.build().database().toBuilder(), source.database()))")
    @Mapping(target = "executor", expression = "java(mergeExecutor(target.build().executor().toBuilder(), source.executor()))")
    @Mapping(target = "proxy", expression = "java(mergeProxy(target.build().proxy().toBuilder(), source.proxy()))")
    @Mapping(target = "internal", expression = "java(mergeInternal(target.build().internal().toBuilder(), source.internal()))")
    @Mapping(target = "logger", expression = "java(mergeLogger(target.build().logger().toBuilder(), source.logger()))")
    @Mapping(target = "cache", expression = "java(mergeCache(target.build().cache().toBuilder(), source.cache()))")
    @Mapping(target = "metrics", expression = "java(mergeMetrics(target.build().metrics().toBuilder(), source.metrics()))")
    Config merge(@MappingTarget Config.ConfigBuilder target, Config source);

    Config.Language mergeLanguage(@MappingTarget Config.Language.LanguageBuilder target, Config.Language source);

    @Mapping(target = "poolSettings", expression = "java(mergeDatabasePoolSettings(target.build().poolSettings().toBuilder(), source.poolSettings()))")
    Config.Database mergeDatabase(@MappingTarget Config.Database.DatabaseBuilder target, Config.Database source);

    Config.Database.PoolSettings mergeDatabasePoolSettings(@MappingTarget Config.Database.PoolSettings.PoolSettingsBuilder target, Config.Database.PoolSettings source);

    @Mapping(target = "keepAlive", expression = "java(mergeExecutorDurationUnit(target.build().keepAlive().toBuilder(), source.keepAlive()))")
    @Mapping(target = "shutdownTimeout", expression = "java(mergeExecutorDurationUnit(target.build().shutdownTimeout().toBuilder(), source.shutdownTimeout()))")
    Config.Executor mergeExecutor(@MappingTarget Config.Executor.ExecutorBuilder target, Config.Executor source);

    Config.Executor.DurationUnit mergeExecutorDurationUnit(@MappingTarget Config.Executor.DurationUnit.DurationUnitBuilder target, Config.Executor.DurationUnit source);

    @Mapping(target = "redis", expression = "java(mergeProxyRedis(target.build().redis().toBuilder(), source.redis()))")
    Config.Proxy mergeProxy(@MappingTarget Config.Proxy.ProxyBuilder target, Config.Proxy source);

    Config.Proxy.Redis mergeProxyRedis(@MappingTarget Config.Proxy.Redis.RedisBuilder target, Config.Proxy.Redis source);

    Config.Internal mergeInternal(@MappingTarget Config.Internal.InternalBuilder target, Config.Internal source);

    Config.Logger mergeLogger(@MappingTarget Config.Logger.LoggerBuilder target, Config.Logger source);

    default Config.Cache mergeCache(@MappingTarget Config.Cache.CacheBuilder target, Config.Cache source) {
        Map<CacheName, Config.Cache.CacheSetting> merged = new EnumMap<>(CacheName.class);

        Map<CacheName, Config.Cache.CacheSetting> defaults = target.build().types();
        if (defaults != null) {
            merged.putAll(defaults);
        }

        if (source != null && source.types() != null) {
            source.types().forEach((cacheName, cacheSetting) -> {
                if (cacheName != null && cacheSetting != null) {
                    merged.put(cacheName, cacheSetting);
                }
            });
        }

        return target.types(merged).build();
    }

    Config.Metrics mergeMetrics(@MappingTarget Config.Metrics.MetricsBuilder target, Config.Metrics source);

}