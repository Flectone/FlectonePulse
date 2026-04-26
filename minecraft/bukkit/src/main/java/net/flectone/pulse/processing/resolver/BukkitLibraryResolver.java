package net.flectone.pulse.processing.resolver;

import com.alessiodp.libby.Library;
import com.alessiodp.libby.relocation.Relocation;
import com.google.inject.Singleton;
import net.flectone.pulse.BuildConfig;
import net.flectone.pulse.processing.resolver.libby.BukkitLibbyResolver;
import org.bukkit.plugin.Plugin;

import java.util.List;

@Singleton
public class BukkitLibraryResolver extends LibraryResolver {

    public BukkitLibraryResolver(Plugin plugin) {
        super(new BukkitLibbyResolver(plugin, "libraries"));
    }

    public List<String> getPacketEventsArtifactIds() {
        return List.of(
                "packetevents-spigot",
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
                .version(BuildConfig.PACKETEVENTS_SPIGOT_VERSION)
                .repository(BuildConfig.CODEMC_REPOSITORY)
                .fallbackRepository("https://repo.codemc.io/repository/maven-snapshots/")
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
