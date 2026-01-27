package net.flectone.pulse.processing.resolver;

import com.alessiodp.libby.Library;
import com.alessiodp.libby.LibraryManager;
import com.alessiodp.libby.relocation.Relocation;
import com.google.inject.Singleton;
import net.flectone.pulse.BuildConfig;

@Singleton
public class HytaleLibraryResolver extends LibraryResolver {

    public HytaleLibraryResolver(LibraryManager libraryManager) {
        super(libraryManager);
    }

    @Override
    public void addLibraries() {
        super.addLibraries();

        addLibrary(Library.builder()
                .groupId("net{}kyori")
                .artifactId("adventure-api")
                .version(BuildConfig.ADVENTURE_API)
                .repository(BuildConfig.MAVEN_REPOSITORY)
                .resolveTransitiveDependencies(true)
                .relocate(Relocation.builder()
                        .pattern("net{}kyori")
                        .relocatedPattern("net.flectone.pulse.library")
                        .build()
                )
                .build()
        );

        addLibrary(Library.builder()
                .groupId("net{}kyori")
                .artifactId("adventure-text-serializer-ansi")
                .version(BuildConfig.ADVENTURE_API)
                .repository(BuildConfig.MAVEN_REPOSITORY)
                .resolveTransitiveDependencies(true)
                .relocate(Relocation.builder()
                        .pattern("net{}kyori")
                        .relocatedPattern("net.flectone.pulse.library")
                        .build()
                )
                .build()
        );

        addLibrary(Library.builder()
                .groupId("net{}kyori")
                .artifactId("adventure-text-minimessage")
                .version(BuildConfig.ADVENTURE_API)
                .repository(BuildConfig.MAVEN_REPOSITORY)
                .resolveTransitiveDependencies(true)
                .relocate(Relocation.builder()
                        .pattern("net{}kyori")
                        .relocatedPattern("net.flectone.pulse.library")
                        .build()
                )
                .build()
        );

        addLibrary(Library.builder()
                .groupId("net{}kyori")
                .artifactId("adventure-text-serializer-plain")
                .version(BuildConfig.ADVENTURE_API)
                .repository(BuildConfig.MAVEN_REPOSITORY)
                .resolveTransitiveDependencies(true)
                .relocate(Relocation.builder()
                        .pattern("net{}kyori")
                        .relocatedPattern("net.flectone.pulse.library")
                        .build()
                )
                .build()
        );

        addLibrary(Library.builder()
                .groupId("net{}kyori")
                .artifactId("adventure-text-serializer-legacy")
                .version(BuildConfig.ADVENTURE_API)
                .repository(BuildConfig.MAVEN_REPOSITORY)
                .resolveTransitiveDependencies(true)
                .relocate(Relocation.builder()
                        .pattern("net{}kyori")
                        .relocatedPattern("net.flectone.pulse.library")
                        .build()
                )
                .build()
        );

        addLibrary(Library.builder()
                .groupId("net{}kyori")
                .artifactId("adventure-nbt")
                .version(BuildConfig.ADVENTURE_API)
                .repository(BuildConfig.MAVEN_REPOSITORY)
                .resolveTransitiveDependencies(true)
                .relocate(Relocation.builder()
                        .pattern("net{}kyori")
                        .relocatedPattern("net.flectone.pulse.library")
                        .build()
                )
                .build()
        );

        addLibrary(Library.builder()
                .groupId("net{}kyori")
                .artifactId("adventure-text-serializer-gson")
                .version(BuildConfig.ADVENTURE_API)
                .repository(BuildConfig.MAVEN_REPOSITORY)
                .resolveTransitiveDependencies(true)
                .relocate(Relocation.builder()
                        .pattern("net{}kyori")
                        .relocatedPattern("net.flectone.pulse.library")
                        .build()
                )
                .relocate(Relocation.builder()
                        .pattern("com{}google{}gson")
                        .relocatedPattern("net.flectone.pulse.library.gson")
                        .build()
                )
                .build()
        );


        addLibrary(Library.builder()
                .groupId("net{}kyori")
                .artifactId("adventure-platform-facet")
                .version("4.4.1")
                .repository(BuildConfig.MAVEN_REPOSITORY)
                .resolveTransitiveDependencies(true)
                .relocate(Relocation.builder()
                        .pattern("net{}kyori")
                        .relocatedPattern("net.flectone.pulse.library")
                        .build()
                )
                .build()
        );

        addLibrary(Library.builder()
                .groupId("com{}github{}ArikSquad")
                .artifactId("adventure-platform-hytale")
                .version("1.0.1")
                .repository(BuildConfig.JITPACK_REPOSITORY)
//                .resolveTransitiveDependencies(true)
                .relocate(Relocation.builder()
                        .pattern("net{}kyori")
                        .relocatedPattern("net.flectone.pulse.library")
                        .build()
                )
                .relocate(Relocation.builder()
                        .pattern("com{}google{}gson")
                        .relocatedPattern("net.flectone.pulse.library.gson")
                        .build()
                )
                .build()
        );

        addLibrary(Library.builder()
                .groupId("org{}incendo")
                .artifactId("cloud-core")
                .version(BuildConfig.CLOUD_CORE_VERSION)
                .repository(BuildConfig.MAVEN_REPOSITORY)
                .resolveTransitiveDependencies(true)
                .relocate(Relocation.builder()
                        .pattern("org{}incendo")
                        .relocatedPattern("net.flectone.pulse.library.cloud")
                        .build()
                )
                .build()
        );

        addLibrary(Library.builder()
                .groupId("org{}apache{}logging{}log4j")
                .artifactId("log4j-core")
                .version(BuildConfig.APACHE_LOGGING_LOG4J_VERSION)
                .repository(BuildConfig.MAVEN_REPOSITORY)
                .resolveTransitiveDependencies(true)
                .build()
        );

        addLibrary(Library.builder()
                .groupId("org{}apache{}logging{}log4j")
                .artifactId("log4j-slf4j2-impl")
                .version(BuildConfig.APACHE_LOGGING_LOG4J_VERSION)
                .repository(BuildConfig.MAVEN_REPOSITORY)
                .resolveTransitiveDependencies(true)
                .build()
        );
    }

}
