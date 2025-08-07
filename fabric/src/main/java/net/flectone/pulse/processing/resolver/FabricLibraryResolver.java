package net.flectone.pulse.processing.resolver;

import com.alessiodp.libby.FabricLibraryManager;
import com.alessiodp.libby.Library;
import com.google.inject.Singleton;
import net.flectone.pulse.BuildConfig;
import org.slf4j.Logger;

@Singleton
public class FabricLibraryResolver extends LibraryResolver {

    public FabricLibraryResolver(Logger logger) {
        super(new FabricLibraryManager(BuildConfig.PROJECT_MOD_ID, logger, "libraries"));
    }

    @Override
    public void addLibraries() {
        super.addLibraries();

        addLibrary(Library.builder()
                .groupId("net{}kyori")
                .artifactId("adventure-api")
                .version(BuildConfig.ADVENTURE_API)
                .resolveTransitiveDependencies(true)
                .build()
        );

        addLibrary(Library.builder()
                .groupId("net{}kyori")
                .artifactId("adventure-text-minimessage")
                .version(BuildConfig.ADVENTURE_API)
                .resolveTransitiveDependencies(true)
                .build()
        );

        addLibrary(Library.builder()
                .groupId("net{}kyori")
                .artifactId("adventure-text-serializer-gson")
                .version(BuildConfig.ADVENTURE_API)
                .resolveTransitiveDependencies(true)
                .build()
        );

        addLibrary(Library.builder()
                .groupId("net{}kyori")
                .artifactId("adventure-text-serializer-plain")
                .version(BuildConfig.ADVENTURE_API)
                .resolveTransitiveDependencies(true)
                .build()
        );

        addLibrary(Library.builder()
                .groupId("net{}kyori")
                .artifactId("adventure-text-serializer-json-legacy-impl")
                .version(BuildConfig.ADVENTURE_API)
                .resolveTransitiveDependencies(true)
                .build()
        );

        addLibrary(Library.builder()
                .groupId("net{}kyori")
                .artifactId("adventure-text-serializer-legacy")
                .version(BuildConfig.ADVENTURE_API)
                .resolveTransitiveDependencies(true)
                .build()
        );

        addLibrary(Library.builder()
                .groupId("net{}kyori")
                .artifactId("adventure-nbt")
                .version(BuildConfig.ADVENTURE_API)
                .resolveTransitiveDependencies(true)
                .build()
        );

        addLibrary(Library.builder()
                .groupId("net{}kyori")
                .artifactId("adventure-key")
                .version(BuildConfig.ADVENTURE_API)
                .resolveTransitiveDependencies(true)
                .build()
        );

        addLibrary(Library.builder()
                .groupId("org{}xerial")
                .artifactId("sqlite-jdbc")
                .version("3.47.1.0")
                .resolveTransitiveDependencies(true)
                .build()
        );
    }

}
