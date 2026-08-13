package net.flectone.pulse.resolver;

import com.alessiodp.libby.Library;
import com.alessiodp.libby.relocation.Relocation;
import com.google.inject.Singleton;
import net.fabricmc.loader.api.FabricLoader;
import net.flectone.pulse.BuildConfig;
import net.flectone.pulse.logging.FLogger;

import java.util.List;

@Singleton
public class FabricLibraryResolver extends LibraryResolverImpl {

    public FabricLibraryResolver(FLogger fLogger) {
        super(fLogger, FabricLoader.getInstance().getConfigDir().resolve(BuildConfig.PROJECT_MOD_ID));
    }

    @Override
    public List<String> getPacketEventsArtifactIds() {
        return List.of(
                "packeteventsmodern-fabric",
                "packeteventsmodern-fabric-common",
                "packeteventsmodern-fabric-official",
                "packeteventsmodern-api",
                "packeteventsmodern-netty-common"
        );
    }

    @Override
    public void loadLibraries() {
        super.loadLibraries();

        getAdventureArtifactIds().forEach(artifactId -> loadLibrary(Library.builder()
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

        getPacketEventsArtifactIds().forEach(artifactId -> loadLibrary(Library.builder()
                .groupId("net{}flectone")
                .artifactId(artifactId)
                .version(BuildConfig.PACKETEVENTS_SPIGOT_VERSION)
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

        loadLibrary(Library.builder()
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

        loadLibrary(Library.builder()
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
