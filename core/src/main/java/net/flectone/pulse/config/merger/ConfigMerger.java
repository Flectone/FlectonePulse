package net.flectone.pulse.config.merger;

import net.flectone.pulse.config.Config;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

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
    @Mapping(target = "proxy", expression = "java(mergeProxy(target.build().proxy().toBuilder(), source.proxy()))")
    @Mapping(target = "internal", expression = "java(mergeInternal(target.build().internal().toBuilder(), source.internal()))")
    @Mapping(target = "logger", expression = "java(mergeLogger(target.build().logger().toBuilder(), source.logger()))")
    @Mapping(target = "cache", expression = "java(mergeCache(target.build().cache().toBuilder(), source.cache()))")
    @Mapping(target = "metrics", expression = "java(mergeMetrics(target.build().metrics().toBuilder(), source.metrics()))")
    Config merge(@MappingTarget Config.ConfigBuilder target, Config source);

    Config.Language mergeLanguage(@MappingTarget Config.Language.LanguageBuilder target, Config.Language source);

    Config.Database mergeDatabase(@MappingTarget Config.Database.DatabaseBuilder target, Config.Database source);

    @Mapping(target = "redis", expression = "java(mergeProxyRedis(target.build().redis().toBuilder(), source.redis()))")
    Config.Proxy mergeProxy(@MappingTarget Config.Proxy.ProxyBuilder target, Config.Proxy source);

    Config.Proxy.Redis mergeProxyRedis(@MappingTarget Config.Proxy.Redis.RedisBuilder target, Config.Proxy.Redis source);

    Config.Internal mergeInternal(@MappingTarget Config.Internal.InternalBuilder target, Config.Internal source);

    Config.Logger mergeLogger(@MappingTarget Config.Logger.LoggerBuilder target, Config.Logger source);

    Config.Cache mergeCache(@MappingTarget Config.Cache.CacheBuilder target, Config.Cache source);

    Config.Metrics mergeMetrics(@MappingTarget Config.Metrics.MetricsBuilder target, Config.Metrics source);

}