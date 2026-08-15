package net.flectone.pulse.resolver;

import com.alessiodp.libby.Library;
import com.alessiodp.libby.relocation.Relocation;
import com.google.inject.Singleton;
import net.flectone.pulse.BuildConfig;
import net.flectone.pulse.logging.FLogger;

import java.nio.file.Path;

@Singleton
public class HytaleLibraryResolver extends LibraryResolverImpl {

    public HytaleLibraryResolver(FLogger fLogger, Path projectPath) {
        super(fLogger, projectPath);
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
                .relocate(Relocation.builder()
                        .pattern("io{}leangen{}geantyref")
                        .relocatedPattern(BuildConfig.RELOCATED_PATTERN + ".geantyref")
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
