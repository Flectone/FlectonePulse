package net.flectone.pulse.processing.resolver;

import com.alessiodp.libby.Library;
import com.alessiodp.libby.LibraryManager;
import com.alessiodp.libby.relocation.Relocation;
import lombok.Getter;
import net.flectone.pulse.BuildConfig;

import java.util.ArrayList;
import java.util.List;

public abstract class LibraryResolver {

    private final List<Library> libraries = new ArrayList<>();

    @Getter private final LibraryManager libraryManager;

    protected LibraryResolver(LibraryManager libraryManager) {
        this.libraryManager = libraryManager;
    }

    public void addLibrary(Library library) {
        libraries.add(library);
    }

    public void loadLibrary(Library library) {
        libraryManager.loadLibrary(library);
    }

    public void loadLibraries(List<Library> libraries) {
        libraries.forEach(this::loadLibrary);
    }

    public void loadLibraries() {
        loadLibraries(libraries);
    }

    public void resolveRepositories() {
        libraryManager.addMavenCentral();
        libraryManager.addJitPack();
        libraryManager.addJCenter();
        libraryManager.addSonatype();
        libraryManager.addRepository("https://hub.spigotmc.org/nexus/content/repositories/snapshots/");
        libraryManager.addRepository("https://repo.aikar.co/content/groups/aikar/");
        libraryManager.addRepository("https://repo.minebench.de");
        libraryManager.addRepository("https://s01.oss.sonatype.org/content/repositories/snapshots");
        libraryManager.addRepository("https://repo.codemc.io/repository/maven-releases");
        libraryManager.addRepository("https://repo.codemc.io/repository/maven-snapshots/");
        libraryManager.addRepository("https://maven.fabricmc.net/");
    }

    public void addLibraries() {

        addLibrary(Library.builder()
                .groupId("com{}google{}inject")
                .artifactId("guice")
                .version(BuildConfig.GUICE_VERSION)
                .resolveTransitiveDependencies(true)
                .relocate(Relocation.builder()
                                .pattern("com{}google{}inject")
                                .relocatedPattern("net.flectone.pulse.library.guice")
                                .build()
                ).relocate(Relocation.builder()
                                .pattern("com{}google{}common")
                                .relocatedPattern("net.flectone.pulse.library.guava")
                                .build()
                )
                .build()
        );

        addLibrary(Library.builder()
                .groupId("net{}elytrium")
                .artifactId("serializer")
                .version(BuildConfig.ELYTRIUM_SERIALIZER_VERSION)
                .resolveTransitiveDependencies(true)
                .relocate(Relocation.builder()
                        .pattern("net{}elytrium{}serializer")
                        .relocatedPattern("net.flectone.pulse.library.elytrium")
                        .build()
                )
                .build()
        );

        addLibrary(Library.builder()
                .groupId("com{}zaxxer")
                .artifactId("HikariCP")
                .version(BuildConfig.HIKARICP_VERSION)
                .resolveTransitiveDependencies(true)
                .relocate(Relocation.builder()
                        .pattern("com{}zaxxer")
                        .relocatedPattern("net.flectone.pulse.library.hikaricp")
                        .build()
                )
                .build()
        );

        addLibrary(Library.builder()
                .groupId("org{}jdbi")
                .artifactId("jdbi3-core")
                .version(BuildConfig.JDBI3_CORE_VERSION)
                .resolveTransitiveDependencies(true)
                .relocate(Relocation.builder()
                        .pattern("org{}jdbi")
                        .relocatedPattern("net.flectone.pulse.library.jdbi3")
                        .build()
                )
                .build()
        );

        addLibrary(Library.builder()
                .groupId("org{}jdbi")
                .artifactId("jdbi3-sqlobject")
                .version(BuildConfig.JDBI3_CORE_VERSION)
                .resolveTransitiveDependencies(true)
                .relocate(Relocation.builder()
                        .pattern("org{}jdbi")
                        .relocatedPattern("net.flectone.pulse.library.jdbi3")
                        .build()
                )
                .build()
        );

        addLibrary(Library.builder()
                .groupId("org{}apache{}commons")
                .artifactId("commons-text")
                .version(BuildConfig.APACHE_COMMONS_TEXT_VERSION)
                .resolveTransitiveDependencies(true)
                .relocate(Relocation.builder()
                        .pattern("org{}apache{}commons")
                        .relocatedPattern("net.flectone.pulse.library.apache")
                        .build()
                )
                .build()
        );
    }
}
