package net.flectone.pulse.processing.resolver;

import com.alessiodp.libby.Library;
import com.alessiodp.libby.logging.LogLevel;
import com.alessiodp.libby.logging.adapters.LogAdapter;
import com.alessiodp.libby.relocation.Relocation;
import com.google.inject.Singleton;
import com.hypixel.hytale.logger.HytaleLogger;
import net.flectone.pulse.BuildConfig;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.nio.file.Path;
import java.util.logging.Level;

@Singleton
public class HytaleLibraryResolver extends LibraryResolverImpl {

    public HytaleLibraryResolver(HytaleLogger hytaleLogger, Path projectPath) {
        super(new LogAdapter() {
            @Override
            public void log(@NonNull LogLevel logLevel, @Nullable String s) {
                switch (logLevel) {
                    case INFO, DEBUG -> hytaleLogger.at(Level.INFO).log(s);
                    case WARN -> hytaleLogger.at(Level.WARNING).log(s);
                    case ERROR -> hytaleLogger.at(Level.SEVERE).log(s);
                }
            }

            @Override
            public void log(@NonNull LogLevel logLevel, @Nullable String s, @Nullable Throwable throwable) {
                switch (logLevel) {
                    case INFO, DEBUG -> hytaleLogger.at(Level.INFO).log(s, throwable);
                    case WARN -> hytaleLogger.at(Level.WARNING).log(s, throwable);
                    case ERROR -> hytaleLogger.at(Level.SEVERE).log(s, throwable);
                }
            }
        }, projectPath);
    }

    @Override
    public void loadLibraries() {
        super.loadLibraries();

        getAdventureArtifactIds().forEach(artifactId -> loadLibrary(Library.builder()
                .groupId("net{}kyori")
                .artifactId(artifactId)
                .version(BuildConfig.NEW_ADVENTURE_API)
                .repository(BuildConfig.MAVEN_REPOSITORY)
                .resolveTransitiveDependencies(true)
                .relocate(Relocation.builder()
                        .pattern("net{}kyori")
                        .relocatedPattern(BuildConfig.RELOCATED_PATTERN)
                        .build()
                )
                .relocate(Relocation.builder()
                        .pattern("com{}google{}gson")
                        .relocatedPattern(BuildConfig.RELOCATED_PATTERN + ".gson")
                        .build()
                )
                .build()
        ));

        loadLibrary(Library.builder()
                .groupId("eu{}mikart{}adventure")
                .artifactId("adventure-platform-hytale")
                .version(BuildConfig.ADVENTURE_PLATFORM_HYTALE_VERSION)
                .repository("https://repo.codemc.io/repository/ArikSquad/")
                .relocate(Relocation.builder()
                        .pattern("net{}kyori")
                        .relocatedPattern(BuildConfig.RELOCATED_PATTERN)
                        .build()
                )
                .relocate(Relocation.builder()
                        .pattern("com{}google{}gson")
                        .relocatedPattern(BuildConfig.RELOCATED_PATTERN + ".gson")
                        .build()
                )
                .build()
        );

        loadLibrary(Library.builder()
                .groupId("curse{}maven")
                .artifactId("hyui-1431415")
                .version(BuildConfig.HYUI_VERSION)
                .repository("https://www.cursemaven.com")
                .resolveTransitiveDependencies(true)
                .build()
        );

        loadLibrary(Library.builder()
                .groupId("org{}incendo")
                .artifactId("cloud-core")
                .version(BuildConfig.CLOUD_CORE_VERSION)
                .repository(BuildConfig.MAVEN_REPOSITORY)
                .resolveTransitiveDependencies(true)
                .relocate(Relocation.builder()
                        .pattern("org{}incendo")
                        .relocatedPattern(BuildConfig.RELOCATED_PATTERN + ".cloud")
                        .build()
                )
                .build()
        );

        loadLibrary(Library.builder()
                .groupId("org{}apache{}logging{}log4j")
                .artifactId("log4j-core")
                .version(BuildConfig.APACHE_LOGGING_LOG4J_VERSION)
                .repository(BuildConfig.MAVEN_REPOSITORY)
                .resolveTransitiveDependencies(true)
                .build()
        );

        loadLibrary(Library.builder()
                .groupId("org{}apache{}logging{}log4j")
                .artifactId("log4j-slf4j2-impl")
                .version(BuildConfig.APACHE_LOGGING_LOG4J_VERSION)
                .repository(BuildConfig.MAVEN_REPOSITORY)
                .resolveTransitiveDependencies(true)
                .build()
        );
    }

}
