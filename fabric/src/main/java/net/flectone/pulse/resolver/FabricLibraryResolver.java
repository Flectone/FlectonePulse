package net.flectone.pulse.resolver;

import com.alessiodp.libby.FabricLibraryManager;
import com.alessiodp.libby.Library;
import com.google.inject.Singleton;
import net.flectone.pulse.BuildConfig;
import org.slf4j.Logger;

@Singleton
public class FabricLibraryResolver extends LibraryResolver {

    public FabricLibraryResolver(String modId, Logger logger) {
        super(new FabricLibraryManager(modId, logger, "libraries"));
    }

    @Override
    public void addLibraries() {
        super.addLibraries();

//        addLibrary(Library.builder()
//                .groupId("com{}github{}retrooper")
//                .artifactId("packetevents-fabric")
//                .version(BuildConfig.PACKETEVENTS_SPIGOT_VERSION)
//                .resolveTransitiveDependencies(true)
//                .relocate(Relocation.builder()
//                        .pattern("com{}github{}retrooper")
//                        .relocatedPattern("net.flectone.pulse.library")
//                        .build())
//                .relocate(Relocation.builder()
//                        .pattern("io{}github{}retrooper{}packetevents")
//                        .relocatedPattern("net.flectone.pulse.library.packetevents.impl")
//                        .build())
//                .relocate(Relocation.builder()
//                        .pattern("net{}kyori")
//                        .relocatedPattern("net.flectone.pulse.library")
//                        .build())
//                .build()
//        );

        addLibrary(Library.builder()
                .groupId("org{}xerial")
                .artifactId("sqlite-jdbc")
                .version("3.47.1.0")
                .resolveTransitiveDependencies(true)
                .build()
        );

        addLibrary(Library.builder()
                .groupId("org{}incendo")
                .artifactId("cloud-fabric")
                .version(BuildConfig.CLOUD_PAPER_VERSION)
                .resolveTransitiveDependencies(true)
                .build()
        );
    }

}
