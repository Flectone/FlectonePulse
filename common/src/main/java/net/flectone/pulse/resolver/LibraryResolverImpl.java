package net.flectone.pulse.resolver;

import com.alessiodp.libby.Library;
import com.alessiodp.libby.LibraryManager;
import com.alessiodp.libby.classloader.ClassLoaderHelper;
import com.alessiodp.libby.classloader.SystemClassLoaderHelper;
import com.alessiodp.libby.classloader.URLClassLoaderHelper;
import com.alessiodp.libby.relocation.Relocation;
import com.google.inject.Singleton;
import net.flectone.pulse.BuildConfig;
import net.flectone.pulse.exception.LibraryLoadException;
import net.flectone.pulse.logging.FLogger;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collection;
import java.util.List;

import static java.util.Objects.requireNonNull;

@Singleton
public class LibraryResolverImpl extends LibraryManager implements LibraryResolver {

    private final ClassLoaderHelper classLoaderHelper;

    public LibraryResolverImpl(@NonNull FLogger fLogger, @NonNull Path dataDirectory) {
        super(fLogger.logAdapter(), dataDirectory, "libraries");

        ClassLoader classLoader = getClass().getClassLoader();
        if (classLoader instanceof URLClassLoader urlClassLoader) {
            classLoaderHelper = new URLClassLoaderHelper(urlClassLoader, this);
        } else if (classLoader == ClassLoader.getSystemClassLoader()) {
            classLoaderHelper = new SystemClassLoaderHelper(classLoader, this);
        } else {
            throw new LibraryLoadException("Unsupported class loader: " + classLoader.getClass().getName());
        }
    }

    @Override
    public @NonNull Path downloadLibrary(@NonNull Library library) {
        Path file = saveDirectory.resolve(requireNonNull(library, "library").getPath());
        if (Files.exists(file)) {
            if (library.hasRelocations()) {
                file = relocate(file, library.getRelocatedPath(), library.getRelocations());
            }

            return file;
        }

        Collection<String> urls = resolveLibrary(library);
        if (urls.isEmpty()) {
            throw new LibraryLoadException("Library '" + library + "' couldn't be resolved, add a repository");
        }

        MessageDigest md = null;
        if (library.hasChecksum()) {
            try {
                md = MessageDigest.getInstance("SHA-256");
            } catch (NoSuchAlgorithmException e) {
                throw new LibraryLoadException("SHA-256 is not available", e);
            }
        }

        Path out = file.resolveSibling(file.getFileName() + ".tmp");
        out.toFile().deleteOnExit();

        try {
            Files.createDirectories(file.getParent());

            for (String url : urls) {
                byte[] bytes = downloadLibrary(url);
                if (bytes == null) {
                    continue;
                }

                if (md != null) {
                    byte[] checksum = md.digest(bytes);
                    if (!Arrays.equals(checksum, library.getChecksum())) {
                        logger.warn("*** INVALID CHECKSUM ***");
                        logger.warn(" Library :  " + library);
                        logger.warn(" URL :  " + url);
                        logger.warn(" Expected :  " + Base64.getEncoder().encodeToString(library.getChecksum()));
                        logger.warn(" Actual :  " + Base64.getEncoder().encodeToString(checksum));
                        continue;
                    }
                }

                Files.write(out, bytes);
                Files.move(out, file);

                // Relocate the file
                if (library.hasRelocations()) {
                    file = relocate(file, library.getRelocatedPath(), library.getRelocations());
                }

                return file;
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } finally {
            try {
                Files.deleteIfExists(out);
            } catch (IOException _) {
            }
        }

        throw new LibraryLoadException("Failed to download library '" + library + "'");
    }

    @Override
    protected void addToClasspath(@NotNull Path path) {
        classLoaderHelper.addToClasspath(path);
    }

    @Override
    public void loadLibraries(List<Library> libraries) {
        libraries.forEach(this::loadLibrary);
    }

    @Override
    public void loadLibraries() {
        loadLibrary(Library.builder()
                .groupId("com{}google{}inject")
                .artifactId("guice")
                .version(BuildConfig.GUICE_VERSION)
                .repository(BuildConfig.MAVEN_REPOSITORY)
                .resolveTransitiveDependencies(true)
                .relocate(Relocation.builder()
                        .pattern("com{}google{}inject")
                        .relocatedPattern(BuildConfig.RELOCATED_PATTERN + ".guice")
                        .build()
                )
                .relocate(Relocation.builder()
                        .pattern("com{}google{}common")
                        .relocatedPattern(BuildConfig.RELOCATED_PATTERN + ".guava")
                        .build()
                )
                .build()
        );

        loadLibrary(Library.builder()
                .groupId("com{}google{}code{}gson")
                .artifactId("gson")
                .version(BuildConfig.GSON_VERSION)
                .repository(BuildConfig.MAVEN_REPOSITORY)
                .resolveTransitiveDependencies(true)
                .relocate(Relocation.builder()
                        .pattern("com{}google{}common")
                        .relocatedPattern(BuildConfig.RELOCATED_PATTERN + ".guava")
                        .build()
                )
                .relocate(Relocation.builder()
                        .pattern("com{}google{}gson")
                        .relocatedPattern(BuildConfig.RELOCATED_PATTERN + ".gson")
                        .build()
                )
                .build()
        );

        loadLibrary(Library.builder()
                .groupId("tools{}jackson{}dataformat")
                .artifactId("jackson-dataformat-yaml")
                .version(BuildConfig.JACKSON_DATAFORMAT_VERSION)
                .repository(BuildConfig.MAVEN_REPOSITORY)
                .resolveTransitiveDependencies(true)
                .relocate(Relocation.builder()
                        .pattern("com{}fasterxml{}jackson")
                        .relocatedPattern(BuildConfig.RELOCATED_PATTERN + ".jackson")
                        .build()
                )
                .relocate(Relocation.builder()
                        .pattern("tools{}jackson")
                        .relocatedPattern(BuildConfig.RELOCATED_PATTERN + ".jackson")
                        .build()
                )
                .relocate(Relocation.builder()
                        .pattern("org{}snakeyaml{}engine")
                        .relocatedPattern(BuildConfig.RELOCATED_PATTERN + ".snakeyaml")
                        .build()
                )
                .relocate(Relocation.builder()
                        .pattern("com{}google{}common")
                        .relocatedPattern(BuildConfig.RELOCATED_PATTERN + ".guava")
                        .build()
                )
                .relocate(Relocation.builder()
                        .pattern("com{}google{}gson")
                        .relocatedPattern(BuildConfig.RELOCATED_PATTERN + ".gson")
                        .build()
                )
                .build()
        );

        loadLibrary(Library.builder()
                .groupId("com{}zaxxer")
                .artifactId("HikariCP")
                .version(BuildConfig.HIKARICP_VERSION)
                .repository(BuildConfig.MAVEN_REPOSITORY)
                .relocate(Relocation.builder()
                        .pattern("com{}zaxxer{}hikari")
                        .relocatedPattern(BuildConfig.RELOCATED_PATTERN + ".hikari")
                        .build()
                )
                .build()
        );

        loadLibrary(Library.builder()
                .groupId("org{}jdbi")
                .artifactId("jdbi3-core")
                .version(BuildConfig.JDBI3_CORE_VERSION)
                .repository(BuildConfig.MAVEN_REPOSITORY)
                .resolveTransitiveDependencies(true)
                .relocate(Relocation.builder()
                        .pattern("org{}jdbi")
                        .relocatedPattern(BuildConfig.RELOCATED_PATTERN + ".jdbi3")
                        .build()
                )
                .relocate(Relocation.builder()
                        .pattern("com{}google{}common")
                        .relocatedPattern(BuildConfig.RELOCATED_PATTERN + ".guava")
                        .build()
                )
                .build()
        );

        loadLibrary(Library.builder()
                .groupId("org{}jdbi")
                .artifactId("jdbi3-sqlobject")
                .version(BuildConfig.JDBI3_CORE_VERSION)
                .repository(BuildConfig.MAVEN_REPOSITORY)
                .resolveTransitiveDependencies(true)
                .relocate(Relocation.builder()
                        .pattern("org{}jdbi")
                        .relocatedPattern(BuildConfig.RELOCATED_PATTERN + ".jdbi3")
                        .build()
                )
                .relocate(Relocation.builder()
                        .pattern("com{}google{}common")
                        .relocatedPattern(BuildConfig.RELOCATED_PATTERN + ".guava")
                        .build()
                )
                .build()
        );

        loadLibrary(Library.builder()
                .groupId("org{}apache{}commons")
                .artifactId("commons-text")
                .version(BuildConfig.APACHE_COMMONS_TEXT_VERSION)
                .repository(BuildConfig.MAVEN_REPOSITORY)
                .resolveTransitiveDependencies(true)
                .relocate(Relocation.builder()
                        .pattern("org{}apache{}commons")
                        .relocatedPattern(BuildConfig.RELOCATED_PATTERN + ".apache")
                        .build()
                )
                .build()
        );
    }

    @Override
    public void resolveRepositories() {
        addRepository(BuildConfig.MAVEN_REPOSITORY);
        addRepository(BuildConfig.CODEMC_REPOSITORY);
        addRepository(BuildConfig.JITPACK_REPOSITORY);

        addSonatype();
        addJCenter();
    }

}
