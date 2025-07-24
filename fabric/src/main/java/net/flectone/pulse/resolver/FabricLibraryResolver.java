package net.flectone.pulse.resolver;

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
                .groupId("org{}xerial")
                .artifactId("sqlite-jdbc")
                .version("3.47.1.0")
                .resolveTransitiveDependencies(true)
                .build()
        );
    }

}
