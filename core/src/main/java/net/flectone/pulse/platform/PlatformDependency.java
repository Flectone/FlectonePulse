package net.flectone.pulse.platform;

import com.alessiodp.libby.Library;
import com.alessiodp.libby.LibraryManager;
import lombok.Getter;
import net.flectone.pulse.BuildConfig;

import java.util.ArrayList;
import java.util.List;

public abstract class PlatformDependency {

    protected final List<Library> libraries = new ArrayList<>();

    @Getter
    private final LibraryManager libraryManager;

    public PlatformDependency(LibraryManager libraryManager) {
        this.libraryManager = libraryManager;
    }

    public void resolveDependencies() {
        libraryManager.addMavenCentral();
        libraryManager.addJitPack();
        libraryManager.addJCenter();
        libraryManager.addSonatype();
        libraryManager.addRepository("https://hub.spigotmc.org/nexus/content/repositories/snapshots/");
        libraryManager.addRepository("https://repo.aikar.co/content/groups/aikar/");
        libraryManager.addRepository("https://repo.minebench.de");
        libraryManager.addRepository("https://s01.oss.sonatype.org/content/repositories/snapshots");
        libraryManager.addRepository("https://repo.codemc.io/repository/maven-releases");

        libraries.forEach(libraryManager::loadLibrary);
    }

    public void loadLibraries() {

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
