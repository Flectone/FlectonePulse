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
        libraryManager.addRepository(BuildConfig.MAVEN_REPOSITORY);
        libraryManager.addRepository(BuildConfig.CODEMC_REPOSITORY);
        libraryManager.addRepository(BuildConfig.JITPACK_REPOSITORY);

        libraryManager.addSonatype();
        libraryManager.addJCenter();
    }

    public void addLibraries() {

        addLibrary(Library.builder()
                .groupId("com{}google{}inject")
                .artifactId("guice")
                .version(BuildConfig.GUICE_VERSION)
                .repository(BuildConfig.MAVEN_REPOSITORY)
                .resolveTransitiveDependencies(true)
                .relocate(Relocation.builder()
                        .pattern("com{}google{}inject")
                        .relocatedPattern("net.flectone.pulse.library.guice")
                        .build()
                )
                .relocate(Relocation.builder()
                        .pattern("com{}google{}common")
                        .relocatedPattern("net.flectone.pulse.library.guava")
                        .build()
                )
                .build()
        );

        addLibrary(Library.builder()
                .groupId("tools{}jackson{}dataformat")
                .artifactId("jackson-dataformat-yaml")
                .version(BuildConfig.JACKSON_DATAFORMAT_VERSION)
                .repository(BuildConfig.MAVEN_REPOSITORY)
                .resolveTransitiveDependencies(true)
                .relocate(Relocation.builder()
                        .pattern("com{}fasterxml{}jackson")
                        .relocatedPattern("net.flectone.pulse.library.jackson")
                        .build()
                )
                .relocate(Relocation.builder()
                        .pattern("tools{}jackson")
                        .relocatedPattern("net.flectone.pulse.library.jackson")
                        .build()
                )
                .relocate(Relocation.builder()
                        .pattern("org{}snakeyaml{}engine")
                        .relocatedPattern("net.flectone.pulse.library.snakeyaml")
                        .build()
                )
                .build()
        );

        addLibrary(Library.builder()
                .groupId("com{}zaxxer")
                .artifactId("HikariCP")
                .version(BuildConfig.HIKARICP_VERSION)
                .repository(BuildConfig.MAVEN_REPOSITORY)
                .relocate(Relocation.builder()
                        .pattern("com{}zaxxer{}hikari")
                        .relocatedPattern("net.flectone.pulse.library.hikari")
                        .build()
                )
                .build()
        );

        addLibrary(Library.builder()
                .groupId("org{}jdbi")
                .artifactId("jdbi3-core")
                .version(BuildConfig.JDBI3_CORE_VERSION)
                .repository(BuildConfig.MAVEN_REPOSITORY)
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
                .repository(BuildConfig.MAVEN_REPOSITORY)
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
                .repository(BuildConfig.MAVEN_REPOSITORY)
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
