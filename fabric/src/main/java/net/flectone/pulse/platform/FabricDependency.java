package net.flectone.pulse.platform;

import com.alessiodp.libby.FabricLibraryManager;
import com.alessiodp.libby.Library;
import lombok.Getter;
import net.flectone.pulse.BuildConfig;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class FabricDependency {

    protected final List<Library> libraries = new ArrayList<>();

    @Getter
    private final FabricLibraryManager libraryManager;

    public FabricDependency(String modId, Logger logger) {
        this.libraryManager = new FabricLibraryManager(modId, logger, "libraries");
    }

    public void resolveDependencies() {
        libraryManager.addMavenCentral();
        libraryManager.addJitPack();
        libraryManager.addJCenter();
        libraryManager.addSonatype();
        libraryManager.addRepository("https://repo.aikar.co/content/groups/aikar/");
        libraryManager.addRepository("https://repo.minebench.de");
        libraryManager.addRepository("https://s01.oss.sonatype.org/content/repositories/snapshots");
        libraryManager.addRepository("https://repo.codemc.io/repository/maven-releases");
        libraries.forEach(libraryManager::loadLibrary);
    }

    public void loadLibraries() {
        addDependency("com.google.inject", "guice", BuildConfig.GUICE_VERSION);

        addDependency("org.xerial", "sqlite-jdbc", "3.47.1.0");
        addDependency("net.elytrium", "serializer", BuildConfig.ELYTRIUM_SERIALIZER_VERSION);
//        addDependency("com.github.retrooper", "packetevents-fabric", BuildConfig.PACKETEVENTS_SPIGOT_VERSION);
        addDependency("com.zaxxer", "HikariCP", BuildConfig.HIKARICP_VERSION);
    }

    public void addDependency(String groupId, String artifactId, String version) {
        addDependency(groupId, artifactId, version, true, null, null);
    }

    public void addDependency(String groupId, String artifactId, String version, boolean resolveTransitive, String toRelocate, String relocatedPackage) {
        addDependency(buildLibrary(groupId, artifactId, version, resolveTransitive, toRelocate, relocatedPackage).build());
    }

    public void addDependency(Library library) {
        libraries.add(library);
    }

    public Library.Builder buildLibrary(String groupId, String artifactId, String version, boolean resolveTransitiveDependencies, String toRelocate, String relocatedPackage) {
        Library.Builder builder = Library.builder()
                .groupId(groupId.replace(".", "{}"))
                .artifactId(artifactId)
                .version(version)
                .resolveTransitiveDependencies(resolveTransitiveDependencies);

        if (toRelocate != null && relocatedPackage != null) {
            builder.relocate(toRelocate.replace(".", "{}"), relocatedPackage.replace(".", "{}"));
        }

        return builder;
    }

}
