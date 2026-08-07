package net.flectone.pulse.util.loader;

import com.alessiodp.libby.Library;
import com.alessiodp.libby.classloader.IsolatedClassLoader;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.BuildConfig;
import net.flectone.pulse.constant.DatabaseType;
import net.flectone.pulse.exception.LibraryLoadException;
import net.flectone.pulse.persistence.database.driver.DriverWrapper;
import net.flectone.pulse.resolver.LibraryResolver;
import org.jspecify.annotations.NonNull;

import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class DriverLoader {

    private final Map<DatabaseType, Driver> registeredDrivers = new ConcurrentHashMap<>();

    private final LibraryResolver libraryResolver;

    public Driver getOrLoad(DatabaseType type) {
        Driver driver = registeredDrivers.get(type);
        if (driver != null) return driver;

        driver = load(type);

        registeredDrivers.put(type, driver);

        return driver;
    }

    public Driver load(DatabaseType type) {
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
            IsolatedClassLoader classLoader = libraryResolver.getIsolatedClassLoaderById(loaderId);
            if (classLoader == null) {
                throw new LibraryLoadException("Isolated class loader '" + loaderId + "' not found");
            }

            Class<?> driverClass = Class.forName(driverClassName, true, classLoader);
            Driver driver = (Driver) driverClass.getDeclaredConstructor().newInstance();

            DriverManager.registerDriver(new DriverWrapper(driver));

            return driver;
        } catch (ReflectiveOperationException | SQLException e) {
            throw new LibraryLoadException("Failed to register isolated JDBC driver " + driverClassName, e);
        }
    }

}