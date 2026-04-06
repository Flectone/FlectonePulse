package net.flectone.pulse.processing.resolver;

import com.alessiodp.libby.Library;
import com.google.inject.Singleton;
import net.flectone.pulse.BuildConfig;
import net.flectone.pulse.processing.resolver.libby.FabricLibbyResolver;
import org.slf4j.Logger;

@Singleton
public class FabricLibraryResolver extends LibraryResolver {

    public FabricLibraryResolver(Logger logger) {
        super(new FabricLibbyResolver(BuildConfig.PROJECT_MOD_ID, logger, "libraries"));
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
                .build()
        );

        addLibrary(Library.builder()
                .groupId("net{}kyori")
                .artifactId("adventure-text-serializer-ansi")
                .version(BuildConfig.ADVENTURE_API)
                .repository(BuildConfig.MAVEN_REPOSITORY)
                .resolveTransitiveDependencies(true)
                .build()
        );

        addLibrary(Library.builder()
                .groupId("net{}kyori")
                .artifactId("adventure-text-minimessage")
                .version(BuildConfig.ADVENTURE_API)
                .repository(BuildConfig.MAVEN_REPOSITORY)
                .resolveTransitiveDependencies(true)
                .build()
        );

        addLibrary(Library.builder()
                .groupId("net{}kyori")
                .artifactId("adventure-text-serializer-gson")
                .version(BuildConfig.ADVENTURE_API)
                .repository(BuildConfig.MAVEN_REPOSITORY)
                .resolveTransitiveDependencies(true)
                .build()
        );

        addLibrary(Library.builder()
                .groupId("net{}kyori")
                .artifactId("adventure-text-serializer-plain")
                .version(BuildConfig.ADVENTURE_API)
                .repository(BuildConfig.MAVEN_REPOSITORY)
                .resolveTransitiveDependencies(true)
                .build()
        );

        addLibrary(Library.builder()
                .groupId("net{}kyori")
                .artifactId("adventure-text-serializer-json-legacy-impl")
                .version(BuildConfig.ADVENTURE_API)
                .repository(BuildConfig.MAVEN_REPOSITORY)
                .resolveTransitiveDependencies(true)
                .build()
        );

        addLibrary(Library.builder()
                .groupId("net{}kyori")
                .artifactId("adventure-text-serializer-legacy")
                .version(BuildConfig.ADVENTURE_API)
                .repository(BuildConfig.MAVEN_REPOSITORY)
                .resolveTransitiveDependencies(true)
                .build()
        );

        addLibrary(Library.builder()
                .groupId("net{}kyori")
                .artifactId("adventure-nbt")
                .version(BuildConfig.ADVENTURE_API)
                .repository(BuildConfig.MAVEN_REPOSITORY)
                .resolveTransitiveDependencies(true)
                .build()
        );

        addLibrary(Library.builder()
                .groupId("net{}kyori")
                .artifactId("adventure-key")
                .version(BuildConfig.ADVENTURE_API)
                .repository(BuildConfig.MAVEN_REPOSITORY)
                .resolveTransitiveDependencies(true)
                .build()
        );

        addLibrary(Library.builder()
                .groupId("org{}incendo")
                .artifactId("cloud-core")
                .version(BuildConfig.CLOUD_CORE_VERSION)
                .repository(BuildConfig.MAVEN_REPOSITORY)
                .resolveTransitiveDependencies(true)
                .build()
        );

        addLibrary(Library.builder()
                .groupId("net{}flectone")
                .artifactId("cloud-brigadier")
                .version(BuildConfig.CLOUD_PAPER_VERSION)
                .repository(BuildConfig.MAVEN_REPOSITORY)
                .resolveTransitiveDependencies(true)
                .build()
        );

        addLibrary(Library.builder()
                .groupId("net{}flectone")
                .artifactId("cloud-minecraft-modded-common")
                .version(BuildConfig.CLOUD_FABRIC_VERSION)
                .repository(BuildConfig.MAVEN_REPOSITORY)
                .resolveTransitiveDependencies(true)
                .build()
        );

        addLibrary(Library.builder()
                .groupId("net{}flectone")
                .artifactId("cloud-fabric")
                .version(BuildConfig.CLOUD_FABRIC_VERSION)
                .repository(BuildConfig.MAVEN_REPOSITORY)
                .build()
        );
    }

}
