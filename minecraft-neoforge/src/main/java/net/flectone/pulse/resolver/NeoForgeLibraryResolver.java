package net.flectone.pulse.resolver;

import com.alessiodp.libby.Library;
import com.alessiodp.libby.relocation.Relocation;
import com.google.inject.Singleton;
import net.flectone.pulse.BuildConfig;
import net.flectone.pulse.logging.FLogger;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLPaths;
import org.apache.maven.artifact.versioning.ArtifactVersion;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;

import java.util.List;

@Singleton
public class NeoForgeLibraryResolver extends LibraryResolverImpl {

    public NeoForgeLibraryResolver(FLogger fLogger) {
        super(fLogger, FMLPaths.CONFIGDIR.get().resolve(BuildConfig.PROJECT_MOD_ID));
    }

    @Override
    public List<String> getPacketEventsArtifactIds() {
        return List.of(
                "packeteventsmodern-neoforge",
                "packeteventsmodern-api",
                "packeteventsmodern-netty-common"
        );
    }

    @Override
    public void loadLibraries() {
        super.loadLibraries();

        String cloudNeoForgeVersion = getCloudNeoForgeVersion();

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
                .version(BuildConfig.PACKETEVENTSMODERN_VERSION)
                .repository(BuildConfig.MAVEN_REPOSITORY)
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
                .relocate(Relocation.builder()
                        .pattern("io{}leangen{}geantyref")
                        .relocatedPattern(BuildConfig.RELOCATED_PATTERN + ".geantyref")
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
                .relocate(Relocation.builder()
                        .pattern("io{}leangen{}geantyref")
                        .relocatedPattern(BuildConfig.RELOCATED_PATTERN + ".geantyref")
                        .build()
                )
                .build()
        );

        loadLibrary(Library.builder()
                .groupId("org{}incendo")
                .artifactId("cloud-minecraft-modded-common")
                .version(cloudNeoForgeVersion)
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
                .groupId("org{}incendo")
                .artifactId("cloud-neoforge")
                .version(cloudNeoForgeVersion)
                .repository(BuildConfig.MAVEN_REPOSITORY)
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
    }

    private String getCloudNeoForgeVersion() {
        ArtifactVersion minecraftVersion = ModList.get().getModContainerById("minecraft")
                .map(container -> container.getModInfo().getVersion())
                .orElseThrow();

        if (isNewerThanOrEquals(minecraftVersion, "26.3")) return BuildConfig.CLOUD_NEOFORGE_VERSION;
        if (isNewerThanOrEquals(minecraftVersion, "26.2")) return BuildConfig.CLOUD_NEOFORGE_V26_2_VERSION;
        if (isNewerThanOrEquals(minecraftVersion, "26.1")) return BuildConfig.CLOUD_NEOFORGE_V26_1_VERSION;
        if (isNewerThanOrEquals(minecraftVersion, "1.21.11")) return BuildConfig.CLOUD_NEOFORGE_V1_21_11_VERSION;
        if (isNewerThanOrEquals(minecraftVersion, "1.21.9")) return BuildConfig.CLOUD_NEOFORGE_V1_21_9_VERSION;

        return BuildConfig.CLOUD_NEOFORGE_V1_21_1_VERSION;
    }

    private boolean isNewerThanOrEquals(ArtifactVersion version, String target) {
        return version.compareTo(new DefaultArtifactVersion(target)) >= 0;
    }
}
