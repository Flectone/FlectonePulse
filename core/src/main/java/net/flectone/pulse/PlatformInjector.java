package net.flectone.pulse;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.google.common.cache.Cache;
import com.google.gson.Gson;
import com.google.inject.AbstractModule;
import com.google.inject.Provider;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.matcher.Matchers;
import com.google.inject.name.Named;
import com.google.inject.name.Names;
import lombok.SneakyThrows;
import net.flectone.pulse.annotation.Async;
import net.flectone.pulse.annotation.Sync;
import net.flectone.pulse.config.localization.EnglishLocale;
import net.flectone.pulse.config.localization.RussianLocale;
import net.flectone.pulse.execution.scheduler.TaskScheduler;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.util.*;
import net.flectone.pulse.platform.adapter.PlatformServerAdapter;
import net.flectone.pulse.processing.processor.YamlFileProcessor;
import net.flectone.pulse.processing.resolver.FileResolver;
import net.flectone.pulse.processing.resolver.LibraryResolver;
import net.flectone.pulse.processing.resolver.ReflectionResolver;
import net.flectone.pulse.processing.resolver.SystemVariableResolver;
import net.flectone.pulse.platform.registry.CacheRegistry;
import net.flectone.pulse.util.creator.BackupCreator;
import net.flectone.pulse.util.interceptor.AsyncInterceptor;
import net.flectone.pulse.util.interceptor.SyncInterceptor;
import net.flectone.pulse.util.logging.FLogger;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.object.PlayerHeadObjectContents;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
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

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class PlatformInjector extends AbstractModule {

    private final Path projectPath;
    private final LibraryResolver libraryResolver;
    private final FLogger fLogger;

    public PlatformInjector(Path projectPath, LibraryResolver libraryResolver, FLogger fLogger) {
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
        bind(Gson.class).toInstance(GsonComponentSerializer.gson().serializer());
        bind(MiniMessage.class).toInstance(MiniMessage.builder().tags(TagResolver.builder().build()).build());

        ReflectionResolver reflectionResolver = new ReflectionResolver(libraryResolver);
        bind(ReflectionResolver.class).toInstance(reflectionResolver);

        // bind paths
        setupPaths();

        // bind booleans
        setupBooleans();

        // bind files
        setupConfigurations();

        // platform binding
        setupPlatform(reflectionResolver);

        // Interceptors
        setupInterceptors();

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

    private void setupPaths() {
        bind(Path.class).annotatedWith(Names.named("projectPath")).toInstance(projectPath);
        bind(Path.class).annotatedWith(Names.named("imagePath")).toInstance(projectPath.resolve("images"));
        bind(Path.class).annotatedWith(Names.named("backupPath")).toInstance(projectPath.resolve("backups"));
        bind(Path.class).annotatedWith(Names.named("translationPath")).toInstance(projectPath.resolve("localizations/minecraft"));
    }

    private void setupBooleans() {
        ServerVersion serverVersion = PacketEvents.getAPI().getServerManager().getVersion();

        bind(Boolean.class).annotatedWith(Names.named("isNewerThanOrEqualsV_1_14")).toInstance(serverVersion.isNewerThanOrEquals(ServerVersion.V_1_14));
        bind(Boolean.class).annotatedWith(Names.named("isNewerThanOrEqualsV_1_21_6")).toInstance(serverVersion.isNewerThanOrEquals(ServerVersion.V_1_21_6));
        bind(Boolean.class).annotatedWith(Names.named("isNewerThanOrEqualsV_1_21_9")).toInstance(serverVersion.isNewerThanOrEquals(ServerVersion.V_1_21_9));
    }

    private void setupConfigurations() throws IOException {
        ObjectMapper mapper = createMapper();
        bind(ObjectMapper.class).toInstance(mapper);

        EnglishLocale englishLocale = new EnglishLocale();
        bind(EnglishLocale.class).toInstance(englishLocale);

        RussianLocale russianLocale = new RussianLocale();
        bind(RussianLocale.class).toInstance(russianLocale);

        YamlFileProcessor yamlFileProcessor = new YamlFileProcessor(mapper, englishLocale, russianLocale);
        bind(YamlFileProcessor.class).toInstance(yamlFileProcessor);

        SystemVariableResolver systemVariableResolver = new SystemVariableResolver();
        bind(SystemVariableResolver.class).toInstance(systemVariableResolver);

        BackupCreator backupCreator = new BackupCreator(systemVariableResolver, projectPath, projectPath.resolve("backups"), fLogger);
        bind(BackupCreator.class).toInstance(backupCreator);

        FileResolver fileResolver = new FileResolver(projectPath, fLogger, yamlFileProcessor, backupCreator);
        bind(FileResolver.class).toInstance(fileResolver);
        fileResolver.reload();

        CacheRegistry cacheRegistry = new CacheRegistry(fileResolver);
        bind(CacheRegistry.class).toInstance(cacheRegistry);
        cacheRegistry.init();
    }

    @Provides @Singleton @Named("offlinePlayers")
    public Cache<UUID, FPlayer> provideOfflinePlayersCache(CacheRegistry cacheRegistry) {
        return cacheRegistry.getOfflinePlayersCache();
    }

    @Provides @Singleton @Named("profileProperty")
    public Cache<UUID, PlayerHeadObjectContents.ProfileProperty> provideProfilePropertyCache(CacheRegistry cacheRegistry) {
        return cacheRegistry.getProfilePropertyCache();
    }

    @Provides @Singleton @Named("dialogClick")
    public Cache<UUID, AtomicInteger> provideDialogClickCache(CacheRegistry cacheRegistry) {
        return cacheRegistry.getDialogClickCache();
    }

    @Provides @Singleton @Named("moderation")
    public Cache<Pair<UUID, Moderation.Type>, List<Moderation>> provideModerationCache(CacheRegistry cacheRegistry) {
        return cacheRegistry.getModerationCache();
    }

    @Provides @Singleton @Named("legacyColorMessage")
    public Cache<String, String> provideLegacyColorMessageCache(CacheRegistry cacheRegistry) {
        return cacheRegistry.getLegacyColorMessageCache();
    }

    @Provides @Singleton @Named("mentionMessage")
    public Cache<String, String> provideMentionMessageCache(CacheRegistry cacheRegistry) {
        return cacheRegistry.getMentionMessageCache();
    }

    @Provides @Singleton @Named("swearMessage")
    public Cache<String, String> provideSwearMessageCache(CacheRegistry cacheRegistry) {
        return cacheRegistry.getSwearMessageCache();
    }

    @Provides @Singleton @Named("replacementMessage")
    public Cache<String, String> provideReplacementMessageCache(CacheRegistry cacheRegistry) {
        return cacheRegistry.getReplacementMessageCache();
    }

    @Provides @Singleton @Named("replacementImage")
    public Cache<String, Component> provideReplacementImageCache(CacheRegistry cacheRegistry) {
        return cacheRegistry.getReplacementImageCache();
    }

    @Provides @Singleton @Named("translateMessage")
    public Cache<String, UUID> provideTranslateMessageCache(CacheRegistry cacheRegistry) {
        return cacheRegistry.getTranslateMessageCache();
    }

    private void setupInterceptors() {
        Provider<TaskScheduler> taskSchedulerProvider = getProvider(TaskScheduler.class);
        Provider<PlatformServerAdapter> platformServerAdapterProvider = getProvider(PlatformServerAdapter.class);

        AsyncInterceptor asyncInterceptor = new AsyncInterceptor(taskSchedulerProvider, platformServerAdapterProvider, fLogger);
        bind(AsyncInterceptor.class).toInstance(asyncInterceptor);

        SyncInterceptor syncInterceptor = new SyncInterceptor(taskSchedulerProvider, platformServerAdapterProvider, fLogger);
        bind(SyncInterceptor.class).toInstance(syncInterceptor);

        bindInterceptor(
                Matchers.any(),
                Matchers.annotatedWith(Sync.class).or(Matchers.annotatedWith(Async.class)),
                asyncInterceptor,
                syncInterceptor
        );
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
                .enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT)
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
                .defaultMergeable(true)
                .withConfigOverride(Destination.class, o -> o.setMergeable(false))
                .withConfigOverride(Sound.class, o -> o.setMergeable(false))
                .withConfigOverride(Range.class, o -> o.setMergeable(false))
                .withConfigOverride(Ticker.class, o -> o.setMergeable(false))
                .withConfigOverride(Cooldown.class, o -> o.setMergeable(false))
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
