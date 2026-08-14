package net.flectone.pulse.resolver;

import com.alessiodp.libby.Library;
import com.alessiodp.libby.relocation.Relocation;
import com.google.inject.Singleton;
import net.flectone.pulse.BuildConfig;
import net.flectone.pulse.logging.FLogger;
import org.bukkit.plugin.Plugin;

import java.util.List;

@Singleton
public class BukkitLibraryResolver extends LibraryResolverImpl {

    public BukkitLibraryResolver(FLogger fLogger, Plugin plugin) {
        super(fLogger, plugin.getDataFolder().toPath());
    }

    @Override
    public List<String> getPacketEventsArtifactIds() {
        return List.of(
                "packeteventsmodern-spigot",
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
                .groupId("com{}github{}Anon8281")
                .artifactId("UniversalScheduler")
                .version(BuildConfig.UNIVERSALSCHEDULER_VERSION)
                .repository(BuildConfig.JITPACK_REPOSITORY)
                .resolveTransitiveDependencies(true)
                .relocate(Relocation.builder()
                        .pattern("com{}github{}Anon8281")
                        .relocatedPattern(BuildConfig.RELOCATED_PATTERN + ".universalscheduler")
                        .build()
                )
                .build()
        );

        loadLibrary(Library.builder()
                .groupId("org{}incendo")
                .artifactId("cloud-paper")
                .resolveTransitiveDependencies(true)
                .version(BuildConfig.CLOUD_PAPER_VERSION)
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
