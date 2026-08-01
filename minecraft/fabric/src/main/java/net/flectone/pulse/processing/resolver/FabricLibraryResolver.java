package net.flectone.pulse.processing.resolver;

import com.alessiodp.libby.Library;
import com.alessiodp.libby.relocation.Relocation;
import com.google.inject.Singleton;
import net.flectone.pulse.BuildConfig;
import net.flectone.pulse.processing.resolver.libby.FabricLibbyResolver;
import org.slf4j.Logger;

import java.util.List;

@Singleton
public class FabricLibraryResolver extends LibraryResolver {

    public FabricLibraryResolver(Logger logger) {
        super(new FabricLibbyResolver(BuildConfig.PROJECT_MOD_ID, logger, "libraries"));
    }

    @Override
    public List<String> getPacketEventsArtifactIds() {
        return List.of(
                "packetevents-fabric",
                "packetevents-fabric-common",
                "packetevents-fabric-official",
                "packetevents-api",
                "packetevents-netty-common"
        );
    }

    @Override
    public void addLibraries() {
        super.addLibraries();

        getAdventureArtifactIds().forEach(artifactId -> addLibrary(Library.builder()
                .groupId("net{}kyori")
                .artifactId(artifactId)
                .version(BuildConfig.ADVENTURE_API)
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

        getPacketEventsArtifactIds().forEach(artifactId -> addLibrary(Library.builder()
                .groupId("com{}github{}retrooper")
                .artifactId(artifactId)
                .version("2.13.0") // TODO always upload PacketEvents to net.flectone maven repository
                .repository(BuildConfig.CODEMC_REPOSITORY)
                .fallbackRepository(BuildConfig.CODEMC_REPOSITORY)
                .relocate(Relocation.builder()
                        .pattern("com{}github{}retrooper")
                        .relocatedPattern(BuildConfig.RELOCATED_PATTERN)
                        .build()
                )
                .relocate(Relocation.builder()
                        .pattern("io{}github{}retrooper{}packetevents")
                        .relocatedPattern(BuildConfig.RELOCATED_PATTERN + ".packetevents.impl")
                        .build()
                )
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

        addLibrary(Library.builder()
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

        addLibrary(Library.builder()
                .groupId("org{}incendo")
                .artifactId("cloud-brigadier")
                .version(BuildConfig.CLOUD_PAPER_VERSION)
                .repository(BuildConfig.MAVEN_REPOSITORY)
                .resolveTransitiveDependencies(true)
                .relocate(Relocation.builder()
                        .pattern("org{}incendo")
                        .relocatedPattern(BuildConfig.RELOCATED_PATTERN + ".cloud")
                        .build()
                )
                .build()
        );

        addLibrary(Library.builder()
                .groupId("org{}incendo")
                .artifactId("cloud-minecraft-modded-common")
                .version(BuildConfig.CLOUD_FABRIC_VERSION)
                .repository(BuildConfig.MAVEN_REPOSITORY)
                .resolveTransitiveDependencies(true)
                .relocate(Relocation.builder()
                        .pattern("org{}incendo")
                        .relocatedPattern(BuildConfig.RELOCATED_PATTERN + ".cloud")
                        .build()
                )
                .build()
        );

        addLibrary(Library.builder()
                .groupId("org{}incendo")
                .artifactId("cloud-fabric")
                .version(BuildConfig.CLOUD_FABRIC_VERSION)
                .repository(BuildConfig.MAVEN_REPOSITORY)
                .relocate(Relocation.builder()
                        .pattern("org{}incendo")
                        .relocatedPattern(BuildConfig.RELOCATED_PATTERN + ".cloud")
                        .build()
                )
                .build()
        );
    }

}
