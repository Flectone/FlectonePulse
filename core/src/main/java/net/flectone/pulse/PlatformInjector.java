package net.flectone.pulse;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import com.google.common.cache.Cache;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.google.inject.name.Names;
import lombok.SneakyThrows;
import net.flectone.pulse.data.repository.CooldownRepository;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.util.Moderation;
import net.flectone.pulse.platform.registry.CacheRegistry;
import net.flectone.pulse.processing.resolver.LibraryResolver;
import net.flectone.pulse.processing.resolver.ReflectionResolver;
import net.flectone.pulse.util.constant.CacheName;
import net.flectone.pulse.util.logging.FLogger;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.object.PlayerHeadObjectContents;
import org.incendo.cloud.type.tuple.Pair;
import org.snakeyaml.engine.v2.api.LoadSettings;
import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.databind.*;
import tools.jackson.databind.cfg.EnumFeature;
import tools.jackson.databind.module.SimpleModule;
import tools.jackson.dataformat.yaml.YAMLFactory;
import tools.jackson.dataformat.yaml.YAMLMapper;
import tools.jackson.dataformat.yaml.YAMLWriteFeature;

import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class PlatformInjector extends AbstractModule {

    private final Path projectPath;
    private final LibraryResolver libraryResolver;
    private final FLogger fLogger;

    protected PlatformInjector(Path projectPath, LibraryResolver libraryResolver, FLogger fLogger) {
        this.projectPath = projectPath;
        this.libraryResolver = libraryResolver;
        this.fLogger = fLogger;
    }

    @SneakyThrows
    @Override
    protected void configure() {
        bind(FLogger.class).toInstance(fLogger);
        bind(FlectonePulseAPI.class).asEagerSingleton();
        bind(LibraryResolver.class).toInstance(libraryResolver);
        bind(MiniMessage.class).toInstance(MiniMessage.builder().tags(TagResolver.builder().build()).build());

        ReflectionResolver reflectionResolver = new ReflectionResolver(libraryResolver);
        bind(ReflectionResolver.class).toInstance(reflectionResolver);

        // bind paths
        bind(Path.class).annotatedWith(Names.named("projectPath")).toInstance(projectPath);
        bind(Path.class).annotatedWith(Names.named("imagePath")).toInstance(projectPath.resolve("images"));
        bind(Path.class).annotatedWith(Names.named("backupPath")).toInstance(projectPath.resolve("backups"));
        bind(Path.class).annotatedWith(Names.named("minecraftPath")).toInstance(projectPath.resolve("minecraft"));

        // create jackson mapper
        bind(ObjectMapper.class).toInstance(createMapper());
        bind(ObjectMapper.class).annotatedWith(Names.named("defaultMapper")).toInstance(new ObjectMapper());

        // bind date format
        bind(SimpleDateFormat.class).toInstance(new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss"));

        // platform binding
        setupPlatform(reflectionResolver);

//        try {
//            Package[] packs = Package.getPackages();
//
//            Arrays.stream(packs)
//                    .map(Package::getName)
//                    .filter(string -> string.contains("net.flectone.pulse.library"))
//                    .sorted()
//                    .forEach(fLogger::warning);
//
//        } catch (Exception e) {
//            fLogger.warning(e);
//        }
    }

    public abstract void setupPlatform(ReflectionResolver reflectionResolver);

    @Provides @Singleton @Named("cooldown")
    public Cache<CooldownRepository.CooldownKey, Long> provideCooldownCache(CacheRegistry cacheRegistry) {
        return cacheRegistry.getCache(CacheName.COOLDOWN);
    }

    @Provides @Singleton @Named("offlinePlayers")
    public Cache<UUID, FPlayer> provideOfflinePlayersCache(CacheRegistry cacheRegistry) {
        return cacheRegistry.getCache(CacheName.OFFLINE_PLAYERS);
    }

    @Provides @Singleton @Named("profileProperty")
    public Cache<UUID, PlayerHeadObjectContents.ProfileProperty> provideProfilePropertyCache(CacheRegistry cacheRegistry) {
        return cacheRegistry.getCache(CacheName.PROFILE_PROPERTY);
    }

    @Provides @Singleton @Named("dialogClick")
    public Cache<UUID, AtomicInteger> provideDialogClickCache(CacheRegistry cacheRegistry) {
        return cacheRegistry.getCache(CacheName.DIALOG_CLICK);
    }

    @Provides @Singleton @Named("moderation")
    public Cache<Pair<UUID, Moderation.Type>, List<Moderation>> provideModerationCache(CacheRegistry cacheRegistry) {
        return cacheRegistry.getCache(CacheName.MODERATION);
    }

    @Provides @Singleton @Named("legacyColorMessage")
    public Cache<String, String> provideLegacyColorMessageCache(CacheRegistry cacheRegistry) {
        return cacheRegistry.getCache(CacheName.LEGACY_COLOR_MESSAGE);
    }

    @Provides @Singleton @Named("mentionMessage")
    public Cache<String, String> provideMentionMessageCache(CacheRegistry cacheRegistry) {
        return cacheRegistry.getCache(CacheName.MENTION_MESSAGE);
    }

    @Provides @Singleton @Named("swearMessage")
    public Cache<String, String> provideSwearMessageCache(CacheRegistry cacheRegistry) {
        return cacheRegistry.getCache(CacheName.SWEAR_MESSAGE);
    }

    @Provides @Singleton @Named("replacementMessage")
    public Cache<String, String> provideReplacementMessageCache(CacheRegistry cacheRegistry) {
        return cacheRegistry.getCache(CacheName.REPLACEMENT_MESSAGE);
    }

    @Provides @Singleton @Named("replacementImage")
    public Cache<String, Component> provideReplacementImageCache(CacheRegistry cacheRegistry) {
        return cacheRegistry.getCache(CacheName.REPLACEMENT_IMAGE);
    }

    @Provides @Singleton @Named("translateMessage")
    public Cache<String, UUID> provideTranslateMessageCache(CacheRegistry cacheRegistry) {
        return cacheRegistry.getCache(CacheName.TRANSLATE_MESSAGE);
    }

    private ObjectMapper createMapper() {
        return YAMLMapper.builder(
                        YAMLFactory.builder()
                                .loadSettings(LoadSettings.builder()
                                        .setBufferSize(8192) // increase string limit
                                        .setAllowDuplicateKeys(true) // fix duplicate keys
                                        .build()
                                )
                                .build()
                )
                // mapper
                .disable(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY) // disable auto sorting
                .disable(MapperFeature.DETECT_PARAMETER_NAMES) // [databind#5314]
                .enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS) // fix enum names
                .enable(MapperFeature.ALLOW_FINAL_FIELDS_AS_MUTATORS) // fix custom classes deserialization
                // deserialization
                .disable(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES) // jackson 2.x value
                .disable(DeserializationFeature.FAIL_ON_TRAILING_TOKENS) // jackson 2.x value
                .enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY) // convert single value to array
                .enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT) // fix empty null string
                // serialization
                .enable(SerializationFeature.INDENT_OUTPUT) // indent output for values
                .disable(YAMLWriteFeature.SPLIT_LINES) // fix split long values
                .disable(YAMLWriteFeature.WRITE_DOC_START_MARKER) // fix header
                .disable(YAMLWriteFeature.USE_NATIVE_TYPE_ID) // fix type id like !!java.util.Hashmap
                .propertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
                // enum
                .disable(EnumFeature.READ_ENUMS_USING_TO_STRING) // jackson 2.x value
                .disable(EnumFeature.WRITE_ENUMS_USING_TO_STRING) // jackson 2.x value
                // fix nulls
                .changeDefaultPropertyInclusion(config -> JsonInclude.Value.construct(JsonInclude.Include.NON_NULL, JsonInclude.Include.NON_NULL)) // show only non-null values
                .changeDefaultNullHandling(config -> JsonSetter.Value.forValueNulls(Nulls.SKIP)) // skip null values deserialization
                .withConfigOverride(String.class, o -> o.setNullHandling(JsonSetter.Value.forContentNulls(Nulls.AS_EMPTY))) // fix null string
                .withConfigOverride(Collection.class, o -> o.setNullHandling(JsonSetter.Value.forContentNulls(Nulls.AS_EMPTY))) // fix null collection
                .withConfigOverride(List.class, o -> o.setNullHandling(JsonSetter.Value.forContentNulls(Nulls.AS_EMPTY))) // fix null list
                .withConfigOverride(Set.class, o -> o.setNullHandling(JsonSetter.Value.forContentNulls(Nulls.AS_EMPTY))) // fix null set
                .withConfigOverride(Map.class, o -> o.setNullHandling(JsonSetter.Value.forContentNulls(Nulls.AS_EMPTY))) // fix null map
                .addModule(new SimpleModule().addDeserializer(String.class, new ValueDeserializer<>() {
                    // fix null values like "key: null"
                    // idk, why withConfigOverride(String.class, ...) doesn't fix it

                    @Override
                    public String deserialize(JsonParser p, DeserializationContext ctxt) {
                        return p.currentToken() == JsonToken.VALUE_NULL ? "" : p.getString();
                    }

                    @Override
                    public String getNullValue(DeserializationContext ctxt) {
                        return "";
                    }

                }))
                .build();
    }

}
