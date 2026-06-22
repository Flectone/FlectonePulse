package net.flectone.pulse.processing.resolver;

import com.alessiodp.libby.Library;
import com.google.inject.Singleton;
import net.flectone.pulse.BuildConfig;
import net.flectone.pulse.processing.resolver.libby.NeoForgeLibbyResolver;
import net.neoforged.fml.ModContainer;
import org.slf4j.Logger;

@Singleton
public class NeoForgeLibraryResolver extends LibraryResolver {

    public NeoForgeLibraryResolver(ModContainer modContainer, Logger logger) {
        super(new NeoForgeLibbyResolver(modContainer, BuildConfig.PROJECT_MOD_ID, logger, "libraries"));
    }

    @Override
    public void addLibraries() {
        super.addLibraries();

        getAdventureArtifactIds().forEach(artifactId -> addLibrary(Library.builder()
                .groupId("net{}kyori")
                .artifactId(artifactId)
                .version(BuildConfig.LEGACY_ADVENTURE_API)
                .repository(BuildConfig.MAVEN_REPOSITORY)
                .resolveTransitiveDependencies(true)
                .build()
        ));

        addLibrary(Library.builder()
                .groupId("org{}incendo")
                .artifactId("cloud-core")
                .version(BuildConfig.CLOUD_CORE_VERSION)
                .repository(BuildConfig.MAVEN_REPOSITORY)
                .resolveTransitiveDependencies(true)
                .build()
        );

        addLibrary(Library.builder()
                .groupId("org{}incendo")
                .artifactId("cloud-brigadier")
                .version(BuildConfig.CLOUD_PAPER_VERSION)
                .repository(BuildConfig.MAVEN_REPOSITORY)
                .resolveTransitiveDependencies(true)
                .build()
        );

        addLibrary(Library.builder()
                .groupId("org{}incendo")
                .artifactId("cloud-minecraft-modded-common")
                .version(BuildConfig.CLOUD_FABRIC_VERSION)
                .repository(BuildConfig.MAVEN_REPOSITORY)
                .resolveTransitiveDependencies(true)
                .build()
        );

        addLibrary(Library.builder()
                .groupId("org{}incendo")
                .artifactId("cloud-neoforge")
                .version(BuildConfig.CLOUD_NEOFORGE_VERSION)
                .repository(BuildConfig.MAVEN_REPOSITORY)
                .build()
        );
    }
}
