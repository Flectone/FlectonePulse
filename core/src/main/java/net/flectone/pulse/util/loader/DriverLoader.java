package net.flectone.pulse.util.loader;

import com.alessiodp.libby.Library;
import com.alessiodp.libby.classloader.IsolatedClassLoader;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.BuildConfig;
import net.flectone.pulse.data.database.Database;
import net.flectone.pulse.data.database.driver.DriverWrapper;
import net.flectone.pulse.processing.resolver.LibraryResolver;
import org.jspecify.annotations.NonNull;

import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * JDBC database driver loader with isolated class loading support.
 * <p>
 * Manages the dynamic loading and registration of JDBC drivers for different database types.
 * Each driver is loaded in an isolated class loader to prevent classpath conflicts
 * and is wrapped in a {@link DriverWrapper} before being registered with the {@link DriverManager}.
 *
 * @author TheFaser
 * @since 1.12.3
 */
@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class DriverLoader {

    private final Map<Database.Type, Driver> registeredDrivers = new ConcurrentHashMap<>();

    private final LibraryResolver libraryResolver;

    /**
     * Returns a JDBC driver for the specified database type.
     * If the driver has already been loaded and cached, returns the cached instance.
     * Otherwise, loads and registers the driver before returning it.
     *
     * @param type the database type for which to retrieve the driver
     * @return the JDBC driver for the specified database type
     */
    public Driver getOrLoad(Database.Type type) {
        Driver driver = registeredDrivers.get(type);
        if (driver != null) return driver;

        driver = load(type);

        registeredDrivers.put(type, driver);

        return driver;
    }

    /**
     * Loads and registers a JDBC driver for the specified database type.
     * <p>
     * Downloads the required library dependencies (if not already present),
     * loads the driver class in an isolated class loader, and registers it
     * with the global {@link DriverManager} via a {@link DriverWrapper} wrapper.
     *
     * @param type the database type for which to load the driver
     * @return the loaded and registered JDBC driver
     * @throws RuntimeException if the driver class cannot be loaded or registered
     */
    public Driver load(Database.Type type) {
        return switch (type) {
            case POSTGRESQL -> {
                libraryResolver.loadLibrary(Library.builder()
                        .groupId("org{}postgresql")
                        .artifactId("postgresql")
                        .version(BuildConfig.POSTGRESQL_VERSION)
                        .repository(BuildConfig.MAVEN_REPOSITORY)
                        .resolveTransitiveDependencies(true)
                        .excludeTransitiveDependency("org.slf4j", "slf4j-api")
                        .isolatedLoad(true)
                        .loaderId("postgresql")
                        .build()
                );

                yield register("postgresql", "org.postgresql.Driver");
            }
            case H2 -> {
                libraryResolver.loadLibrary(Library.builder()
                        .groupId("com{}h2database")
                        .artifactId("h2")
                        .version(BuildConfig.H2_VERSION)
                        .repository(BuildConfig.MAVEN_REPOSITORY)
                        .resolveTransitiveDependencies(true)
                        .excludeTransitiveDependency("org.slf4j", "slf4j-api")
                        .isolatedLoad(true)
                        .loaderId("h2")
                        .build()
                );

                yield register("h2", "org.h2.Driver");
            }
            case SQLITE -> {
                libraryResolver.loadLibrary(Library.builder()
                        .groupId("org{}xerial")
                        .artifactId("sqlite-jdbc")
                        .version(BuildConfig.SQLITE_JDBC_VERSION)
                        .repository(BuildConfig.MAVEN_REPOSITORY)
                        .resolveTransitiveDependencies(true)
                        .excludeTransitiveDependency("org.slf4j", "slf4j-api")
                        .isolatedLoad(true)
                        .loaderId("sqlite")
                        .build()
                );

                yield register("sqlite", "org.sqlite.JDBC");
            }
            case MYSQL -> {
                libraryResolver.loadLibrary(Library.builder()
                        .groupId("com{}mysql")
                        .artifactId("mysql-connector-j")
                        .version(BuildConfig.MYSQL_CONNECTOR_VERSION)
                        .repository(BuildConfig.MAVEN_REPOSITORY)
                        .resolveTransitiveDependencies(true)
                        .isolatedLoad(true)
                        .loaderId("mysql")
                        .build()
                );

                yield register("mysql", "com.mysql.cj.jdbc.Driver");
            }
            case MARIADB -> {
                libraryResolver.loadLibrary(Library.builder()
                        .groupId("org{}mariadb{}jdbc")
                        .artifactId("mariadb-java-client")
                        .version(BuildConfig.MARIADB_JAVA_CLIENT_VERSION)
                        .repository(BuildConfig.MAVEN_REPOSITORY)
                        .resolveTransitiveDependencies(true)
                        .excludeTransitiveDependency("org.slf4j", "slf4j-api")
                        .isolatedLoad(true)
                        .loaderId("mariadb")
                        .build()
                );

                yield register("mariadb", "org.mariadb.jdbc.Driver");
            }
        };
    }

    @NonNull
    private Driver register(@NonNull String loaderId, @NonNull String driverClassName) {
        try {
            IsolatedClassLoader classLoader = libraryResolver.getLibraryManager().getIsolatedClassLoaderById(loaderId);
            if (classLoader == null) {
                throw new IllegalStateException("Isolated class loader '" + loaderId + "' not found");
            }

            Class<?> driverClass = Class.forName(driverClassName, true, classLoader);
            Driver driver = (Driver) driverClass.getDeclaredConstructor().newInstance();

            DriverManager.registerDriver(new DriverWrapper(driver));

            return driver;
        } catch (ReflectiveOperationException | SQLException e) {
            throw new RuntimeException("Failed to register isolated JDBC driver: " + driverClassName, e);
        }
    }

}