package net.flectone.pulse.platform;

import com.alessiodp.libby.FabricLibraryManager;
import com.alessiodp.libby.Library;
import com.alessiodp.libby.relocation.Relocation;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.BuildConfig;
import org.slf4j.Logger;

@Singleton
public class FabricDependencyResolver extends DependencyResolver {

    @Inject
    public FabricDependencyResolver(String modId, Logger logger) {
        super(new FabricLibraryManager(modId, logger, "libraries"));
    }

    @Override
    public void resolveDependencies() {
        getLibraryManager().addRepository("https://maven.fabricmc.net/");

        super.resolveDependencies();
    }

    @Override
    public void loadLibraries() {
//        super.loadLibraries();

        addDependency(Library.builder()
                .groupId("com{}google{}inject")
                .artifactId("guice")
                .version(BuildConfig.GUICE_VERSION)
                .resolveTransitiveDependencies(true)
                .build()
        );

        addDependency(Library.builder()
                .groupId("net{}elytrium")
                .artifactId("serializer")
                .version(BuildConfig.ELYTRIUM_SERIALIZER_VERSION)
                .resolveTransitiveDependencies(true)
                .build()
        );

        addDependency(Library.builder()
                .groupId("net{}kyori")
                .artifactId("adventure-api")
                .version(BuildConfig.ADVENTURE_API)
                .resolveTransitiveDependencies(true)
                .relocate(Relocation.builder()
                        .pattern("net{}kyori")
                        .relocatedPattern("net.flectone.pulse.library")
                        .build())
                .build()
        );

        addDependency(Library.builder()
                .groupId("net{}kyori")
                .artifactId("adventure-text-minimessage")
                .version(BuildConfig.ADVENTURE_API)
                .resolveTransitiveDependencies(true)
//                .relocate(Relocation.builder()
//                        .pattern("net{}kyori")
//                        .relocatedPattern("net.flectone.pulse.library")
//                        .build())
                .build()
        );

        addDependency(Library.builder()
                .groupId("net{}kyori")
                .artifactId("adventure-text-serializer-plain")
                .version(BuildConfig.ADVENTURE_API)
                .resolveTransitiveDependencies(true)
//                .relocate(Relocation.builder()
//                        .pattern("net{}kyori")
//                        .relocatedPattern("net.flectone.pulse.library")
//                        .build())
                .build()
        );

        addDependency(Library.builder()
                .groupId("net{}kyori")
                .artifactId("adventure-text-serializer-legacy")
                .version(BuildConfig.ADVENTURE_API)
                .resolveTransitiveDependencies(true)
//                .relocate(Relocation.builder()
//                        .pattern("net{}kyori")
//                        .relocatedPattern("net.flectone.pulse.library")
//                        .build())
                .build()
        );

        addDependency(Library.builder()
                .groupId("net{}kyori")
                .artifactId("adventure-nbt")
                .version(BuildConfig.ADVENTURE_API)
                .resolveTransitiveDependencies(true)
//                .relocate(Relocation.builder()
//                        .pattern("net{}kyori")
//                        .relocatedPattern("net.flectone.pulse.library")
//                        .build())
                .build()
        );

        addDependency(Library.builder()
                .groupId("net{}kyori")
                .artifactId("adventure-platform-fabric")
                .version("4.0.0")
//                .relocate(Relocation.builder()
//                        .pattern("net{}kyori")
//                        .relocatedPattern("net.flectone.pulse.library")
//                        .build())
                .build()
        );

//        addDependency(Library.builder()
//                .groupId("com{}github{}retrooper")
//                .artifactId("packetevents-fabric")
//                .version(BuildConfig.PACKETEVENTS_SPIGOT_VERSION)
////                .relocate(Relocation.builder()
////                        .pattern("com{}github{}retrooper")
////                        .relocatedPattern("net.flectone.pulse.library")
////                        .build())
////                .relocate(Relocation.builder()
////                        .pattern("io{}github{}retrooper{}packetevents")
////                        .relocatedPattern("net.flectone.pulse.library.packetevents.impl")
////                        .build())
////                .relocate(Relocation.builder()
////                        .pattern("net{}kyori")
////                        .relocatedPattern("net.flectone.pulse.library")
////                        .build())
//                .resolveTransitiveDependencies(true)
//                .build()
//        );

        addDependency(Library.builder()
                .groupId("org{}xerial")
                .artifactId("sqlite-jdbc")
                .version("3.47.1.0")
                .resolveTransitiveDependencies(true)
                .build()
        );

        addDependency(Library.builder()
                .groupId("com{}zaxxer")
                .artifactId("HikariCP")
                .version(BuildConfig.HIKARICP_VERSION)
                .resolveTransitiveDependencies(true)
//                .relocate(Relocation.builder()
//                        .pattern("com{}zaxxer")
//                        .relocatedPattern("net.flectone.pulse.library.database")
//                        .build()
//                )
                .build()
        );
    }

}
